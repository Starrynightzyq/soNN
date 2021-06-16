package sonn

import chisel3._
import chisel3.util._

class PETop(w:Int=16, fifolen:Int=256) extends Module{
    val io = IO(new Bundle{
        val stateSW = Input(UInt(2.W))
        val peconfig = Flipped(DecoupledIO(new PEConfigReg(16)))
        val filter = Flipped(Decoupled(SInt(w.W)))
        val ifmap = Flipped(Decoupled(SInt(w.W)))
        val pSumIn = Flipped(DecoupledIO(SInt(w.W)))
        val pSumOut = Decoupled(SInt(w.W))
        val stateOut = Output(UInt(4.W))
    })
    val pe = Module(new PE)
    val fIn = Queue(io.filter, fifolen)
    val iIn = Queue(io.ifmap, fifolen)
    val pSumIn = Queue(io.pSumIn, fifolen)
    val oSumOut2 = Queue(pe.io.pSumOut, fifolen)

//  override def desiredName: String = position.toString()

    oSumOut2 <> io.pSumOut
    pe.io.filter <> fIn
    pe.io.ifmap <> iIn
    pe.io.peconfig <> io.peconfig
    pe.io.pSumIn <> pSumIn
    pe.io.stateSW := io.stateSW

    io.stateOut := pe.io.stateOut
}
