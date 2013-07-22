package net.virtualvoid.sbt.cross

import sbt._

object CrossPlugin extends Plugin {
  def crossBuildingSettings = CrossBuilding.settings
}
