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
sbt 'testOnly sonn.PEArrayConv2Tester'
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

Now you can find *test_run_dir/PEArrayConv2* folder, use gtkwave or verdi to open *PEArrayConv2.vcd* in this folder.

Run this, to get Verilog code:

~~~bash
sbt 'test:runMain sonn.getVerilogPEArray'
~~~

You can find Verilog code in folder *test_run_dir/PEArray*.

## ToDo

- [x] 设计单个的 PE 单元，实现一维卷积运算；

- [x] PE 阵列设计；

- [x] 仿真测试；

- [ ] 总线接口设计；

- [ ] 软件驱动设计；

- [ ] 上板测试；

> more

## Details

[设计报告](paper/report.md)


