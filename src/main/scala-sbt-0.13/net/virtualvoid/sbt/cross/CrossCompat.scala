package net.virtualvoid.sbt.cross

import sbt._
import sbt.Keys._

object CrossCompat {
  def extraSettings = seq()
  object Keys
  object Extras {
    val ScopedKey = Def.ScopedKey
  }

  def initializeTerminal(): Unit = jline.TerminalFactory.get().init()
}
