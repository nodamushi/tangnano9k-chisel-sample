set(GWIN_DEFAULT_OPTION -verilog_std sysv2017 -vhdl_std vhd2008 -print_all_synthesis_warning 1 CACHE STRING "Default Option")

find_path(GWIN_IDE_BIN_DIR
  gw_sh
  PATHS ${GWIN_ROOT}
  PATH_SUFFIXES IDE/bin)

find_path(GWIN_PORGAMMER_BIN_DIR
  programmer_cli
  PATHS ${GWIN_ROOT}
  PATH_SUFFIXES Programmer/bin)

find_path(GWIN_OPENFPGALOADER_BIN_DIR
  openFPGALoader
  PATHS ${OPENFPGALOADER_ROOT})

if (${GWIN_PORGAMMER_BIN_DIR} STREQUAL "GWIN_PORGAMMER_BIN_DIR-NOTFOUND")
  set(GWIN_Found_Programmer FALSE)
  set(GWIN_Programmer "")
else ()
  set(GWIN_Found_Programmer TRUE)
  set(GWIN_Programmer ${GWIN_PORGAMMER_BIN_DIR}/programmer_cli)
endif()

if (${GWIN_OPENFPGALOADER_BIN_DIR} STREQUAL "GWIN_OPENFPGALOADER_BIN_DIR-NOTFOUND")
  set(GWIN_Found_OPENFPGALOADER FALSE)
  set(GWIN_OPENFPGALOADER "")
else ()
  set(GWIN_Found_OPENFPGALOADER TRUE)
  set(GWIN_OPENFPGALOADER ${GWIN_OPENFPGALOADER_BIN_DIR}/openFPGALoader)
endif()

# save current directory
set(GWIN_CMAKE_DIR ${CMAKE_CURRENT_LIST_DIR})
set(GWIN_TCL_DIR ${CMAKE_CURRENT_LIST_DIR}/tcl)


# hide variables
mark_as_advanced(GWIN_PORGAMMER_BIN_DIR GWIN_IDE_BIN_DIR GWIN_CMAKE_DIR GWIN_TCL_DIR)

# find package
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(GWIN
  REQUIRED_VARS
    GWIN_IDE_BIN_DIR
    GWIN_TCL_DIR)

set(GWIN_SH ${GWIN_IDE_BIN_DIR}/gw_sh)

# add_gwin_project(
#  <project>
#  [TOP <top module>]
#  [BOARD <bard name>]
#  [FAMILY <FPGA family name>]
#  [PART <FPGA part name>]
#  [VERILOG <verilog file>...]
#  [VHDL <vhdl file>...]
#  [CST <cst file>...]
#  [SDC <sdc file>...]
#  [OPTIONS [<option> <arg>]...]
#  [DEPENDS <depends target>...]
# )
#
# Define targets:
#   ${project}                 : Generate bitstreams
#   ${project}.clean           : Delete bitstream directory
#   ${project}.openFPGALoader  : Program with openFPGALoader
#   ${project}.program         : Program with GWIN Programmer
#
# Argument:
#  project: target name
#
# Options:
#  TOP     : top module name
#  BOARD   : tangnano1k/tangnano4k/tangnano9k or else
#  FAMILY  : FPGA family name. When BOARD is tangnano1k/4k/9k, you can skip this parameter.
#  PART    : FPGA part name. When BOARD is tangnano1k/4k/9k, you can skip this parameter.
#  VERILOG : SystemVerilog source code
#  VHDL    : VHDL source code
#  CST     : .cst files
#  SDC     : .sdc files
#  OPTIONS : `set_option` tcl command option
#  DEPENDS : depends
#
function(add_gwin_project project)
  cmake_parse_arguments(
    GWIN_ADD_PROJECT
    ""
    "TOP;FAMILY;PART;BOARD"
    "VERILOG;VHDL;CST;SDC;OPTIONS;DEPENDS"
    ${ARGN})

  if(GWIN_ADD_PROJECT_BOARD)
    if(${GWIN_ADD_PROJECT_BOARD} STREQUAL "tangnano9k")
      set(GWIN_ADD_PROJECT_FAMILY GW1NR-9C)
      set(GWIN_ADD_PROJECT_PART GW1NR-LV9QN88PC6/I5)
    elseif(${GWIN_ADD_PROJECT_BOARD} STREQUAL "tangnano4k")
      set(GWIN_ADD_PROJECT_FAMILY GW1NSR-4C)
      set(GWIN_ADD_PROJECT_PART GW1NSR-LV4CQN48PC6/I5)
    elseif(${GWIN_ADD_PROJECT_BOARD} STREQUAL "tangnano1k")
      set(GWIN_ADD_PROJECT_FAMILY GW1NR-1)
      set(GWIN_ADD_PROJECT_PART GW1NZ-LV1QN48C6/I5)
    endif()
  endif()

  if(NOT GWIN_ADD_PROJECT_FAMILY)
    message(FATAL_ERROR "add_gwin_project: FAMILY is not defined")
  endif()
  if(NOT GWIN_ADD_PROJECT_PART)
    message(FATAL_ERROR "add_gwin_project: PART is not defined")
  endif()


  # options
  set(GWIN_ADD_PROJECT_OPTS)

  list(APPEND GWIN_ADD_PROJECT_OPTS ${GWIN_DEFAULT_OPTION})
  if(GWIN_ADD_PROJECT_TOP)
    list(APPEND GWIN_ADD_PROJECT_OPTS -top_module ${GWIN_ADD_PROJECT_TOP})
  endif()
  if(GWIN_ADD_PROJECT_OPTIONS)
    list(APPEND GWIN_ADD_PROJECT_OPTS ${GWIN_ADD_PROJECT_OPTIONS})
  endif()

  # fix relative path
  set(GWIN_ADD_PROJECT_VERILOG_0)
  foreach(GWIN_ADD_PROJECT_SRC IN LISTS GWIN_ADD_PROJECT_VERILOG)
    if (IS_ABSOLUTE ${GWIN_ADD_PROJECT_SRC})
      list(APPEND GWIN_ADD_PROJECT_VERILOG_0 ${GWIN_ADD_PROJECT_SRC})
    else()
      list(APPEND GWIN_ADD_PROJECT_VERILOG_0 ${CMAKE_CURRENT_SOURCE_DIR}/${GWIN_ADD_PROJECT_SRC})
    endif()
  endforeach()

  set(GWIN_ADD_PROJECT_VHDL_0)
  foreach(GWIN_ADD_PROJECT_SRC IN LISTS GWIN_ADD_PROJECT_VHDL)
    if (IS_ABSOLUTE ${GWIN_ADD_PROJECT_SRC})
      list(APPEND GWIN_ADD_PROJECT_VHDL_0 ${GWIN_ADD_PROJECT_SRC})
    else()
      list(APPEND GWIN_ADD_PROJECT_VHDL_0 ${CMAKE_CURRENT_SOURCE_DIR}/${GWIN_ADD_PROJECT_SRC})
    endif()
  endforeach()

  set(GWIN_ADD_PROJECT_CST_0)
  foreach(GWIN_ADD_PROJECT_SRC IN LISTS GWIN_ADD_PROJECT_CST)
    if (IS_ABSOLUTE ${GWIN_ADD_PROJECT_SRC})
      list(APPEND GWIN_ADD_PROJECT_CST_0 ${GWIN_ADD_PROJECT_SRC})
    else()
      list(APPEND GWIN_ADD_PROJECT_CST_0 ${CMAKE_CURRENT_SOURCE_DIR}/${GWIN_ADD_PROJECT_SRC})
    endif()
  endforeach()

  set(GWIN_ADD_PROJECT_SDC_0)
  foreach(GWIN_ADD_PROJECT_SRC IN LISTS GWIN_ADD_PROJECT_SDC)
    if (IS_ABSOLUTE ${GWIN_ADD_PROJECT_SRC})
      list(APPEND GWIN_ADD_PROJECT_SDC_0 ${GWIN_ADD_PROJECT_SRC})
    else()
      list(APPEND GWIN_ADD_PROJECT_SDC_0 ${CMAKE_CURRENT_SOURCE_DIR}/${GWIN_ADD_PROJECT_SRC})
    endif()
  endforeach()

  set(GWIN_ADD_PROJECT_IMPL_DIR ${CMAKE_CURRENT_BINARY_DIR}/impl)
  set(GWIN_ADD_PROJECT_PNR_DIR ${GWIN_ADD_PROJECT_IMPL_DIR}/pnr)
  set(GWIN_ADD_PROJECT_FS ${GWIN_ADD_PROJECT_PNR_DIR}/${project}.fs)
  add_custom_target(${project} SOURCES ${GWIN_ADD_PROJECT_FS})
  add_custom_command(
    OUTPUT ${GWIN_ADD_PROJECT_FS}
    DEPENDS ${GWIN_ADD_PROJECT_DEPENDS}
    COMMAND
      NDS_GWIN_VERILOG_FILES="${GWIN_ADD_PROJECT_VERILOG_0}"
      NDS_GWIN_VHDL_FILES="${GWIN_ADD_PROJECT_VHDL_0}"
      NDS_GWIN_OPTIONS="${GWIN_ADD_PROJECT_OPTS}"
      NDS_GWIN_CST_FILES="${GWIN_ADD_PROJECT_CST_0}"
      NDS_GWIN_SDC_FILES="${GWIN_ADD_PROJECT_SDC_0}"
      # run gw-sh
      ${GWIN_SH} ${GWIN_TCL_DIR}/run.tcl ${project} ${GWIN_ADD_PROJECT_FAMILY} ${GWIN_ADD_PROJECT_PART})
  set_target_properties(${project}
    PROPERTIES
      PROJECT_NAME   ${project}
      PROJECT_DIR    ${CMAKE_CURRENT_BINARY_DIR}
      IMPL_DIR       ${GWIN_ADD_PROJECT_IMPL_DIR}
      PNR_DIR        ${GWIN_ADD_PROJECT_PNR_DIR}
      FS_BITSTREAM   ${GWIN_ADD_PROJECT_FS}
      BIN_BITSTREAM  ${GWIN_ADD_PROJECT_PNR_DIR}/${project}.bin
      BINX_BITSTREAM ${GWIN_ADD_PROJECT_PNR_DIR}/${project}.binx)
  add_custom_target(${project}.clean
    COMMAND
      ${CMAKE_COMMAND} -E remove_directory ${GWIN_ADD_PROJECT_IMPL_DIR})
  if (GWIN_Found_OPENFPGALOADER)
    add_custom_target(${project}.openFPGALoader
      DEPENDS ${project}
      COMMAND
        ${GWIN_OPENFPGALOADER} -b ${GWIN_ADD_PROJECT_BOARD} --write-flash ${GWIN_ADD_PROJECT_FS})
  endif()

  if (GWIN_Found_Programmer)
    add_custom_target(${project}.programmer
      WORKING_DIRECTORY ${GWIN_PORGAMMER_BIN_DIR}
      DEPENDS ${project}
      COMMAND
        ./programmer_cli --device ${GWIN_ADD_PROJECT_FAMILY} --run 2 --fsFile ${GWIN_ADD_PROJECT_FS}
      )
  endif()

endfunction()