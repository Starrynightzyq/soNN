package sonn

import java.io.File
import scala.util._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class NodeTest2(dut: Node) extends PeekPokeTester(dut) {

    val r = scala.util.Random
    val rstart = -100
    val rend   = 100

    poke(dut.io.dataPackageIn.valid, 1)
    poke(dut.io.dataPackageOut.ready, 1)
    for (i <- 0 until 16) {
        for (j <- 0 until 16) {
            poke(dut.io.dataPackageIn.bits.data, (rstart + r.nextInt(rend-rstart+1)).S)
            poke(dut.io.dataPackageIn.bits.dataType, 1.U)
            poke(dut.io.dataPackageIn.bits.positon.row, i.U)
            poke(dut.io.dataPackageIn.bits.positon.col, j.U)
            step(1)
        }
    }
}

class NodeTester extends ChiselFlatSpec {
  "running with --generate-vcd-output on" should "create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on", "--target-dir", "test_run_dir/Node", "--top-name", "make_Test_vcd",
        "--backend-name", "verilator"),
      () => new Node(false, (0, 3), 16, 5)
    ) {
      c => new NodeTest2(c)
    } should be(true)
    new File("test_run_dir/Node/Node.vcd").exists should be(true)
  }
}
