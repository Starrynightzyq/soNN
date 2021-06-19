package sonn

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._

@chiselName
class PEArray(val shape: (Int, Int), w: Int = 16, nodefifolen: Int = 5, pefifolen: Int = 256)
    extends Module {
  val io = IO(new Bundle {
    val dataIn = Flipped(DecoupledIO(new dataPackage(w).cloneType))
    val stateSW = Input(UInt(2.W))
    val peconfig = Flipped(DecoupledIO(new PEConfigReg(16)))
    val oSumOut = Vec(shape._2, DecoupledIO(SInt((4 * w).W)))
    val done = Output(UInt(1.W))
  })
  val doneReg = RegInit(0.U(1.W))
  io.done := doneReg

  val configReg = Reg(new PEConfigReg(16))
  when(io.peconfig.fire()) {
    configReg := io.peconfig.bits
  }

  val NoC = List[List[Node]]().toBuffer
  val pes = List[List[PETop]]().toBuffer
  val dss = List[List[dataSwitch]]().toBuffer

  for (i <- Range(0, shape._1)) {
    val tempNoC = List[Node]().toBuffer
    val tempPE = List[PETop]().toBuffer
    val tempds = List[dataSwitch]().toBuffer
    for (j <- Range(0, shape._2 + 1)) { // 第 0 列是列广播器
      val node = Module(new Node(j == 0, (i, j), w, nodefifolen))
      if (j != 0) {
        val pe = Module(new PETop(w, pefifolen))
        val ds = Module(new dataSwitch())
        ds.io.dataIn <> node.io.dataPackageOut
        pe.io.filter <> ds.io.filter
        pe.io.ifmap <> ds.io.ifmap
        // pe.io.peconfig <> io.peconfig
        pe.io.peconfig.bits := io.peconfig.bits
        pe.io.peconfig.valid := io.peconfig.valid
        pe.io.stateSW := io.stateSW
        if (i == 0) {
          pe.io.pSumIn <> ds.io.pSum
        } else {
          ds.io.pSum.ready := 0.U
        }
        if (i == shape._1 - 1) {
          // io.oSumOut(j-1) <> pe.io.pSumOut
          io.oSumOut(j-1).valid := pe.io.pSumOut.valid
          pe.io.pSumOut.ready := io.oSumOut(j-1).ready
          // relu
          when(configReg.relu === 1.U & pe.io.pSumOut.bits < 0.S) {
            io.oSumOut(j-1).bits := 0.S
          }.otherwise{
            io.oSumOut(j-1).bits := pe.io.pSumOut.bits
          }
        }
        tempPE.append(pe)
        tempds.append(ds)
      }
      tempNoC.append(node)
    }
    NoC.append(tempNoC.toList)
    pes.append(tempPE.toList)
    dss.append(tempds.toList)
  }

  // NoC valid and bits
  for (i <- Range(0, shape._1)) {
    for (j <- Range(1, shape._2 + 1)) {
      NoC(i)(j).io.dataPackageIn.valid := NoC(i).head.io.dataPackageOut.valid
      NoC(i)(j).io.dataPackageIn.bits := NoC(i).head.io.dataPackageOut.bits
    }
  }

  // NoC ready
  for (i <- NoC) {
    i.head.io.dataPackageOut.ready := i.tail
      .map(_.io.dataPackageIn.ready)
      .reduce(_ | _)
    i.head.io.dataPackageIn.valid := io.dataIn.valid
    i.head.io.dataPackageIn.bits := io.dataIn.bits
  }
  io.dataIn.ready := NoC.map(_.head.io.dataPackageIn.ready).reduce(_ | _)


  // pSumIn
  for (i <- Range(1, shape._1)) {
    for (j <- Range(0, shape._2)) {
      pes(i)(j).io.pSumIn <> pes(i - 1)(j).io.pSumOut
    }
  }

  // done reg
  val idle :: cal :: padd :: pdone :: hmove :: getdata :: alldone :: Nil = Enum(
    7
  )

  // doneReg := pes.map(_.map(_.io.stateOut === alldone)).flatten.reduce(_ | _)
  doneReg := pes(shape._1-1).map(_.io.stateOut === alldone).reduce(_ | _)

  // peconfig ready
  io.peconfig.ready := pes.map(_.map(_.io.peconfig.ready)).flatten.reduce(_ | _)

}
