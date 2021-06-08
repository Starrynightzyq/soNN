package myutil

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.experimental.BundleLiterals._
import chisel3.tester._
import chisel3.tester.RawTester.test


// class FIFOTest extends FlatSpec with ChiselScalatestTester with Matchers {
//     test(FIFO) { dut =>
//         dut.io.dataPackageIn.initSource().setSourceClock(dut.clock)
//         dut.io.dataPackageOut.initSink().setSinkClock(dut.clock)

//         val r = scala.util.Random
//         val rstart = -100
//         val rend   = 100
//         val testPositon = for { x <- 0 to 10; y <- 0 to 10} yield (x, y)
//         // val inputSeq = testPositon.map { case (x, y) => 
//         //     (new dataPackage(16)).Lit(_.data -> (rstart + r.nextInt(rend-rstart+1)).S, _.dataType -> 0.U, _.positon.row -> x.S, _.positon.col -> y.S)
//         // }
//         val testVector = Seq.tabulate(300){ i => i.U }

//         dut.io.dataPackageIn.enqueueSeq(testVector)

//         // fork {
//         //     dut.io.dataPackageIn.enqueueSeq(inputSeq)
//         // }.fork {
//         //     dut.io.dataPackageOut.expectDequeueSeq(resSeq)
//         // }.join()

//     }
// }
