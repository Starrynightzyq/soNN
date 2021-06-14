
// enablePlugins(PackPlugin)

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

organization := "edu.berkeley.cs"

version := "3.3.0"

name := "sonn"

scalaVersion := "2.12.10"

crossScalaVersions := Seq("2.12.10", "2.11.12")

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-language:reflectiveCalls")

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
// The following are the current "release" versions.
val defaultVersions = Seq(
  "chisel-iotesters" -> "1.4.1+"
  )

libraryDependencies ++= defaultVersions.map { case (dep, ver) =>
  "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", ver) }
libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.4.0"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.3.0" % "test"
libraryDependencies += "org.scalatest" %% "scalatest-flatspec" % "3.2.9" % "test"
libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.2" % Test
libraryDependencies += "org.easysoc" %% "layered-firrtl" % "1.1.+"
libraryDependencies += "org.scalanlp" %% "breeze" % "1.2"
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)


resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Recommendations from http://www.scalatest.org/user_guide/using_scalatest_with_sbt
logBuffered in Test := false

// Disable parallel execution when running tests.
//  Running tests in parallel on Jenkins currently fails.
parallelExecution in Test := false

scalacOptions ++= scalacOptionsVersion(scalaVersion.value)

javacOptions ++= javacOptionsVersion(scalaVersion.value)

trapExit := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q", "-a")