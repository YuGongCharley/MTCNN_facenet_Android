cmake_minimum_required(VERSION 3.4.1)


#include头文件目录
include_directories(src/main/cpp/include
        src/main/cpp/)

#source directory源文件目录
file(GLOB Face_SRC src/main/cpp/*.h
        src/main/cpp/*.cpp)
set(Face_COMPILE_CODE ${Face_SRC})


#添加ncnn库
add_library(libncnn STATIC IMPORTED )
set_target_properties(libncnn
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)

#添加opencv库
add_library(libopencv_java3 SHARED IMPORTED )
set_target_properties(libopencv_java3
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

#编译为动态库
add_compile_options(-Wall)
add_library(charleyszcFace SHARED ${Face_COMPILE_CODE})

#添加工程所依赖的库
find_library(  log-lib log )
target_link_libraries(  charleyszcFace
        libncnn
        libopencv_java3
        jnigraphics
        z
        ${log-lib} )
