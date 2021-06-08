package sonn

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

object getVerilogNode extends App {
  println("generate node verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/Node"),
    Seq(ChiselGeneratorAnnotation(() => new Node(false, (0, 3), 16))))
}

object getVerilogPE extends App {
  println("generate PE verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/PE"),
    Seq(ChiselGeneratorAnnotation(() => new PE( 256, 256, 256, 16, (0, 3)))))
}

object getVerilogPETop extends App {
  println("generate PETop verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/PETop"),
    Seq(ChiselGeneratorAnnotation(() => new PETop((0, 3), 16))))
}

object getVerilogPEArray extends App {
  println("generate PEArray verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/PEArray"),
    Seq(ChiselGeneratorAnnotation(() => new PEArray((3, 4), 16))))
}
