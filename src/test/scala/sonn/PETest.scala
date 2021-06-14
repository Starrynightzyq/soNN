package sonn

import java.io.File

import breeze.linalg._
import breeze.linalg.{DenseMatrix, DenseVector}
import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class PETopTest(c: PETesterTop) extends PeekPokeTester(c) {
    poke(c.io.stateSW, 0)
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
    val ifmap1D: List[Int] = List(-3, -5, -5, 3, 3, 3)
    val pSum1D: List[Int] = List(1, 1, 1)
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
    poke(c.io.stateSW, 1)
    for (i <- Range(0, 200)) {
        step(1)
    }
}

class PETopTester extends ChiselFlatSpec {
  "running with --generate-vcd-output on" should "create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on", "--target-dir", "test_run_dir/PETesterTop", "--top-name", "make_Test_vcd",
        "--backend-name", "verilator"),
      () => new PETesterTop(16)
    ) {
      c => new PETopTest(c)
    } should be(true)
    new File("test_run_dir/PETesterTop/PETesterTop.vcd").exists should be(true)
  }
}