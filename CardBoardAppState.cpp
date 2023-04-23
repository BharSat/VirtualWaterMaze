#include <jni.h>
#include <memory>
#include <mutex>
#include <thread>
#include <vector>

#include "com_jme3_system_android_JmeSystem.h"
#include "com_jme3_system_android_NativeLibraryLoader.h"
#include "com_jme3_system_android_WindowSurface.h"
#include "com_jme3_system_android_WindowSurfaceFactory.h"
#include "com_jme3_system_ios_IOSInput.h"
#include "glm/glm.hpp"
#include "glm/gtc/quaternion.hpp"
#include "glm/gtx/euler_angles.hpp"
#include "vr/gvr/capi/include/gvr.h"
#include "vr/gvr/capi/include/gvr_audio.h"
#include "vr/gvr/capi/include/gvr_types.h"

extern "C" {
    JNIEXPORT void JNICALL Java_com_jme3_app_Application_initialize(JNIEnv *env, jobject obj, jlong appPtr);
    JNIEXPORT void JNICALL Java_com_jme3_app_Application_terminate(JNIEnv *env, jobject obj, jlong appPtr);
    JNIEXPORT void JNICALL Java_com_jme3_app_Application_reshape(JNIEnv *env, jobject obj, jlong appPtr, jint width, jint height);
    JNIEXPORT void JNICALL Java_com_jme3_app_Application_update(JNIEnv *env, jobject obj, jlong appPtr);
    JNIEXPORT void JNICALL Java_com_jme3_app_Application_requestClose(JNIEnv *env, jobject obj, jlong appPtr);
};

namespace {

class CardboardAppState : public Jme::BaseAppState {
 public:
    CardboardAppState(JNIEnv *env, jobject activity, jobject vrView)
      : env_(env), activity_(env->NewGlobalRef(activity)), vr_view_(env->NewGlobalRef(vrView)) {}

    virtual void initialize(Application *app) override {
        // Initialize the GVR context and renderer
        gvr::GvrApi::Options options;
        options.enable_logging = false;
        gvr_api_ = gvr::GvrApi::WrapNonOwned(gvr::GvrApi::Create(env_->GetJavaVM(), env_, activity_, options));
        gvr_audio_ = gvr::AudioApi::WrapNonOwned(gvr_api_->CreateAudioApi());
        gvr_render_params_ = gvr_api_->CreateDefaultViewerParameters();
        gvr_render_params_->SetNeckModelEnabled(false);
        gvr_render_params_->SetAutoDriftCorrectionEnabled(false);
        gvr_render_params_->SetPerformanceHudEnabled(false);
        gvr_render_params_->SetLensOffset(gvr::Vec2f(0.0f, 0.0f));
        gvr_render_params_->SetViewport(0, 0, gvr_viewport_width_, gvr_viewport_height_);
        gvr_render_params_->SetEyeToLensDistance(0.015f);
        gvr_render_params_->SetInterpupillaryDistance(0.063f);
        gvr_render_params_->SetEyeOffset(gvr::Eye::kLeft, gvr::Vec3f(-0.032f, 0.0f, 0.0f));
        gvr_render_params_->SetEyeOffset(gvr::Eye::kRight, gvr::Vec3f(0.032f, 0.0f, 0.0f));
        gvr_render_params_->SetProjectionChanged();
        gvr_api_->SetDefaultViewerProfile("Cardboard");
        gvr_api_->RefreshViewerProfile();
        gvr_renderer_ = gvr::Renderer::WrapNonOwned(gvr_api_->CreateRenderer());
        gvr_renderer_->InitializeGl();
        //gvr
        // Create a new thread to poll for Cardboard events
        running_ = true;
        thread_ = std::make_unique<std::thread>([this]() { PollEvents(); });
    }

    virtual void cleanup(Application *app) override {
        // Stop the event polling thread and release resources
        running_ = false;
        thread_->join();
        thread_.reset();
        gvr_renderer_.reset();
        gvr_audio_.reset();
        gvr_api_.reset();
        env_->DeleteGlobalRef(vr_view_);
        env_->DeleteGlobalRef(activity_);
    }

    virtual void update(Application *app) override {
        // Update the GVR frame and render it to the screen
        gvr::ClockTimePoint next_vsync = gvr::GvrApi::GetTimePointNow();
        while (gvr::GvrApi::GetTimePointNow() >= next_vsync) {
        next_vsync = gvr::GvrApi::TimePointAfterVsync(gvr_api_->GetVsyncIntervalNano());
        }
        const gvr::Mat4f head_view = gvr_api_->GetHeadSpaceFromStartSpaceRotation(gvr::Quatf(head_rotation_));
        const gvr::Mat4f eye_view_left = gvr_api_->GetEyeFromHeadMatrix(gvr::Eye::kLeft) * head_view;
        const gvr::Mat4f eye_view_right = gvr_api_->GetEyeFromHeadMatrix(gvr::Eye::kRight) * head_view;
        const gvr::Mat4f projection_left = gvr_api_->GetProjectionMatrix(gvr::Eye::kLeft, near_clip_, far_clip_);
        const gvr::Mat4f projection_right = gvr_api_->GetProjectionMatrix(gvr::Eye::kRight, near_clip_, far_clip_);
        const gvr::Rectf fov_left = gvr_api_->GetEyeFoV(gvr::Eye::kLeft);
        const gvr::Rectf fov_right = gvr_api_->GetEyeFoV(gvr::Eye::kRight);
        const gvr::Sizei render_size = gvr_api_->GetMaximumEffectiveRenderTargetSize();
        gvr_renderer_->BeginFrame(gvr_render_params_);
        glViewport(0, 0, render_size.width, render_size.height);
        gvr_renderer_->BeginEye(gvr::Eye::kLeft);
        glMatrixMode(GL_PROJECTION);
        glLoadMatrixf(glm::value_ptr(projection_left));
        glMatrixMode(GL_MODELVIEW);
        glLoadMatrixf(glm::value_ptr(eye_view_left));
        gvr_renderer_->DrawFrame();
        gvr_renderer_->EndEye(gvr::Eye::kLeft);
        gvr_renderer_->BeginEye(gvr::Eye::kRight);
        glMatrixMode(GL_PROJECTION);
        glLoadMatrixf(glm::value_ptr(projection_right));
        glMatrixMode(GL_MODELVIEW);
        glLoadMatrixf(glm::value_ptr(eye_view_right));
        gvr_renderer_->DrawFrame();
        gvr_renderer_->EndEye(gvr::Eye::kRight);
        gvr_renderer_->EndFrame();
    }

    virtual void onInputEvent(const CardboardAppInputEvent &event) {
        // Handle Cardboard input events
        switch (event.type) {
            case CARDBOARD_APP_EVENT_BACK_BUTTON:
                app_->stop();
                break;
            case CARDBOARD_APP_EVENT_HEAD_TRACKING:
                head_rotation_ = glm::quat(event.head_tracking.quaternion.w,
                event.head_tracking.quaternion.x,
                event.head_tracking.quaternion.y,
                event.head_tracking.quaternion.z);
                break;
            default:
                break;
        }
    }

 private:
    // Poll for Cardboard events
    void PollEvents() {
        JNIEnv *env;
        java_vm_->AttachCurrentThread(&env, nullptr);
        while (running_) {
            CardboardAppInputEvent event;
            if (GetCardboardAppInputEvent(&event)) {
                onInputEvent(event);
            }
        }
        java_vm_->DetachCurrentThread();
    }

    // Get a JNIEnv pointer from the Java VM and store the global refs
    void GetJNIEnv() {
        if (java_vm_->GetEnv(reinterpret_cast<void **>(&env_), JNI_VERSION_1_6) != JNI_OK) {
            java_vm_->AttachCurrentThread(&env_, nullptr);
        }
        jclass clazz = env_->GetObjectClass(activity_);
        jmethodID get_window = env_->GetMethodID(clazz, "getWindow", "()Landroid/view/Window;");
        jobject window = env_->CallObjectMethod(activity_, get_window);
        jmethodID get_decor_view = env_->GetMethodID(clazz, "getDecorView", "()Landroid/view/View;");
        jobject decor_view = env_->CallObjectMethod(activity_, get_decor_view);
        jmethodID get_display = env_->GetMethodID(clazz, "getDisplay", "()Landroid/view/Display;");
        jobject display = env_->CallObjectMethod(activity_, get_display);
        jmethodID get_context = env_->GetMethodID(clazz, "getApplicationContext", "()Landroid/content/Context;");
        jobject context = env_->CallObjectMethod(activity_, get_context);
        jobject vr_view = env_->NewObject(vr_view_class_, vr_view_constructor_,
        env_->NewGlobalRef(window), env_->NewGlobalRef(decor_view),
        env_->NewGlobalRef(display), env_->NewGlobalRef(context));
        vr_view_ = env_->NewGlobalRef(vr_view);
        activity_ = env_->NewGlobalRef(activity_);
        env_->DeleteLocalRef(window);
        env_->DeleteLocalRef(decor_view);
        env_->DeleteLocalRef(display);
        env_->DeleteLocalRef(context);
    }

    // Initialize the GVR API and resources
    bool InitGvr() {
        gvr_api_ = gvr::GvrApi::WrapNonOwned(gvr::GvrApi::Create(env_->GetJavaVM(), env_->functions));
        if (!gvr_api_->InitializeGl()) {
            LOGE("Failed to initialize GVR GL context");
        return false;
        }
        gvr_audio_ = gvr::AudioApi::WrapNonOwned(gvr_api_->CreateAudioApi());
        gvr_audio_->Resume();
        gvr_renderer_ = gvr::Renderer::WrapNonOwned(gvr_api_->CreateRenderer());
        return true;
    }

    // Initialize the GVR render parameters
    void InitGvrRenderParams() {
        gvr_render_params_ = gvr_renderer_->CreateRenderParams();
        gvr_render_params_->SetEyeBufferSize(gvr::Eye::kLeft, gvr::Sizei{kRenderWidth_, kRenderHeight_});
        gvr_render_params_->SetEyeBufferSize(gvr::Eye::kRight, gvr::Sizei{kRenderWidth_, kRenderHeight_});
        gvr_render_params_->SetFramebufferObject(gvr_api_->CreateBufferSpec());
    }

    // Create the JME application
    bool InitJme() {
        app_ = new SimpleApplication();
        app_->setShowSettings(false);
        app_->setSettings(settings_);
        app_->start(JmeContext::Type::Headless);
        return true;
    }

    // Create the OpenGL context and set the GVR viewport
    void InitGl() {
        EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        EGLint major, minor;
        eglInitialize(display, &major, &minor);
        EGLint config_attribs[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_DEPTH_SIZE, 0,
            EGL_STENCIL_SIZE, 0,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT | EGL_PBUFFER_BIT,
            EGL_RENDERABLE_TYPE,EGL_OPENGL_ES3_BIT_KHR,
            EGL_NONE
        };
        EGLConfig config;
        EGLint num_configs;
        eglChooseConfig(display, config_attribs, &config, 1, &num_configs);
        EGLint context_attribs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL_NONE
        };
        EGLContext context = eglCreateContext(display, config, EGL_NO_CONTEXT, context_attribs);
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, context);
        gvr_renderer_->InitializeGl();
        glViewport(0, 0, kRenderWidth_, kRenderHeight_);
    }

    // Load the JME scene and attach it to the root node
    void LoadScene() {
        Node *root_node = app_->getRootNode();
        AssetManager *asset_mgr = app_->getAssetManager();
        Spatial *scene = asset_mgr->loadModel("Scenes/HelloJME3/HelloJME3.j3o");
        root_node->attachChild(scene);
    }

    // Render the scene to the GVR framebuffer
    void RenderFrame() {
        // Update the JME scene
        float tpf = static_cast<float>(timer_->getTimePerFrame());
        app_->update();
        app_->render();
        // Get the JME framebuffer
        const FrameBuffer *fb = app_->getRenderManager()->getMainBuffer();
        // Set the GVR viewport and clear the framebuffer
        glViewport(0, 0, kRenderWidth_, kRenderHeight_);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // Render the JME framebuffer to the GVR framebuffer
        gvr_renderer_->BeginFrame(gvr_render_params_);
        gvr::Mat4f left_eye_matrix = gvr_api_->GetEyeFromHeadMatrix(gvr::Eye::kLeft);
        gvr_renderer_->BindBuffer(fb->getId());
        gvr_renderer_->SetViewport(0, 0, kRenderWidth_, kRenderHeight_);
        gvr_renderer_->SetEyeMatrix(gvr::Eye::kLeft, ToGvrMatrix(left_eye_matrix));
        gvr_renderer_->DrawFramebuffer();
        gvr_renderer_->Unbind();
        gvr::Mat4f right_eye_matrix = gvr_api_->GetEyeFromHeadMatrix(gvr::Eye::kRight);
        gvr_renderer_->BindBuffer(fb->getId());
        gvr_renderer_->SetViewport(kRenderWidth_, 0, kRenderHeight_);
        gvr_renderer_->SetEyeMatrix(gvr::Eye::kRight, ToGvrMatrix(right_eye_matrix));
        gvr_renderer_->DrawFramebuffer();
        gvr_renderer_->Unbind();
        gvr_renderer_->EndFrame(gvr::Eye::kLeft, gvr::Mat4f());
        gvr_renderer_->EndFrame(gvr::Eye::kRight, gvr::Mat4f());
    }

    // Convert a JME matrix to a GVR matrix
    static gvr::Mat4f ToGvrMatrix(const Matrix4f &mat) {
        const float *m = mat.data();
        gvr::Mat4f gvr_mat(m[0], m[4], m[8], m[12],
        m[1], m[5], m[9], m[13],
        m[2], m[6], m[10], m[14],
        m[3], m[7], m[11], m[15]);
        return gvr_mat;
    }

 private:
    // JME objects
    Settings *settings_;
    SimpleApplication *app_;
    Timer *timer_;

    // GVR objects
    gvr_context *gvr_context_;
    gvr::CardboardApi *gvr_api_;
    std::unique_ptrgvr::Renderer gvr_renderer_;
    std::unique_ptrgvr::BufferViewportList gvr_buffer_viewports_;
    std::unique_ptrgvr::BufferViewport gvr_left_viewport_;
    std::unique_ptrgvr::BufferViewport gvr_right_viewport_;
    std::unique_ptrgvr::SwapChain gvr_swap_chain_;
    std::unique_ptrgvr::FrameBufferObject gvr_framebuffer_;
    std::unique_ptrgvr::RenderParams gvr_render_params_;
};

// Declare the JNI functions
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_example_jmonkeycardboard_JmeCardboardAppState_nativeCreateAppState(JNIEnv *env, jclass cls, jobject activity) {
    JmeCardboardAppState *app_state = new JmeCardboardAppState(env, activity);
    return reinterpret_cast<jlong>(app_state);
}

JNIEXPORT void JNICALL Java_com_example_jmonkeycardboard_JmeCardboardAppState_nativeDestroyAppState(JNIEnv *env, jclass cls, jlong app_state_ptr) {
    JmeCardboardAppState app_state = reinterpret_cast<JmeCardboardAppState>(app_state_ptr);
    delete app_state;
}

#ifdef __cplusplus
}
#endif