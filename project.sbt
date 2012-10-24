sbtPlugin := true

name := "sbt-cross-building"

organization := "net.virtual-void"

version := "0.7.0"

homepage := Some(url("http://github.com/jrudolph/sbt-cross-building"))

licenses in GlobalScope += "BSD 2-Clause License" -> url("http://www.opensource.org/licenses/BSD-2-Clause")

(LsKeys.tags in LsKeys.lsync) := Seq("sbt-plugin", "sbt", "cross", "build")

(LsKeys.docsUrl in LsKeys.lsync) <<= homepage

(description in LsKeys.lsync) :=
  "An sbt plugin which allows to cross build sbt plugins without having to change the sbt version of the build of the plugin itself."
