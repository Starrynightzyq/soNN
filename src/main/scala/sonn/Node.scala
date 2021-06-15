package sonn

import chisel3._
import chisel3.util._

class Positon(val w: Int) extends Bundle {
  val row = SInt(w.W)
  val col = SInt(w.W)
}

class dataPackage(val w: Int) extends Bundle {
  val data = SInt(w.W)
  val dataType = UInt(2.W)
  val positon = new Positon(8)
}

class Node(row: Boolean, positon: (Int, Int), w: Int, fifolen: Int) extends Module {
  val io = IO(new Bundle {
    val dataPackageIn = Flipped(DecoupledIO(new dataPackage(w)))
    val dataPackageOut = DecoupledIO(new dataPackage(w))
  })

  val x = WireInit(positon._1.asUInt(8.W)) // 带有初值的 wire
  val y = WireInit(positon._2.asUInt(8.W))
  core.dontTouch(x)
  core.dontTouch(y)

  // dataPackageIn 经过一个深度为 fifolen 的 fifo 到达 dataPackageOut
  val qIn = Wire(io.dataPackageIn.cloneType)
  val q = Queue(qIn, fifolen)

  qIn <> io.dataPackageIn
  q <> io.dataPackageOut

  val boardcast = Wire(Bool())

  if (row) {
    boardcast := (io.dataPackageIn.bits.positon.row === (-1).S)
    io.dataPackageIn.ready := qIn.ready &
      (boardcast | (io.dataPackageIn.bits.positon.row === positon._1.S))
    qIn.valid := io.dataPackageIn.valid &
      ((io.dataPackageIn.bits.positon.row === (-1).S) | (io.dataPackageIn.bits.positon.row === positon._1.S))
  } else {
    boardcast := (io.dataPackageIn.bits.positon.col === (-1).S)
    io.dataPackageIn.ready := qIn.ready &
      (boardcast | (io.dataPackageIn.bits.positon.col === positon._2.S))
    qIn.valid := io.dataPackageIn.valid &
      (boardcast | (io.dataPackageIn.bits.positon.col === positon._2.S))
  }

}
