package sonn

import java.io.File
import scala.util._
import org.scalatest._
import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.experimental.BundleLiterals._
import chisel3.tester._
import chisel3.tester.RawTester.test


// class NodeTest(dut: Node) extends PeekPokeTester(dut) {
//     dut.io.dataPackageIn.initSource().setSourceClock(dut.clock)
//     dut.io.dataPackageOut.initSink().setSinkClock(dut.clock)

//     val r = scala.util.Random
//     val rstart = -100
//     val rend   = 100
//     val testPositon = for { x <- 1 to 10; y <- 1 to 10} yield (x, y)
//     val inputSeq = testPositon.map { case (x, y) => 
//         (new dataPackage(16)).Lit(_.data -> (rstart + r.nextInt(rend-rstart+1)).S, _.dataType -> 0.U, _.positon._1 -> x.S, _.positon._2 -> y.S)
//     }

//     dut.dataPackageIn.enqueueSeq(inputSeq)
//     // fork {
//     //     dut.dataPackageIn.enqueueSeq(inputSeq)
//     // }.fork {
//     //     dut.dataPackageOut.expectDequeueSeq(inputSeq)
//     // }.join()
// }

// class NodeTester extends ChiselFlatSpec {
//   "running with --generate-vcd-output on" should "create a vcd file from your test" in {
//     iotesters.Driver.execute(
//       Array("--generate-vcd-output", "on", "--target-dir", "test_run_dir/Node", "--top-name", "make_Test_vcd",
//         "--backend-name", "verilator"),
//       () => new Node(false, (0, 3), 16)
//     ) {
//       c => new NodeTest(c)
//     } should be(true)
//     new File("test_run_dir/Node/Node.vcd").exists should be(true)
//   }
// }

class NodeTest extends FlatSpec with ChiselScalatestTester with Matchers {
    test(new Node(false, (0, 3), 16)) { dut =>
        dut.io.dataPackageIn.initSource().setSourceClock(dut.clock)
        dut.io.dataPackageOut.initSink().setSinkClock(dut.clock)

        val r = scala.util.Random
        val rstart = -100
        val rend   = 100
        val testPositon = for { x <- 0 to 10; y <- 0 to 10} yield (x, y)
        val inputSeq = testPositon.map { case (x, y) => 
            (new dataPackage(16)).Lit(_.data -> (rstart + r.nextInt(rend-rstart+1)).S, _.dataType -> 0.U, _.positon.row -> x.S, _.positon.col -> y.S)
        }

        val resPosition = for { x <- 0 to 0; y <- 3 to 3} yield (x, y)
        val resSeq = resPosition.map { case (x, y) => 
            (new dataPackage(16)).Lit(_.data -> (rstart + r.nextInt(rend-rstart+1)).S, _.dataType -> 0.U, _.positon.row -> x.S, _.positon.col -> y.S)
        }

        dut.io.dataPackageIn.enqueueSeq(inputSeq)

        // fork {
        //     dut.io.dataPackageIn.enqueueSeq(inputSeq)
        // }.fork {
        //     dut.io.dataPackageOut.expectDequeueSeq(resSeq)
        // }.join()

    }
}
