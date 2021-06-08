package adder

import chisel3._
import chisel3.experimental._

object FullAdderSpec extends App {
  chisel3.Driver.execute(Array("--target-dir", "test_run_dir/adder"), () => new FullAdder)
}