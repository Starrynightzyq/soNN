# soNN
=======================

A Reconﬁgurable Accelerator for Deep Convolutional Neural Networks implemented by [chisel3](https://www.chisel-lang.org/).

## How to get started

### Dependencies

#### JDK 8 or newer

We recommend LTS releases Java 8 and Java 11. You can install the JDK as recommended by your operating system, or use the prebuilt binaries from [AdoptOpenJDK](https://adoptopenjdk.net/).

#### SBT or mill

SBT is the most common built tool in the Scala community. You can download it [here](https://www.scala-sbt.org/download.html).  
mill is another Scala/Java build tool without obscure DSL like SBT. You can download it [here](https://github.com/com-lihaoyi/mill/releases)

### Usage

Run this, to check your environmrnt:

~~~bash
sbt 'testOnly sonn.PEArrayConv1Tester'
~~~

You will get output like this:

~~~
[info] [14.390] ===============ERROR: 0======================
Enabling waves..
Exit Code: 0
[info] [14.394] RAN 7096 CYCLES PASSED
[info] - should create a vcd file from your test
[info] Run completed in 1 minute, 20 seconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 81 s (01:21), completed Jun 19, 2021 3:43:08 PM
~~~

Now you can find *test_run_dir/PEArrayConv1* folder, use gtkwave or verdi to open *PEArrayConv1.vcd* in this folder.

Run this, to get Verilog code:

~~~bash
sbt 'test:runMain sonn.getVerilogPEArray'
~~~

You can find Verilog code in folder *test_run_dir/Node*.

## ToDo

- [x] 设计单个的 PE 单元，实现一维卷积运算；

- [x] PE 阵列设计；

- [x] 仿真测试；

- [ ] 总线接口设计；

- [ ] 软件驱动设计；

- [ ] 上板测试；

> more

## 引言

## 原理介绍

### 卷积运算

### 脉动阵列

### 适用于 PE 单元的一维卷积

### Chisel 介绍

## 设计结构

### 广播结构设计

### PE 单元结构设计

单个的 PE 计算单元需要实现一维卷积运算，也就是说单个的 PE 计算单元可以实现输入图像中的一行数据（包含所有的通道）与卷积核中的一行数据进行卷积运算，最终可以得到输出图像中的一行数据（包含所有通道）。卷积的过程可以用下图来表示：

![一维卷积运算](pic/conv1d.png)

上图是卷积核宽度 (col) 为 3，输入图像宽度为 5 的一维卷积运算的过程。可以看到卷积核和输入图像的数据会在计算中反复使用到，每计算一个输出数据，使用的卷积核的数据是相同的，而使用的输入图像的数据会每次移动一个步长，因此在设计 PE 单元的时候需要设计一个**循环 buffer** 的结构用来暂存数据。同时考虑到输入通道和输出通道通常都不是单通道，因此 PE 单元中需要一块 RAM 用来存放每个输出通道的部分和。最终设计的 PE 单元结构如下图所示。

![PE 单元结构图](pic/PE_sch.svg)

PE 单元的设计分为 3 层，最里层 *PE Core* 的逻辑可以实现单个输出数据的计算，其中 *Paratial Sum* 是用来暂存部分和的 RAM，*PE Core* 包含一个 16bit 的乘法器和一个 64bit 的加法器，用以实现乘加运算。*PE Core* 的输入除了图像数据 (ifmaps) 和权重数据 (filters) 之外，还有一个部分和 (psum) 的输入接口，这样可以将多个 PE 单元串联起来，实现多行数据的累加。在数据位宽的设计上，输入图像数据和权重数据都是 16bit，输入输出的部分和是 64bit，用来避免多次累加后数据溢出。同时，权重数据和输入的图像数据在使用之前应该先进行软件仿真，选择合适的量化位宽，以避免数据溢出。

中间层 *PE* 实现了前一小节提到的循环 buffer 的结构，其中权重数据的 buffer (filters loop buffer) 实现比较简单，只需要在 *PE Core* 计算时，将 buffer 的输出送到 buffer 的输入。输入图像数据的 Buffer (ifmaps loop buffer) 结构和权重数据的 buffer 类似，但需要考虑到卷积水平方向上的步长 (stepW)，每次卷积核滑动，需要丢弃掉 stepW 个数据，同时从外部再获取 stepW 个新数据，这个过程通过图中的 *if trash* 逻辑来控制。

最外层的 *PE Top* 相当于在中间层的基础上在输入加了 FIFO，用来缓存循环 Buffer 暂时没有使用到的数据。

> buffer 深度的计算

### 脉动阵列结构设计

在将单个的 PE 单元组合成 PE 阵列时，第一行 PE 单元的三种输入数据 (ifmaps, filters, psum) 都由 *Node* 提供，后面的每一行的 ifmaps 和 filters 由 *Node* 提供，而 psum 输入来自前一行 PE 的 psum 输出，最后一行 PE 的 psum 输出是整个阵列的输出。下图是一个 3 行 4 列的阵列结构。

![脉动整列结构](pic/array.svg)

## 测试与调试

## 改进方向



