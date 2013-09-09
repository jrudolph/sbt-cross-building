package net.virtualvoid.sbt.cross

import sbt._
import sbt.Keys._

object CrossCompat {
  def extraSettings = seq(
    Keys.scalaBinaryVersion <<= scalaVersion(CrossVersionUtil.binaryScalaVersion)
  )

  object Keys {
    val scalaBinaryVersion = SettingKey[String]("scala-binary-version", "The Scala version substring describing binary compatibility.")
  }
  object Extras

  def initializeTerminal(): Unit = jline.Terminal.getTerminal.initializeTerminal()
}
