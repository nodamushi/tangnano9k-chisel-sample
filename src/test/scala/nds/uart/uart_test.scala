package nds.uart

import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import chisel3._
import chisel3.util._

/** Test bench for UartRx. This test bench inputs UART signal with UartTx
  * module.
  */
class UartRxTB(bitWidth: Int, baudrate: Int, freq: Int) extends Module {
  val io = IO(new Bundle {
    val din = Flipped(Decoupled(UInt(bitWidth.W)))
    val dout = Valid(UInt(bitWidth.W))
  })

  val rx = Module(UartRx(bitWidth, baudrate, freq))
  val tx = Module(UartTx(bitWidth, baudrate, freq))
  tx.io.din <> io.din
  rx.io.dout <> io.dout
  rx.io.rx <> tx.io.tx
}

class UartTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Uart"

  def txTest(
      u: UartTx,
      bitWidth: Int,
      baudrate: Int,
      clkFreq: Int,
      data: Int
  ): Unit = {
    val clkDiv = clkFreq / baudrate
    // Check Idle
    u.io.tx.expect(true)
    u.io.din.ready.expect(true)

    // input data
    u.io.din.bits.poke(data.U)
    u.io.din.valid.poke(true.B)

    // Start bit
    for (div <- 0 until clkDiv) {
      u.clock.step()
      u.io.tx.expect(false, "Start Bit :" + div + " / " + clkDiv)
      u.io.din.ready.expect(false)
    }

    // output bit data
    for {
      i <- 0 until bitWidth
      div <- 0 until clkDiv
    } {
      u.clock.step()
      val e = (data >> i) & 1
      u.io.tx.expect(e == 1, "Bit " + i + ": " + div + " / " + clkDiv)
      u.io.din.ready.expect(false)
    }

    // (clear valid)
    u.io.din.valid.poke(false.B)

    // Stop bit
    for (div <- 0 until clkDiv) {
      u.clock.step()
      u.io.tx.expect(true, "Stop Bit: " + div + " / " + clkDiv)
      u.io.din.ready.expect(false)
    }

    // Idle
    u.clock.step()
    u.io.tx.expect(true)
    u.io.din.ready.expect(true)
  }

  def runTxTest(bitWidth: Int, baudrate: Int, clkFreq: Int): Unit = {
    test(UartTx(bitWidth, baudrate, clkFreq)) { u =>
      {
        val max = 1 << bitWidth
        for (data <- 0 until max) {
          txTest(u, bitWidth, baudrate, clkFreq, data)
          txTest(u, bitWidth, baudrate, clkFreq, data)
          // 特に意味はないけどIDLEを適当に入れてみる
          if (data % 3 == 0) {
            u.clock.step(10)
          }
        }
      }
    }
  }

  it should "Uart Tx(baudrate 1, clk 2)" in {
    runTxTest(8, 1, 2)
  }
  it should "Uart Tx(baudrate 11, clk 221)" in {
    runTxTest(8, 11, 221)
  }
  it should "Uart Tx(baudrate 91, clk 1221)" in {
    runTxTest(8, 91, 1221)
  }

  def rxTest(
      u: UartRxTB,
      bitWidth: Int,
      baudrate: Int,
      clkFreq: Int,
      data: Int
  ): Unit = {
    val clkDiv = clkFreq / baudrate
    // check idle
    u.io.din.ready.expect(true, "data: " + data)
    fork {
      // input
      u.io.din.bits.poke(data)
      u.io.din.valid.poke(true)
      u.clock.step(2)
      while (!u.io.din.ready.peekBoolean()) {
        u.clock.step()
      }
      u.clock.step()
      u.io.din.valid.poke(false)
      u.clock.step()
      while (!u.io.din.ready.peekBoolean()) {
        u.clock.step()
      }
    }.fork {
      u.io.dout.valid.expect(false)
      while (!u.io.dout.valid.peekBoolean()) {
        u.clock.step()
      }
      u.io.dout.bits.expect(data)
      u.clock.step()
      u.io.dout.valid.expect(false)
      while (!u.io.dout.valid.peekBoolean()) {
        u.clock.step()
      }
      u.io.dout.bits.expect(data)
      u.clock.step(10)
    }.join()
  }

  def runRxTest(bitWidth: Int, baudrate: Int, clkFreq: Int): Unit = {
    test(new UartRxTB(bitWidth, baudrate, clkFreq)) { u =>
      {
        val max = 1 << bitWidth
        for (data <- 0 until max) {
          rxTest(u, bitWidth, baudrate, clkFreq, data)
          rxTest(u, bitWidth, baudrate, clkFreq, data)
          // 特に意味はないけどIDLEを適当に入れてみる
          if (data % 3 == 0) {
            u.clock.step(10)
          }
        }
      }
    }
  }

  it should "Uart Rx(baudrate: 1, clock 2)" in {
    runRxTest(8, 1, 2)
  }
  it should "Uart Rx(baudrate: 43, clock 340)" in {
    runRxTest(8, 43, 340)
  }
  it should "Uart Rx(baudrate: 134, clock 1315)" in {
    runRxTest(8, 134, 1315)
  }
}
