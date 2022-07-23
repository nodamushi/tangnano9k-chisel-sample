cmake_minimum_required(VERSION 3.14)
project(chisel-sample)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR}/cmake)
find_package(GWIN REQUIRED)

set(CHISEL_OUT_DIR ${CMAKE_CURRENT_BINARY_DIR}/chisel)
add_custom_target(chisel
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  COMMAND sbt "run ${CHISEL_OUT_DIR}")

add_subdirectory(gwin)
