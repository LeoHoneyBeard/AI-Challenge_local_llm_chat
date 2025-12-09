cmake_minimum_required(VERSION 3.22.1)
project("ai-challenge-local-llm")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(LLAMA_CPP_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party/llama.cpp)
if (NOT EXISTS ${LLAMA_CPP_DIR}/CMakeLists.txt)
    message(FATAL_ERROR "llama.cpp sources are missing at ${LLAMA_CPP_DIR}. Run git submodule update or clone the repo.")
endif()

# Force-enable helper libraries we depend on
set(LLAMA_BUILD_COMMON ON CACHE BOOL "Build common utils library" FORCE)
set(LLAMA_CURL OFF CACHE BOOL "Disable curl dependency for Android build" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "Disable OpenMP for stability on emulators" FORCE)

add_subdirectory(${LLAMA_CPP_DIR} ${CMAKE_CURRENT_BINARY_DIR}/llama-build)

add_library(llama-android SHARED
    llama-android.cpp
)

target_include_directories(llama-android
    PRIVATE
    ${LLAMA_CPP_DIR}
    ${LLAMA_CPP_DIR}/include
    ${LLAMA_CPP_DIR}/common
)

target_link_libraries(llama-android
    PRIVATE
    llama
    common
    android
    log
)
