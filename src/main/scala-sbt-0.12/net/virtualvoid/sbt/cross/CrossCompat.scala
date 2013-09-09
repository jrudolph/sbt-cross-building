package net.virtualvoid.sbt.cross

import sbt._
import sbt.Keys._

object CrossCompat {
  def extraSettings = seq()
  object Keys
  object Extras

  def initializeTerminal(): Unit = jline.Terminal.getTerminal.initializeTerminal()
}
