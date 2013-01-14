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
  val pluginSbtVersion = sbtVersion in sbtPlugin

  val crossSbtVersions = SettingKey[Seq[String]]("cross-sbt-versions", "The versions of Sbt used when cross-building an sbt plugin.")
  val forceUpdate = TaskKey[Unit]("force-update", "An uncached version of `update`")

  def settings = seq(
    crossTarget <<= (target, scalaVersion, pluginSbtVersion, sbtPlugin, crossPaths)(Defaults.makeCrossTarget),
    allDependencies <<= (projectDependencies, libraryDependencies, sbtPlugin, sbtDependency in sbtPlugin) map {
      (projDeps, libDeps, isPlugin, sbtDep) =>
        val base = projDeps ++ libDeps
        if (isPlugin) sbtDep.copy(configurations = Some(Provided.name)) +: base else base
    },
    sbtDependency in sbtPlugin <<= sbtModuleDependencyInit("sbt"),
    projectID <<= pluginProjectID,
    scalaVersion <<= (scalaVersion, sbtPlugin, pluginSbtVersion) {
      (sv, isPlugin, psbtv) =>
        if (isPlugin) scalaVersionByVersion(psbtv) else sv
    },
    crossSbtVersions <<= pluginSbtVersion (Seq(_)),
    unmanagedSourceDirectories in Compile <++= (sbtPlugin, pluginSbtVersion, sourceDirectory in Compile) {
      (isPlugin, psbtv, src) =>
        if (isPlugin) extraSourceFolders(psbtv, src) else Nil
    },

    sbtVersion in Global in sbtPlugin <<= sbtVersion(chooseDefaultSbtVersion),

    commands ++= Seq(SbtPluginCross.switchVersion, SbtPluginCross.crossBuild),

    forceUpdate <<= (ivyModule, updateConfiguration, streams) map { (module, config, streams) =>
      IvyActions.update(module, config, streams.log)
    },

    deliver <<= deliver.dependsOn(forceUpdate)
  )

  def scriptedSettings = SbtScriptedSupport.scriptedSettings

  val Version = """0\.(\d+)(?:\.(\d+))?(?:-(.*))?""".r
  def groupIdByVersion(version: String): String = version match {
    case Version("11", fix, _) if fix.toInt <= 2 =>
      "org.scala-tools.sbt"
    case Version(major, _, _) if major.toInt < 11 =>
      "org.scala-tools.sbt"
    case _ =>
      "org.scala-sbt"
  }
  def scalaVersionByVersion(version: String): String =
    byMajorVersion(version) { major =>
      if (major >= 12) "2.9.2" else "2.9.1"
    }
  def usesCrossBuilding(version: String): Boolean =
    byMajorVersion(version)(_ < 12)

  def byMajorVersion[T](version: String)(f: Int => T): T = version match {
    case Version(m, _, _) => f(m.toInt)
  }
  def currentCompatibleSbtVersion(version: String): String = version match {
    case "0.12" => "0.12.1"
    case _ => version
  }
  def chooseDefaultSbtVersion(version: String): String =
    byMajorVersion(version) { major =>
      if (major >= 12) "0."+major else version
    }

  def crossedName(name: String, version: String): String =
    if (usesCrossBuilding(version)) name + "_" + scalaVersionByVersion(version) else name

  def sbtModuleDependencyInit(moduleName: String) =
    pluginSbtVersion(sbtModuleDependency(moduleName))
  def sbtModuleDependency(moduleName: String)(version: String): ModuleID =
    groupIdByVersion(version) % crossedName(moduleName, version) % currentCompatibleSbtVersion(version)

  def extraSourceFolders(version: String, sourceFolder: File): Seq[File] = version match {
    case Version(major, minor, _) =>
      Seq(sourceFolder / ("scala-sbt-0."+major), sourceFolder / "scala-sbt-0.%s.%s".format(major, minor))
  }

  def pluginProjectID = (sbtVersion in sbtPlugin, scalaVersion, projectID, sbtPlugin) {
    (sbtV, scalaV, pid, isPlugin) =>
      if (isPlugin) Defaults.sbtPluginExtra(pid, sbtV, scalaV) else pid
  }
}
