package sonn

import chisel3._
import chisel3.util._
import myutil._

class PE(
  filterSpadLen: Int = 225,
  imgSpadLen: Int = 225,
  pSumMemLen: Int = 256,
  w: Int = 16
) extends Module {
  val io = IO(new Bundle {
    val stateSW = Input(UInt(2.W))
    val peconfig = Flipped(DecoupledIO(new PEConfigReg(16)))
    val filter = Flipped(Decoupled(SInt(w.W)))
    val ifmap = Flipped(Decoupled(SInt(w.W)))
    val pSumIn = Flipped(DecoupledIO(SInt((4 * w).W)))
    val pSumOut = Decoupled(SInt((4 * w).W))
    val stateOut = Output(UInt(4.W))
  })

  val iQMuxIn = Wire(DecoupledIO(io.ifmap.bits.cloneType))
  val fQMuxIn = Wire(DecoupledIO(io.filter.bits.cloneType))

  val pecore = Module(new PECore(w))
  val fQIn = Queue(fQMuxIn, filterSpadLen)
  val iQIn = Queue(iQMuxIn, imgSpadLen)
  val pSumQIn = Queue(io.pSumIn, pSumMemLen)
  val oSumQOut = Queue(pecore.io.pSumOut, pSumMemLen)

//  override def desiredName: String = position.toString()
  val idle :: cal :: padd :: pdone :: hmove :: getdata :: alldone :: Nil = Enum(
    7
  )

  val state = RegInit(idle)
  val pstate = WireInit(pecore.io.stateOut)

  val configReg = Reg(new PEConfigReg(16))
  val pstateSWReg = RegInit(idle)

  // Cnt
  val colCnt = Counter(256)
  val trashCnt = Counter(16)
  val getdataCnt = Counter(256)

  // trash to consume the data in the FIFO Consume the data in the FIFO when moving the convolution kernel horizontally
  val trash = RegInit(0.S(w.W))
  // val iQMuxOut = Wire(DecoupledIO(io.ifmap.cloneType))

  // iQMuxOut.valid := 0.U
  // iQMuxOut.bits := 0.U

  io.peconfig.ready := 0.U
  io.ifmap.ready := 0.U

  oSumQOut <> io.pSumOut //
  // pe io
  pecore.io.filter <> fQIn //
  // pecore.io.ifmap <> iQMuxOut      // ok
  pecore.io.regConfig := configReg // ok
  pecore.io.pSumIn <> pSumQIn //
  pecore.io.stateSW := pstateSWReg // ok

  io.stateOut := state

  // iQIn FIFO input
  when(pstate === cal || state === cal) {
    // iQMuxIn <> pecore.io.ifmap
    iQMuxIn.bits := pecore.io.ifmap.bits
    iQMuxIn.valid := pecore.io.ifmap.fire()
    io.ifmap.ready := 0.U
  }.otherwise {
    // iQMuxIn <> io.ifmap
    // io.ifmap.ready := iQMuxIn.ready
    iQMuxIn.bits := io.ifmap.bits
    iQMuxIn.valid := io.ifmap.valid
    when(state === getdata || state === hmove) {
      io.ifmap.ready := iQMuxIn.ready
    }.otherwise {
      io.ifmap.ready := 0.U
    }
  }

  when(pstate === cal) {
    fQMuxIn.bits := pecore.io.filter.bits
    fQMuxIn.valid := pecore.io.filter.fire()
    io.filter.ready := 0.U
  }.otherwise {
    fQMuxIn <> io.filter
  }

  // iQIn FIFO output
  when(state === hmove) {
    iQIn.ready := 1.U
    pecore.io.ifmap.bits := 0.S
    pecore.io.ifmap.valid := 0.U
  }.otherwise {
    // iQIn.ready := pecore.io.ifmap.ready
    // pecore.io.ifmap.bits := iQIn.bits
    // pecore.io.ifmap.valid := iQIn.valid
    pecore.io.ifmap <> iQIn
  }

  pecore.io.filter <> fQIn

  switch(state) {
    is(idle) {
      io.peconfig.ready := 1.U
      when(io.peconfig.fire()) {
        configReg := io.peconfig.bits
        state := getdata
      }
      // when(pecore.io.clrDoneOut === 1.U & io.stateSW === cal){
      //     state := cal
      //     pstateSWReg := cal
      // }
    }
    is(getdata) {
      when(io.ifmap.fire()) {
        when(getdataCnt.value >= configReg.ifmapLen - 1.U) {
          state := cal
          getdataCnt.value := 0.U
          pstateSWReg := cal
        }.otherwise {
          getdataCnt.inc()
        }
      }
    }
    is(cal) {
      // iQMuxOut <> iQIn
      pstateSWReg := cal
      when(pstate === pdone) {
        trashCnt.value := 0.U
        pstateSWReg := idle
        when(colCnt.value >= configReg.ofmapW - 1.U) {
          state := alldone
        }.otherwise {
          state := hmove
          colCnt.inc()
        }
      }
    }
    is(padd) {
      state := idle
    }
    is(pdone) {
      state := idle
    }
    is(hmove) {
      // when hmove, pe should idle
      pstateSWReg := idle

      when(io.ifmap.fire()) {
        when(
          trashCnt.value >= (configReg.stepH * configReg.ichannelNum) - 1.U
        ) {
          state := cal
        }.otherwise {
          trashCnt.inc()
        }
      }
    }
    is(alldone) {
      pstateSWReg := idle
      when(io.stateSW === idle){
        state := idle
      }.elsewhen(io.stateSW === getdata){
        state := getdata
      }
      // state := idle
    }
  }
}
