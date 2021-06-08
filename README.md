soNN
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
sbt 'test:runMain sonn.NodeTester'
~~~

You will get output like this:

~~~
[info] Updated file /home/EDA/vsim/chisel-demo/sonn/test_run_dir/Node/project/build.properties: set sbt.version to 1.5.3
[info] welcome to sbt 1.5.3 (Private Build Java 1.8.0_292)
[info] loading project definition from /home/EDA/vsim/chisel-demo/sonn/test_run_dir/Node/project
[info] set current project to node (in build file:/home/EDA/vsim/chisel-demo/sonn/test_run_dir/Node/)
[info] Passed: Total 0, Failed 0, Errors 0, Passed 0
[info] No tests to run for Test / testOnly
[success] Total time: 0 s, completed Jun 8, 2021 3:22:59 PM
~~~

Now you can find *test_run_dir/Node* folder, use gtkwave or verdi to open *Node.vcd* in this folder.

Run this, to get Verilog code:

~~~bash
sbt 'test:runMain sonn.getVerilogPEArray'
~~~

You can find Verilog code in folder *test_run_dir/Node*.