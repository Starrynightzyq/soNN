package sonn

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import myutil._



class PEConfigReg(val w:Int = 16) extends Bundle{
  val filterNum = UInt(w.W)
  val singleFilterLen = UInt(w.W)
  val imgNum = UInt(w.W)
  val singleImgLen = UInt(w.W)
  val nchannel = UInt(w.W)
  val relu = UInt(1.W)
}


// take care!  in PE stateSw is buffer one time
@chiselName
class PE(filterSpadLen: Int = 225, imgSpadLen: Int = 225, pSumMemLen: Int = 256, w:Int = 16, position: (Int, Int)=(0,0))
  extends Module{
    val io = IO(new Bundle {
      // 00 -> idle
      // 01 -> getData
      // 10 -> cal
      val stateSW = Input(UInt(2.W))
      val regConfig = Input(new PEConfigReg(16))

      val filter = Flipped(DecoupledIO(SInt(w.W)))
      val img = Flipped(DecoupledIO(SInt(w.W)))
      val pSumIn = Flipped(DecoupledIO(SInt(w.W)))
//      val oSumMEM = DecoupledIO(SInt(w.W))
      val oSumSRAM = DecoupledIO(SInt(w.W))

      val stateOut = Output(UInt(4.W))
    })

//  override def desiredName: String = position.toString()
  val x = WireInit(position._1.U(8.W))
  val y = WireInit(position._2.U(8.W))
  core.dontTouch(x) // makes sure that a particular signal is not removed by the “Dead Code Elimination” pass in FIRRTL
  core.dontTouch(y)
  val configReg = Reg(new PEConfigReg(16))

  io.pSumIn.ready := 0.U
//  io.oSumMEM.valid := 0.U
//  io.oSumMEM.bits := 0.S

  val fCnt = Counter(256)             // input filter total length
  val iCnt = Counter(256)             // input img total length
  val calCnt = Counter(256)           // calculate times
  val fCalCnt = Counter(256)          // f shift times
  val iCalCnt = Counter(256)          // i shift times
  val cCalCnt = Counter(256)          // channel shift times
  val pDoneCnt = Counter(256)         // in pDont state times
  val newImgCnt = Counter(256)         // in pDont state times
  val pSumAddr = Counter(256)         // for addr
  val zfc = WireInit(fCnt.value)
  val zic = WireInit(iCnt.value)
  val zcc = WireInit(calCnt.value)
  val zfcc = WireInit(fCalCnt.value)
  val zccc = WireInit(cCalCnt.value)
  val zpc = WireInit(pDoneCnt.value)
  val zpsa = WireInit(pSumAddr.value)
  val znic = WireInit(newImgCnt.value)
  core.dontTouch(zfc)
  core.dontTouch(zic)
  core.dontTouch(zcc)
  core.dontTouch(zfcc)
  core.dontTouch(zccc)
  core.dontTouch(zpc)
  core.dontTouch(zpsa)
  core.dontTouch(znic)
  // data mean getData
  val idle :: data :: cal :: pDone :: newImg :: allDone:: Nil = Enum(6)
  val state = RegInit(idle)
  io.stateOut := state
  val dodata = WireInit(state === data)
  val docal = WireInit(state === cal)


  val fQMuxIn = Wire(DecoupledIO(io.filter.bits.cloneType))
  val fQ = FIFO(fQMuxIn, filterSpadLen)
  io.filter.ready := 0.U
  fQ.ready := 0.U
  // when getdata switch io.statwSW == cal and filter must all translate to fQ
  when(dodata) {
    fQMuxIn <> io.filter
  }.otherwise{
    fQMuxIn <> fQ
  }
  fQ.ready := state === cal

  val iQMuxIn = Wire(DecoupledIO(io.img.bits.cloneType))
  val iQ = FIFO(iQMuxIn, imgSpadLen)
  val iQreg = Reg(iQ.bits.cloneType)
  io.img.ready := 0.U
  iQ.ready := 0.U
  when(dodata){
    iQMuxIn <> io.img
    when(iCnt.value === configReg.singleFilterLen * configReg.nchannel){
      iQMuxIn.valid := 0.U
      io.img.ready := 0.U
    }
  }.otherwise{
    iQMuxIn <> iQ
  }
  iQ.ready := (state === cal) & (fCalCnt.value === 0.U)

//  val pSumMem = Mem(pSumMemLen, SInt(w.W))
  val pSumSRAM = Module(new SRAM()) // 256 16bits
  pSumSRAM.io.rstLowas := reset.asUInt() + 1.U
  pSumSRAM.io.din := 0.S
  io.oSumSRAM.bits := 0.S

  val trash = RegInit(0.S(w.W))

//  io.oSumMEM.valid := 0.U
  io.oSumSRAM.valid := 0.U
  core.dontTouch(io.oSumSRAM)

  val normal :: multiFilter :: multiChannel :: Nil = Enum(3)
  val mode = Wire(UInt(8.W))
  mode := multiFilter
//  when(configReg.nchannel =/= 1.U){
//    mode := multiFilter//multiChannel
//  }.elsewhen(configReg.filterNum =/= 1.U){
//    mode := multiFilter
//  }.otherwise{
//    mode := normal
//  }

  val addr = Wire(UInt(log2Ceil(pSumMemLen).W))
  val singleResultLen = configReg.singleImgLen - configReg.singleFilterLen + 1.U
  addr := fCalCnt.value
//  when(mode === normal){
//    addr := pSumAddr.value
//  }.elsewhen(mode === multiFilter){
//    addr := /*pSumAddr.value + */  fCalCnt.value // * singleResultLen
//  }.elsewhen(mode === multiChannel){
//    addr := pSumAddr.value
//  }

//  val pResultCurrect = WireInit(fQ.bits * iQ.bits + pSumMem(addr))
//  val pResultLast = WireInit(fQ.bits * iQreg + pSumMem(addr))

  // mul $ add
  val pResultCurrentSRAM = WireInit(fQ.bits * iQ.bits + pSumSRAM.read(addr))
  val pResultLastSRAM = WireInit(fQ.bits * iQreg + pSumSRAM.read(addr))

  val needNewImg = RegInit(0.U(1.W))

  switch (state){
    is(idle){
      configReg := io.regConfig
      when(io.stateSW === data){
        state := data
      }
    }
    is(data){
      switch(mode){
        is(normal){
        }
        is(multiFilter){
//          io.oSumMEM.valid := 0.U
          io.oSumSRAM.valid := 0.U
          when(fQMuxIn.fire()){ // fire = ready && valid
            fCnt.inc() // Increments the counter by a step.
          }
          when(iQMuxIn.fire()){
            iCnt.inc()
          }
          when(io.stateSW === cal){
            when(iCnt.value === 0.U | fCnt.value === 0.U){
              state := allDone
            }.otherwise{
              state := cal
            }
          }
        }
        is(multiChannel){
        }
      }
    }
    is(cal){
      switch(mode){
        is(normal){
        }

        is(multiFilter){
          iQMuxIn.valid := 0.U

//          io.oSumMEM.bits := 0.S
          io.oSumSRAM.bits := 0.S
          when(   (calCnt.value === configReg.singleFilterLen * configReg.nchannel)) {
//            io.oSumMEM.valid := 1.U
//            io.oSumMEM.bits := pResultLast

            io.oSumSRAM.valid := 1.U
            io.oSumSRAM.bits := pResultLastSRAM
            // io.oSum.bits := fQ.bits * iQreg + pSumMem(addr)
            //                           /*^*/
          }.elsewhen((calCnt.value ===   /*|*/ configReg.singleFilterLen * configReg.nchannel - 1.U) &
                                                                    (fCalCnt.value === 0.U)){ // 0 mean the last
//            io.oSumMEM.valid := 1.U         /*v*/   // when the first result it use iQ.bits, else will use iQreg
//            io.oSumMEM.bits := pResultCurrect

            io.oSumSRAM.valid := 1.U
            io.oSumSRAM.bits := pResultCurrentSRAM
            // io.oSum.bits := fQ.bits * iQ.bits + pSumMem(addr)
          }

          when(fQ.fire() & iQ.fire()){
            iQMuxIn.valid := 1.U
//            pSumMem(addr) := pResultCurrect
            pSumSRAM.write(addr, pResultCurrentSRAM)
            iQreg := iQ.bits
            fCalCnt.inc()   // when configReg.fNum = 1 error!!! so change to next
            when((configReg.filterNum === 1.U) & fCalCnt.value === 0.U){
              fCalCnt.value := 0.U
            }.otherwise{
              fCalCnt.inc()
            }

            calCnt.inc()
            when((configReg.filterNum===1.U) & (calCnt.value === configReg.singleFilterLen * configReg.nchannel - 1.U) &
              (fCalCnt.value === 0.U)){
              calCnt.value := 0.U
              pSumAddr.inc()
              state := pDone
            }

          }.elsewhen(fQ.fire()){
//            pSumMem(addr) := pResultLast
            pSumSRAM.write(addr, pResultLastSRAM)
            when(fCalCnt.value === configReg.filterNum - 1.U){
              fCalCnt.value := 0.U
            }.otherwise{
              fCalCnt.inc()
            }

//            calCnt.inc()
            when((calCnt.value === configReg.singleFilterLen * configReg.nchannel) &
                                                                (fCalCnt.value === configReg.filterNum - 1.U)){
              calCnt.value := 0.U
              pSumAddr.inc()
              state := pDone
            }
          } .otherwise{
            state := allDone
          }
        }

        is(multiChannel){
        }
      }
    }
    is(pDone){
      for(i <- Range(0, pSumMemLen)){
//        pSumMem(i) := 0.S
      }
      pSumSRAM.io.rstLowas := 0.U
      switch(mode){
        is(normal){
        }
        is(multiFilter){
          fQMuxIn.valid := 0.U
          // let img FIFO spit a data to trash
          iQ.ready := 1.U
          when(iQ.fire()){
            trash := iQ.bits
          }
          core.dontTouch(trash)
          iQMuxIn <> io.img
          pDoneCnt.inc()
          when((io.img.valid === 0.U) /*| (pSumAddr.value === configReg.singleImgLen - iCnt.value + 1.U)*/){
            state := allDone
          }.elsewhen((needNewImg === 1.U) | (configReg.singleImgLen === configReg.singleFilterLen)){
            state := newImg
            pDoneCnt.value := 0.U
          }.elsewhen(pDoneCnt.value === configReg.nchannel -1.U){
            state := cal
            pDoneCnt.value := 0.U
            when(iCalCnt.value === configReg.singleImgLen - configReg.singleFilterLen - 1.U){
              iCalCnt.value := 0.U
              needNewImg := 1.U
            }.otherwise{
              iCalCnt.inc()
              needNewImg := 0.U
            }
          }.otherwise{
            state := pDone
          }
        }
        is(multiChannel){
        }
      }
    }
    is(newImg){
      when(iCnt.value === 1.U){
        state := cal
        needNewImg := 0.U
        iQ.ready := 0.U
        fQMuxIn.valid := 0.U
        iQMuxIn.valid := 0.U
      }.otherwise {
        needNewImg := 0.U
        fQMuxIn.valid := 0.U
        // let img FIFO spit a data to trash
        iQ.ready := 1.U
        when(iQ.fire()) {
          trash := iQ.bits
        }
        core.dontTouch(trash)
        iQMuxIn <> io.img
        newImgCnt.inc()
        when(io.img.valid === 0.U) {
          state := allDone
        }.elsewhen(newImgCnt.value === iCnt.value - 2.U) {
          newImgCnt.value := 0.U
          state := cal
        }
      }
    }
    is(allDone){
      // then don't read data
      fQMuxIn.ready := 0.U
      iQMuxIn.ready := 0.U
//      io.oSumMEM.valid := 0.U
      io.oSumSRAM.valid := 0.U
    }
  }


}
