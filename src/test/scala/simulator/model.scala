package simulator

import breeze.linalg._

import scala.collection.mutable.ArrayBuffer

object model {
  def conv4d(
    filters: DenseMatrix[DenseMatrix[Int]],
    ifmaps: DenseMatrix[DenseMatrix[Int]],
    bias: DenseMatrix[Int],
    ichannelNum: Int,
    ochannelNum: Int,
    filterK: Int,
    ofmapW: Int,
    ofmapH: Int,
    stepW: Int,
    stepH: Int,
    activate: Boolean = false
  ): DenseMatrix[DenseMatrix[Int]] = {
    assert(filters.rows == ichannelNum) // channelIn must ==
    assert(ifmaps.rows == ichannelNum)
    assert(filters.cols == ochannelNum)
    assert(ifmaps.cols == 1)
    assert(bias.cols == 1)
    assert(bias.rows == ochannelNum)
    val result = DenseMatrix.fill(ochannelNum, 1)(DenseMatrix.fill(ofmapH, ofmapW)(0))
    // DenseMatrix.fill(ochannelNum, 1)(DenseMatrix.fill(ofmapW, ofmapH)(0))

    for (ochlcnt <- Range(0, ochannelNum)){ // 输出通道
      for (orowcnt <- Range(0, ofmapH)){    // 输出行
        for (ocolcnt <- Range(0, ofmapW)){  // 输出列
          var tmpsum = 0
          for (ichlcnt <- Range(0, ichannelNum)){ // 输入通道
            for (krowcnt <- Range(0, filterK)){   // 卷积核行
              for (kcolcnt <- Range(0, filterK)){ // 卷积核列
                val ifmap_row = orowcnt * stepH + krowcnt
                val ifmap_col = ocolcnt * stepW + kcolcnt
                val ifmapdata = ifmaps(ichlcnt, 0)(ifmap_row, ifmap_col)
                val filterdata = filters(ichlcnt, ochlcnt)(krowcnt, kcolcnt)
                tmpsum = tmpsum + ifmapdata * filterdata
              }
            }
          }
          val tmpwithbias = tmpsum + bias(ochlcnt, 0)
          if (activate && tmpwithbias < 0) {
            result(ochlcnt, 0)(orowcnt, ocolcnt) = 0
          }else{
            result(ochlcnt, 0)(orowcnt, ocolcnt) = tmpwithbias
          }
        }
      }
    }

    result
  }
}