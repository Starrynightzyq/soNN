package sonn

import java.io.File

import breeze.linalg._
import breeze.linalg.{DenseMatrix, DenseVector}
import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class PETest(c: PETestTop) extends PeekPokeTester(c) {

  val idle :: cal :: padd :: pdone :: hmove :: getdata :: alldone :: Nil = Enum(
    7
  )

    poke(c.io.stateSW, idle)
    step(1)
    poke(c.io.peconfig.valid, 1)
    poke(c.io.peconfig.bits.ichannelNum, 2)
    poke(c.io.peconfig.bits.ochannelNum, 3)
    poke(c.io.peconfig.bits.ifmapW, 5)
    poke(c.io.peconfig.bits.ifmapH, 5)
    poke(c.io.peconfig.bits.filterK, 3)
    poke(c.io.peconfig.bits.ofmapW, 3)
    poke(c.io.peconfig.bits.ofmapH, 3)
    poke(c.io.peconfig.bits.stepW, 1)
    poke(c.io.peconfig.bits.stepH, 1)
    poke(c.io.peconfig.bits.filterLen, 18)
    poke(c.io.peconfig.bits.ifmapLen, 6)
    poke(c.io.peconfig.bits.relu, 1)
    poke(c.io.peconfig.bits.ID, 1)
    step(1)
    poke(c.io.peconfig.valid, 0)
    // first: let PE in idle and input data to input FIFO
    val filter1D: List[Int] = List(-5, -5, 4, -5, 4, -2, 4, 3, -3, -1, -2, 3, 1, 0, -5, 0, 1, -3)
    val ifmap1D: List[Int] = List(-3, -5, -5, 3, 3, 3, 0, -5, -5, -3)
    val pSum1D: List[Int] = List(1, 1, 1, 2, 2, 2, 3, 3, 3)
    for (i <- filter1D.indices) {
        poke(c.io.filter.valid, 1)
        poke(c.io.filter.bits, filter1D(i))
        step(1)
    }
    poke(c.io.filter.valid, 0)

    for (i <- ifmap1D.indices) {
        poke(c.io.ifmap.valid, 1)
        poke(c.io.ifmap.bits, ifmap1D(i))
        step(1)
    }
    poke(c.io.ifmap.valid, 0)
    step(1)

    for (i <- pSum1D.indices) {
        poke(c.io.pSumIn.valid, 1)
        poke(c.io.pSumIn.bits, pSum1D(i))
        step(1)
    }
    poke(c.io.pSumIn.valid, 0)
    step(1)
    // second: let PE in getData, it will get data from FIFO
    poke(c.io.stateSW, getdata)
    step(1)
    poke(c.io.stateSW, alldone)
    step(1)
    poke(c.io.pSumOut.ready, 1)
    for (i <- Range(0, 200)) {
      if (peek(c.io.pSumOut.valid) == 1) {
        println(peek(c.io.pSumOut.bits).toString())
      }
      step(1)
    }
    // var ocnt = 0
    // println("pSumOut")
    // while (ocnt < 9) {
    //   if (peek(c.io.pSumOut.valid) == 1) {
    //     ocnt = ocnt + 1
    //     println(peek(c.io.pSumOut.bits).toString())
    //   }
    //   step(1)
    // }
    // println("done")
}

class PETester extends ChiselFlatSpec {
  "running with --generate-vcd-output on" should "create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on", "--target-dir", "test_run_dir/PE", "--top-name", "make_Test_vcd",
        "--backend-name", "verilator"),
      () => new PETestTop
    ) {
      c => new PETest(c)
    } should be(true)
    new File("test_run_dir/PE/PETestTop.vcd").exists should be(true)
  }
}