#include <jni.h>
#include <cstring>
#include <cstdlib>
#include <ctime>

#include "GLES3/gl32.h"
#include "cardboard.h"

JavaVM *vm;
CardboardHeadTracker *headTracker;
CardboardDistortionRenderer *distortionRenderer;

float out_position[3];
float out_orientation[4];
GLuint leftEyeTex, rightEyeTex;

CardboardEyeTextureDescription left_eye_texture_description;
CardboardEyeTextureDescription right_eye_texture_description;

int width, height;

int64_t GetBootTimeNano() {
    struct timespec res;
    clock_gettime(CLOCK_BOOTTIME, &res);
    return (res.tv_sec * 1000000000) + res.tv_nsec;
}

extern "C" JNIEXPORT void JNICALL
Java_com_spatial_learning_cardboard_CardboardRenderer_initialize(JNIEnv *env, jclass clazz,
                                                                 jobject app, jint width_,
                                                                 jint height_) {
    env->GetJavaVM(&vm);
    Cardboard_initializeAndroid(vm, app);
    headTracker = CardboardHeadTracker_create();
    distortionRenderer = CardboardOpenGlEs3DistortionRenderer_create();
    glGenTextures(1, &leftEyeTex);
    glGenTextures(1, &rightEyeTex);
    width = width_;
    height = height_;
}

extern "C" JNIEXPORT void JNICALL
Java_com_spatial_learning_cardboard_CardboardRenderer_cleanup(JNIEnv *env, jclass clazz) {
    CardboardHeadTracker_destroy(headTracker);
    CardboardDistortionRenderer_destroy(distortionRenderer);
}

extern "C" JNIEXPORT void JNICALL
Java_com_spatial_learning_cardboard_CardboardRenderer_render(JNIEnv *env, jclass clazz,
                                                             jbyteArray data_left,
                                                             jlong offset_left, jlong length_left,
                                                             jbyteArray data_right,
                                                             jlong offset_right,
                                                             jlong length_right) {
    jbyte *leftEyeDataPtr = env->GetByteArrayElements(data_left, NULL);
    char *leftEyeHeapPtr = (char *) malloc(length_left);
    memcpy(leftEyeHeapPtr + offset_left, leftEyeDataPtr, length_left);
    env->ReleaseByteArrayElements(data_left, leftEyeDataPtr, JNI_ABORT);

    jbyte *rightEyeDataPtr = env->GetByteArrayElements(data_right, NULL);
    char *rightEyeHeapPtr = (char *) malloc(length_right);
    memcpy(rightEyeHeapPtr + offset_right, rightEyeDataPtr, length_right);
    env->ReleaseByteArrayElements(data_right, rightEyeDataPtr, JNI_ABORT);

    CardboardHeadTracker_getPose(headTracker, GetBootTimeNano(),
                                 CardboardViewportOrientation::kLandscapeLeft, &out_position[0],
                                 &out_orientation[0]);

    glBindTexture(GL_TEXTURE_2D, leftEyeTex);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE,
                 leftEyeHeapPtr);
    left_eye_texture_description.texture = leftEyeTex;

    glBindTexture(GL_TEXTURE_2D, rightEyeTex);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE,
                 rightEyeHeapPtr);
    right_eye_texture_description.texture = rightEyeTex;

    CardboardDistortionRenderer_renderEyeToDisplay(distortionRenderer, 0, 0, 0, width, height,
                                                   &left_eye_texture_description,
                                                   &right_eye_texture_description);

}
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_spatial_learning_cardboard_CardboardRenderer_getPosition(JNIEnv *env, jclass clazz) {
    jfloatArray result = env->NewFloatArray(3);
    jfloat res[3];
    res[0] = out_position[0];
    res[1] = out_position[1];
    res[2] = out_position[2];
    env->SetFloatArrayRegion(result, 0, 3, res);
    return result;
}
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_spatial_learning_cardboard_CardboardRenderer_getOrientation(JNIEnv *env, jclass clazz) {
    jfloatArray result = env->NewFloatArray(4);
    jfloat res[4];
    res[0] = out_orientation[0];
    res[1] = out_orientation[1];
    res[2] = out_orientation[2];
    res[3] = out_orientation[3];
    env->SetFloatArrayRegion(result, 0, 4, res);
    return result;
}