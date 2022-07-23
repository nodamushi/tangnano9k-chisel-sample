set project [lindex $argv 0]
set device_family [lindex $argv 1]
set device_part [lindex $argv 2]


set_option -output_base_name $project
set_device -name $device_family $device_part

if { [info exists ::env(NDS_GWIN_OPTIONS)] } {
  foreach {opt arg} $env(NDS_GWIN_OPTIONS) {
    set_option $opt $arg
  }
}

if { [info exists ::env(NDS_GWIN_VERILOG_FILES)] } {
  foreach src $env(NDS_GWIN_VERILOG_FILES) {
    add_file -type verilog $src
  }
}

if { [info exists ::env(NDS_GWIN_VHDL_FILES)] } {
  foreach src $env(NDS_GWIN_VHDL_FILES) {
    add_file -type vhdl $src
  }
}

if { [info exists ::env(NDS_GWIN_CST_FILES)] } {
  foreach src $env(NDS_GWIN_CST_FILES) {
    add_file -type cst $src
  }
}

if { [info exists ::env(NDS_GWIN_SDC_FILES)] } {
  foreach src $env(NDS_GWIN_SDC_FILES) {
    add_file -type sdc $src
  }
}

run all
