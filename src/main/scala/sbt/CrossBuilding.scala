/* sbt -- Simple Build Tool
 * Copyright 2011, 2012 Mark Harrah, Johannes Rudolph
 *
 * I copied and adapted this from xsbt/main/Defaults.scala
 */

package sbt

import Keys._

/**
 * The idea here is to be able to define a "sbtVersion in sbtPlugin" which
 * directs the dependencies of the plugin to build to the specified sbt plugin
 * version.
 *
 * More work is needed to make that work properly for sbt >= 0.12.
 */
object CrossBuilding {

  def settings = seq(
    crossTarget <<= (target, scalaVersion, sbtVersion in sbtPlugin, sbtPlugin, crossPaths)(Defaults.makeCrossTarget),
    allDependencies <<= (projectDependencies, libraryDependencies, sbtPlugin, sbtDependency in sbtPlugin) map {
      (projDeps, libDeps, isPlugin, sbtDep) =>
        val base = projDeps ++ libDeps
        if (isPlugin) sbtDep.copy(configurations = Some(Provided.name)) +: base else base
    },
    sbtDependency in sbtPlugin <<= (appConfiguration, sbtVersion in sbtPlugin)(sbtDependencyForVersion),
    projectID <<= pluginProjectID
  )

  def sbtDependencyForVersion(app: xsbti.AppConfiguration, version: String): ModuleID = {
    val id = app.provider.id
    val (groupId, cross) =
      if (version startsWith "0.12")
        ("org.scala-sbt", false)
      else
        (id.groupID, true)

    val base = ModuleID(groupId, id.name, version, crossVersion = cross)
    IvySbt.substituteCross(base, app.provider.scalaProvider.version).copy(crossVersion = false)
  }

  def pluginProjectID = (sbtVersion in sbtPlugin, scalaVersion, projectID, sbtPlugin) {
    (sbtV, scalaV, pid, isPlugin) =>
      if (isPlugin) Defaults.sbtPluginExtra(pid, sbtV, scalaV) else pid
  }
}
