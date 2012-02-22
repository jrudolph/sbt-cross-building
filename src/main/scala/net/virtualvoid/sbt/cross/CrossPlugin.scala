package net.virtualvoid.sbt.cross

import sbt._

object CrossPlugin extends Plugin {
  override def settings = sbt.CrossBuilding.settings
}
