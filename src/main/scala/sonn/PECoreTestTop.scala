package sonn

import chisel3._
import chisel3.util._

class PECoreTestTop(w:Int=16) extends Module{
    val io = IO(new Bundle{
        val stateSW = Input(UInt(2.W))
        val peconfig = Flipped(DecoupledIO(new PEConfigReg(16)))
        val filter = Flipped(Decoupled(SInt(w.W)))
        val ifmap = Flipped(Decoupled(SInt(w.W)))
        val pSumIn = Flipped(DecoupledIO(SInt(w.W)))
        val pSumOut = Decoupled(SInt(w.W))
        val stateOut = Output(UInt(4.W))
    })
    val pe = Module(new PECore(16))
    val fIn = Queue(io.filter, 256)
    val iIn = Queue(io.ifmap, 256)
    val pSumIn = Queue(io.pSumIn, 256)
    val oSumOut2 = Queue(pe.io.pSumOut, 256)

//  override def desiredName: String = position.toString()

    oSumOut2 <> io.pSumOut
    pe.io.filter <> fIn
    pe.io.ifmap <> iIn
    pe.io.regConfig := io.peconfig.bits
    io.peconfig.ready := 1.U
    pe.io.pSumIn <> pSumIn
    pe.io.stateSW := io.stateSW

    io.stateOut := pe.io.stateOut
}
