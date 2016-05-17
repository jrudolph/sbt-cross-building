package sbt

import sbt.Keys._

object SbtCrossCompat {

  private type Sbt_0_13_9_ScalaArtifacts = {
    def toolDependencies(org: String, version: String): Seq[ModuleID]
  }

  private type Sbt_0_13_11_ScalaArtifacts = {
    def toolDependencies(org: String, version: String, isDotty: Boolean): Seq[ModuleID]
  }

  // 0.13.11 broke binary compatibility on ScalaArtifacts.toolDependencies (which is a private API so sbt can't be blamed).
  // We use structural types to invoke reflectively, optimistically assuming Scala 0.13.11 since it's the latest, but
  // fall back to 0.13.9 and earlier.
  private def scalaArtifactsToolDependencies(org: String, version: String): Seq[ModuleID] = {
    try {
      ScalaArtifacts.asInstanceOf[Sbt_0_13_11_ScalaArtifacts].toolDependencies(org, version, false)
    } catch {
      case _: NoSuchMethodError | _: NoSuchMethodException =>
        ScalaArtifacts.asInstanceOf[Sbt_0_13_9_ScalaArtifacts].toolDependencies(org, version)
    }
  }

  def allDependenciesSetting =
    allDependencies := {
      val base = projectDependencies.value ++ libraryDependencies.value
      val pluginAdjust = if(sbtPlugin.value) (sbtDependency in sbtPlugin).value.copy(configurations = Some(Provided.name)) +: base else base
      if(scalaHome.value.isDefined || ivyScala.value.isEmpty || !managedScalaInstance.value)
        pluginAdjust
      else
        scalaArtifactsToolDependencies(scalaOrganization.value, scalaVersion.value) ++ pluginAdjust
    }
}
