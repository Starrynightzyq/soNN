package sonn

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import myutil._

class PEConfigReg(val w:Int = 16) extends Bundle{
    val ichannelNum = UInt(w.W)
    val ochannelNum = UInt(w.W)
    val ifmapW = UInt(w.W)
    val ifmapH = UInt(w.W)
    val filterK = UInt(w.W)
    val ofmapW = UInt(w.W)
    val ofmapH = UInt(w.W)
    val stepW = UInt(w.W)
    val stepH = UInt(w.W)
    val filterLen = UInt(w.W)
    val ifmapLen = UInt(w.W)
    val relu = UInt(1.W)
    val ID = UInt(w.W)
}


// take care!  in PE stateSw is buffer one time
@chiselName
class PE(w:Int = 16)
  extends Module{
    val io = IO(new Bundle {
      // 00 -> idle
      // 01 -> getData
      // 10 -> cal (calculate)
      val stateSW = Input(UInt(2.W))
      val regConfig = Flipped(DecoupledIO(new PEConfigReg(16)))

      val filter = Flipped(DecoupledIO(SInt(w.W)))
      val ifmap = Flipped(DecoupledIO(SInt(w.W)))
      val pSumIn = Flipped(DecoupledIO(SInt(w.W)))
      val pSumOut = DecoupledIO(SInt(w.W))

      val stateOut = Output(UInt(4.W))
    })

    val configReg = Reg(new PEConfigReg(16))
    val pSumSRAM = Module(new SRAM()) // 256depth 64bits
    pSumSRAM.io.rstLowas := reset.asUInt() + 1.U
    pSumSRAM.io.din := 0.S

    val idle :: cal :: add :: done:: Nil = Enum(4)
    val state = RegInit(idle)

    val calCnt = Counter(256)
    val clrCnt = Counter(256)
    val ichannelCnt = Counter(256)
    val ochannelCnt = Counter(256)

    val pSumSRAMadder = WireInit(ochannelCnt.value)
    val pSumSRAMadderReg = RegNext(pSumSRAMadder)

    val pSumInTmp = RegInit(0.S(w.W))

    val clrDone = WireInit(state === idle & clrCnt.value === configReg.ochannelNum)

    io.pSumIn.ready := 0.U
    io.filter.ready := 0.U
    io.ifmap.ready := 0.U
    io.regConfig.ready := 0.U
    io.pSumOut.valid := 0.U
    io.pSumOut.bits := 0.S

    io.stateOut := state

    // mul & add
    val pRes = WireInit(io.filter.bits * io.ifmap.bits + pSumSRAM.read(pSumSRAMadder))
    val pResReg = RegNext(pRes)

    val dataOn = WireInit(state === cal & io.filter.fire() & io.ifmap.valid === 1.U)
    val dataOnReg = RegNext(dataOn, false.B)

    // when(dataOnReg){
    //     pSumSRAM.write(pSumSRAMadderReg, pResReg)
    // }

    switch(state) {
        is(idle) {
            // clear cnt
            calCnt.value := 0.U
            // clrCnt.value := 0.U
            ichannelCnt.value := 0.U
            ochannelCnt.value := 0.U    

            io.regConfig.ready := 1.U
            when(io.regConfig.fire()) {
                configReg := io.regConfig.bits
            }
            when(io.stateSW === cal & clrDone) {
                state := cal
            }
            // clear psum ram
            when(clrCnt.value < configReg.ochannelNum){
                clrCnt.inc()
                pSumSRAM.write(clrCnt.value, 0.S)
            }
        }
        is(cal) {
            io.filter.ready := 1.U

            when(io.filter.fire() & io.ifmap.valid === 1.U) {
                pSumSRAM.write(pSumSRAMadder, pRes)

                when(calCnt.value === configReg.filterLen - 1.U) {
                    state := add
                }
                when(calCnt.value === configReg.filterLen - 1.U) {
                    calCnt.value := 0.U
                }.otherwise{
                    calCnt.inc()
                }

                when(ochannelCnt.value === configReg.ochannelNum - 1.U){
                    ochannelCnt.value := 0.U
                    io.ifmap.ready := 1.U
                    when(ichannelCnt.value === configReg.ichannelNum - 1.U){
                        ichannelCnt.value := 0.U
                    }.otherwise{
                        ichannelCnt.inc()
                    }
                }.otherwise{
                    ochannelCnt.inc()
                }
            }
        }
        is(add) {
            ochannelCnt.value := 0.U
            io.pSumIn.ready := 1.U
            when(io.pSumIn.fire()){
                // pSumInTmp := io.pSumIn.bits
                when(ochannelCnt.value === configReg.ochannelNum - 1.U){
                    ochannelCnt.value := 0.U
                    state := done
                }.otherwise{
                    ochannelCnt.inc()
                }
                io.pSumOut.bits := pSumSRAM.read(pSumSRAMadder) + io.pSumIn.bits
            }
        }
        is(done) {
            // addCnt.value := 0.U
            clrCnt.value := 0.U
            switch(io.stateSW){
                is(cal){
                    state := cal
                }
                is(idle){
                    state := idle
                }
            }
        }
    }
}
