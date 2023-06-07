# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("cardboard")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        cardboard

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        CardboardRenderer.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library(android-lib android)
find_library(GLESv2-lib GLESv2)
find_library(GLESv3-lib GLESv3)
find_library(log-lib log)

set(libs_dir ${CMAKE_CURRENT_SOURCE_DIR}/libraries)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(
        cardboard
        ${android-lib}
        ${GLESv2-lib}
        ${GLESv3-lib}
        ${log-lib}
        ${libs_dir}/jni/${ANDROID_ABI}/libGfxPluginCardboard.so)

target_include_directories(cardboard PRIVATE "D:/bhargav/projects/SpatialLearningVWM/sdk/include")
target_include_directories(cardboard PRIVATE "D:/Programs/AndroidSDK/ndk/21.4.7075529/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include")