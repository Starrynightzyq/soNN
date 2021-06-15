package sonn

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._

@chiselName
class dataSwitch(w: Int = 16) extends Module {
  val io = IO(new Bundle {
    val dataIn = Flipped(DecoupledIO(new dataPackage(w)))
    val filter = DecoupledIO(SInt(w.W))
    val ifmap = DecoupledIO(SInt(w.W))
    val pSum = DecoupledIO(SInt(w.W))
  })
  io.filter.bits := 0.S
  io.filter.valid := 0.U
  io.ifmap.bits := 0.S
  io.ifmap.valid := 0.U
  io.pSum.bits := 0.S
  io.pSum.valid := 0.U
  io.dataIn.ready := 0.U
  when(io.dataIn.bits.dataType === 0.U) {
    io.filter.bits := io.dataIn.bits.data
    io.filter.valid := io.dataIn.valid
    io.dataIn.ready := io.filter.ready
  }.elsewhen(io.dataIn.bits.dataType === 1.U) {
    io.ifmap.bits := io.dataIn.bits.data
    io.ifmap.valid := io.dataIn.valid
    io.dataIn.ready := io.ifmap.ready
  }.otherwise {
    io.pSum.bits := io.dataIn.bits.data
    io.pSum.valid := io.dataIn.valid
    io.dataIn.ready := io.pSum.ready
  }
}