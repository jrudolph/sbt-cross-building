sbtPlugin := true

name := "sbt-cross-building"

organization := "net.virtual-void"

version := "0.5.0-SNAPSHOT"

homepage := Some(url("http://github.com/jrudolph/sbt-cross-building"))

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/jrudolph/sbt-dependency-graph/raw/master/LICENSE")

//(LsKeys.tags in LsKeys.lsync) := Seq("sbt-plugin", "sbt", "cross", "build")

//(LsKeys.docsUrl in LsKeys.lsync) <<= homepage

//(description in LsKeys.lsync) :=
//  "An sbt plugin which allows to cross build sbt plugins without having to change the sbt version of the build of the plugin itself."
