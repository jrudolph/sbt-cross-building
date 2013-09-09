package sbt

import sbt.Keys._
import scala.Some

object SbtCrossCompat {
  def allDependenciesSetting =
    allDependencies := {
      val base = projectDependencies.value ++ libraryDependencies.value
      val pluginAdjust = if(sbtPlugin.value) (sbtDependency in sbtPlugin).value.copy(configurations = Some(Provided.name)) +: base else base
      if(scalaHome.value.isDefined || ivyScala.value.isEmpty || !managedScalaInstance.value)
        pluginAdjust
      else
        ScalaArtifacts.toolDependencies(scalaOrganization.value, scalaVersion.value) ++ pluginAdjust
    }
}
