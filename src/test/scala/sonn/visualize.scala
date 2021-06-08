// get ELK Graph from https://rtsys.informatik.uni-kiel.de/elklive/elkgraph.html

package sonn

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}
import layered.stage.ElkStage

object visualizeNode extends App {
  val targetDir = "test_run_dir/Node"
  (new ElkStage).execute(
    Array("-td", targetDir),
    Seq(ChiselGeneratorAnnotation(() => new Node(false, (0, 3), 16)))
  )
}

object visualizePE extends App {
  val targetDir = "test_run_dir/PE"
  (new ElkStage).execute(
    Array("-td", targetDir),
    Seq(ChiselGeneratorAnnotation(() => new PE( 256, 256, 256, 16, (0, 3))))
  )
}

object visualizePETop extends App {
  val targetDir = "test_run_dir/PETop"
  (new ElkStage).execute(
    Array("-td", targetDir),
    Seq(ChiselGeneratorAnnotation(() => new PETop((0, 3), 16)))
  )
}

object visualizePEArray extends App {
  val targetDir = "test_run_dir/PEArray"
  (new ElkStage).execute(
    Array("-td", targetDir),
    Seq(ChiselGeneratorAnnotation(() => new PEArray((3, 4), 16)))
  )
}