package sonn

import java.io.File

import breeze.linalg._
import breeze.linalg.{DenseMatrix, DenseVector}
import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import simulator._

class PEArrayTest(c: PEArrayTestTop) extends PeekPokeTester(c) {

  val idle :: cal :: padd :: pdone :: hmove :: getdata :: alldone :: Nil = Enum(
    7
  )

  val ichannelNum = 2
  val ochannelNum = 3
  val ifmapW = 5
  val ifmapH = 5
  val filterK = 3
  val stepW = 1
  val stepH = 1
  val ofmapW = (ifmapW - filterK) / stepW + 1
  val ofmapH = (ifmapH - filterK) / stepH + 1
  val filterLen = filterK * ichannelNum * ochannelNum
  val ifmapLen = filterK * ichannelNum
  val relu = 1
  val ID = 1

  val filters = DenseMatrix.fill(ichannelNum, ochannelNum)(
    SW.randomMatrix((filterK, filterK))
  )
  val ifmaps = DenseMatrix.fill(ichannelNum, 1)(SW.randomMatrix(ifmapW, ifmapH))
  val bias = SW.randomMatrix(ochannelNum, 1)
  // val bias = DenseMatrix.fill(ochannelNum, 1)(0)


  // 行为级仿真
  val sw = model.conv4d(filters, ifmaps, bias, ichannelNum, ochannelNum, filterK, ofmapW, ofmapH, stepW, stepH, (relu==1))

  println("filters: ")
  filters.map((x) => {
    println(x.toString())
    println()
  })
  println("ifmaps: ")
  ifmaps.map((x) => {
    println(x.toString())
    println()
  })

  // output
  c.io.oSumOut.foreach((x) => {
    poke(x.ready, 1)
  })
  step(1)

  // config
  poke(c.io.stateSW, idle)
  step(1)
  poke(c.io.peconfig.valid, 1)
  poke(c.io.peconfig.bits.ichannelNum, ichannelNum)
  poke(c.io.peconfig.bits.ochannelNum, ochannelNum)
  poke(c.io.peconfig.bits.ifmapW, ifmapW)
  poke(c.io.peconfig.bits.ifmapH, ifmapH)
  poke(c.io.peconfig.bits.filterK, filterK)
  poke(c.io.peconfig.bits.ofmapW, ofmapW)
  poke(c.io.peconfig.bits.ofmapH, ofmapH)
  poke(c.io.peconfig.bits.stepW, stepW)
  poke(c.io.peconfig.bits.stepH, stepH)
  poke(c.io.peconfig.bits.filterLen, filterLen)
  poke(c.io.peconfig.bits.ifmapLen, ifmapLen)
  poke(c.io.peconfig.bits.relu, relu)
  poke(c.io.peconfig.bits.ID, ID)
  step(1)
  poke(c.io.peconfig.valid, 0)
  step(1)

  poke(c.io.stateSW, alldone)
  step(1)

  // filters
  for (col <- Range(0, filterK)) {
    for (ic <- Range(0, ichannelNum)) {
      for (oc <- Range(0, ochannelNum)) {
        for (row <- Range(0, filterK)) {
          assert(peek(c.io.dataIn.ready) == 1)
          val ftmp = filters(ic, oc)(row, col)
          poke(c.io.dataIn.valid, 1.U)
          poke(c.io.dataIn.bits.data, ftmp)
          poke(c.io.dataIn.bits.dataType, 0.U)
          poke(c.io.dataIn.bits.positon.row, row)
          poke(c.io.dataIn.bits.positon.col, -1)
          step(1)
        }
      }
    }
  }
  poke(c.io.dataIn.valid, 0.U)
  step(1)

  // ifmaps
  for (col <- Range(0, ifmapW)) {
    for (ic <- Range(0, ichannelNum)) {
      for (arrayrow <- Range(0, filterK)) {
        for (arraycol <- Range(0, ofmapH)) {
          assert(peek(c.io.dataIn.ready) == 1)
          val itmp = ifmaps(ic, 0)(arrayrow+stepH*arraycol, col)

          poke(c.io.dataIn.valid, 1.U)
          poke(c.io.dataIn.bits.data, itmp)
          poke(c.io.dataIn.bits.dataType, 1.U)
          poke(c.io.dataIn.bits.positon.row, arrayrow)
          poke(c.io.dataIn.bits.positon.col, arraycol+1)
          step(1)
        }
      }
    }
  }
  poke(c.io.dataIn.valid, 0.U)
  step(1)

  // bias
  // for (arraycol <- Range(0, ofmapH)) {
  for (ocol <- Range(0, ofmapW)) {
    for (oc <- Range(0, ochannelNum)) {
      assert(peek(c.io.dataIn.ready) == 1)
      poke(c.io.dataIn.valid, 1.U)
      // poke(c.io.dataIn.bits.data, 0.S)
      poke(c.io.dataIn.bits.data, bias(oc, 0))
      poke(c.io.dataIn.bits.dataType, 2.U)
      poke(c.io.dataIn.bits.positon.row, 0)
      poke(c.io.dataIn.bits.positon.col, -1)
      step(1)
    }
  }
  // }
  poke(c.io.dataIn.valid, 0.U)
  step(1)

  println("ofmaps:")
  var ofmaps = DenseMatrix.fill(ochannelNum, 1)(DenseMatrix.fill(ofmapH, ofmapW)(0))
  var ocnt = 0
  var colcnt = 0
  var error = 0
  while (peek(c.io.done) == 0) {
    if(peek(c.io.oSumOut(0).valid) == 1){
      for (i <- c.io.oSumOut.indices) {
        var otmp = 0
        otmp = peek(c.io.oSumOut(i).bits).toInt
        // println(s"i = ${i}, o = ${otmp}")

        ofmaps(ocnt,0)(i,colcnt) = otmp

        expect(c.io.oSumOut(i).bits, sw(ocnt,0)(i,colcnt))
        if (otmp != sw(ocnt,0)(i,colcnt)) {
          error += 1
        }

      }
      if(ocnt == ochannelNum-1){
        ocnt = 0
        colcnt = colcnt + 1
      }else{
        ocnt = ocnt + 1
      }
    }
    step(1)
  }
  // println(s"ocnt = ${ocnt}, colcnt = ${colcnt}")
  println(ofmaps.toString())
  println()
  println("behavioral model ofmaps:")
  println(sw.toString())
  println()
  println(s"===============ERROR: ${error}======================")
}

class PEArrayTester extends ChiselFlatSpec {
  "running with --generate-vcd-output on" should "create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array(
        "--generate-vcd-output",
        "on",
        "--target-dir",
        "test_run_dir/PEArray",
        "--top-name",
        "make_Test_vcd",
        "--backend-name",
        "verilator"
      ),
      () => new PEArrayTestTop((3, 3), 16)
    ) { c =>
      new PEArrayTest(c)
    } should be(true)
    new File("test_run_dir/PEArray/PEArrayTestTop.vcd").exists should be(true)
  }
}
