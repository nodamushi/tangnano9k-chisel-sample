package nds

import nds.uart.UartTxSampleTop
import nds.util._
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.StandardCharsets
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File
import chisel3.stage.ChiselStage

object UartTopRTLGenerator extends App {
  val dir = if (args.length == 0) { "build/chisel" }
  else { args(0) }
  val stage = new ChiselStage
  stage.emitVerilog(
    new UartTxSampleTop("💩💩💩", 27.MHz, 115200),
    Array(
      "-td=" + dir,
      "--emission-options=disableMemRandomization,disableRegisterRandomization"
    )
  )

}
