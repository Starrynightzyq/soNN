package sonn

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

object getVerilogNode extends App {
  println("generate node verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/Node"),
    Seq(ChiselGeneratorAnnotation(() => new Node(false, (0, 3), 16, 5))))
}

object getVerilogPECore extends App {
  println("generate PECore verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/PECore"),
    Seq(ChiselGeneratorAnnotation(() => new PECore(16))))
}

// object getVerilogPETesterTop extends App {
//   println("generate PETesterTop verilog")
//   (new ChiselStage).execute(
//     Array("-X", "verilog", "--target-dir", "test_run_dir/PETesterTop"),
//     Seq(ChiselGeneratorAnnotation(() => new PETesterTop(16))))
// }

object getVerilogPE extends App {
  println("generate PE verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/PE"),
    Seq(ChiselGeneratorAnnotation(() => new PE)))
}

object getVerilogPEArray extends App {
  println("generate PEArray verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/PEArray"),
    Seq(ChiselGeneratorAnnotation(() => new PEArray((3, 4), 16, 256))))
}