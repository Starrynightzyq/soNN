package sonn

import chisel3._
import chisel3.util._

class PEArrayTestTop(val shape: (Int, Int), w: Int=16, fifolen: Int = 256) extends Module{
  val io = IO(new Bundle{
    val dataIn = Flipped(DecoupledIO(new dataPackage(w).cloneType))
    val stateSW = Input(UInt(2.W))
    val peconfig = Flipped(DecoupledIO(new PEConfigReg(16)))
    val oSumOut = Vec(shape._2, DecoupledIO(SInt((4 * w).W)))
    val done = Output(UInt(1.W))
  })

  val pearray = Module(new PEArray((shape._1, shape._2), w, 5, fifolen))

  val dInQ = Queue(io.dataIn, 81920)

  pearray.io.dataIn <> dInQ
  pearray.io.stateSW <> io.stateSW
  pearray.io.peconfig <> io.peconfig
  io.oSumOut <> pearray.io.oSumOut
  io.done := pearray.io.done
}