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

// 生成 3行24列的矩阵
// 每个 node 带深度为 5 的 FIFO
// 每个 PE 的输入输出都带深度为 256 的 FIFO
object getVerilogPEArray extends App {
  println("generate PEArray verilog")
  (new ChiselStage).execute(
    Array("-X", "verilog", "--target-dir", "test_run_dir/PEArray"),
    Seq(ChiselGeneratorAnnotation(() => new PEArray((3, 24), 5, 16, 256))))
}