package nds.uart

import chisel3._
import chisel3.util._

object Uart {
  final val START_BIT = 0
  final val STOP_BIT = 1
}

object UartTx {
  def apply(bitWidth: Int, baudrate: Int, clkFrequency: Int) = {
    if (bitWidth <= 0) {
      throw new IllegalArgumentException("Invalid BitWidth: " + bitWidth)
    }
    if (baudrate <= 0) {
      throw new IllegalArgumentException("Invalid Baudrate: " + baudrate)
    }
    if (clkFrequency <= 0) {
      throw new IllegalArgumentException(
        "Invalid Clock Frequency: " + clkFrequency
      )
    }
    if (clkFrequency < baudrate) {
      throw new IllegalArgumentException(
        "Clock Frequency < Baud Rate: " + clkFrequency + " < " + baudrate
      )
    }
    val div = clkFrequency / baudrate
    new UartTx(bitWidth, div)
  }
}

object UartRx {
  def apply(bitWidth: Int, baudrate: Int, clkFrequency: Int) = {
    if (bitWidth <= 0) {
      throw new IllegalArgumentException("Invalid BitWidth: " + bitWidth)
    }
    if (baudrate <= 0) {
      throw new IllegalArgumentException("Invalid Baudrate: " + baudrate)
    }
    if (clkFrequency <= 0) {
      throw new IllegalArgumentException(
        "Invalid Clock Frequency: " + clkFrequency
      )
    }
    if (clkFrequency < baudrate) {
      throw new IllegalArgumentException(
        "Clock Frequency < Baud Rate: " + clkFrequency + " < " + baudrate
      )
    }
    val div = clkFrequency / baudrate
    new UartRx(bitWidth, div)
  }
}



/** constant baudrate UART Tx module
  * @param bitWidth
  *   UART output data bit width
  * @param div
  *   clock divide
  */
class UartTx(bitWidth: Int, div: Int) extends Module {

  val io = IO(new Bundle {
    val din = Flipped(Decoupled(UInt(bitWidth.W)))
    val tx = Output(Bool())
  })

  // Stop Bit(IDLE) + Start Bit + data(8bit)
  private val states = 2 + bitWidth
  private val stateBits = log2Ceil(states)
  private val dataBits = states - 1
  private val idle = 0.U(stateBits.W)

  private val divBits = log2Ceil(div)

  // FF
  val clkCounter = RegInit(0.U(divBits.W))
  val state = RegInit(idle)
  val data = RegInit(Uart.STOP_BIT.U(dataBits.W))

  // assign io
  io.din.ready := state === idle
  io.tx := data(0)

  // capture din
  when(io.din.ready && io.din.valid) {
    data := Cat(Uart.STOP_BIT.U, io.din.bits, Uart.START_BIT.U)
    clkCounter := (div - 1).U
    state := states.U
  }

  // update counter, data
  when(state =/= idle) {
    when(clkCounter === 0.U) {
      clkCounter := (div - 1).U
      state := state - 1.U
      // bit shift
      data := Cat(Uart.STOP_BIT.U, data(dataBits - 1, 1))
    }.otherwise {
      clkCounter := clkCounter - 1.U
    }
  }
}

/** constant baudrate UART Rx module
  * @param bitWidth
  *   UART output data bit width
  * @param div
  *   clock divide
  */
class UartRx(bitWidth: Int, div: Int) extends Module {

  val io = IO(new Bundle {
    val dout = Valid(UInt(bitWidth.W))
    val rx = Input(Bool())
  })

  // IDLE -[StartBit]->data(0)->....->data(last)->STOP BIT->IDLE
  private val states = bitWidth + 1
  private val stateBits = log2Ceil(states)
  private val idle = 0.U(stateBits.W)
  private val stateStopBit = 1.U(stateBits.W)

  private val divBit = log2Ceil(div)

  // FF
  val clkCounter = RegInit(0.U(divBit.W))
  val state = RegInit(idle)
  val data = Reg(UInt(bitWidth.W))
  val valid = RegInit(false.B)

  // assign
  io.dout.bits := data
  io.dout.valid := valid

  // start capture
  when(state === idle && io.rx === Uart.START_BIT.U) {
    clkCounter := (div - 1).U
    state := states.U
  }

  // update counter, data
  when(state =/= idle) {
    when(clkCounter === 0.U) {
      clkCounter := (div - 1).U
      when(state === stateStopBit + 1.U) {
        valid := true.B
      }
      when(state =/= stateStopBit) {
        data := Cat(io.rx, data(bitWidth - 1, 1))
      }
      state := state - 1.U
    }.otherwise {
      clkCounter := clkCounter - 1.U
    }
  }

  // down valid
  when(valid) {
    valid := false.B
  }

}
