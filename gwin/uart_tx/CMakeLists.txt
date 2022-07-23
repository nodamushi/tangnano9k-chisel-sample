cmake_minimum_required(VERSION 3.14)
project(vivado-prj)

add_gwin_project(uart_tx
  BOARD   tangnano9k
  TOP     UartTxSampleTop
  VERILOG ${CHISEL_OUT_DIR}/UartTxSampleTop.v
  CST     src/uart_tx.cst
  SDC     src/uart_tx.sdc
)
