package nds.uart

import chisel3.Input
import chisel3.Output
import chisel3._
import chisel3.Module
import chisel3.util.log2Ceil
import chisel3.util.Decoupled
import java.nio.charset.StandardCharsets

class UartTxSampleTop(message: String, clkFrequency: Int, baudrate: Int)
    extends RawModule {
  val clock = IO(Input(Clock()))
  val rstn = IO(Input(Bool()))
  val tx = IO(Output(Bool()))
  val led = IO(Output(Bool()))

  val reset = Wire(Reset())
  reset := !rstn

  withClockAndReset(clock, reset) {
    val utf8Array = message.getBytes(StandardCharsets.UTF_8)
    val len = utf8Array.length + 1
    val stateBits = log2Ceil(len - 1)

    val msg = Wire(Vec(len, UInt(8.W)))
    val utx = Module(UartTx(8, baudrate, clkFrequency))
    val state = RegInit(0.U(stateBits.W))

    for (i <- 0 until len - 1) {
      msg(i) := (utf8Array(i) & 0xff).U
    }
    msg(len - 1) := '\n'.U

    utx.io.din.valid := true.B
    utx.io.din.bits := msg(state)
    tx := utx.io.tx

    when(utx.io.din.ready) {
      state := Mux(state === (len - 1).U, 0.U, state + 1.U)
    }

    // LED
    val ledCounterBits = log2Ceil(clkFrequency / 2 - 1)
    val ctx = RegInit(0.U(ledCounterBits.W))
    val ctxNext = Wire(Bool())
    val ledff = RegInit(false.B)
    ctxNext := ctx === (clkFrequency / 2 - 1).U
    ctx := Mux(ctxNext, 0.U, ctx + 1.U)
    when(ctxNext) {
      ledff := !ledff
    }
    led := ledff
  }
}
