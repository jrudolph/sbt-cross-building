/* sbt -- Simple Build Tool
 * Copyright 2011, 2012 Mark Harrah, Johannes Rudolph
 *
 * I copied and adapted this from xsbt/main/Defaults.scala
 */

package sbt

import Keys._
import net.virtualvoid.sbt.cross.CrossCompat
import CrossCompat.Keys._

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
  val latestCompatibleVersionMapper =
    SettingKey[String => String](
      "cross-latest-compatible-version-mapper",
      "A mapping from binary compatible version to the full version to use for building")

  def settings = seq(
    crossTarget <<= (target, scalaBinaryVersion, pluginSbtVersion, sbtPlugin, crossPaths)(Defaults.makeCrossTarget),
    SbtCrossCompat.allDependenciesSetting,
    sbtDependency in sbtPlugin <<= sbtModuleDependencyInit("sbt"),
    projectID <<= pluginProjectID,
    scalaVersion <<= (scalaVersion, sbtPlugin, pluginSbtVersion) {
      (sv, isPlugin, psbtv) =>
        if (isPlugin) scalaVersionByVersion(psbtv) else sv
    },
    crossSbtVersions in Global <<= pluginSbtVersion (Seq(_)),
    unmanagedSourceDirectories in Compile <++= (sourceDirectory in Compile, sbtPlugin, pluginSbtVersion) {
      (src, isPlugin, psbtv) =>
        if (isPlugin) extraSourceFolders(psbtv, src) else Nil
    },

    sbtVersion in Global in sbtPlugin <<= sbtVersion(chooseDefaultSbtVersion),

    commands ++= Seq(SbtPluginCross.switchVersion, SbtPluginCross.crossBuild),

    forceUpdate <<= (ivyModule, updateConfiguration, streams) map { (module, config, streams) =>
      IvyActions.update(module, config, streams.log)
    },

    latestCompatibleVersionMapper := currentCompatibleSbtVersion,

    deliver <<= deliver.dependsOn(forceUpdate),
    deliverLocal <<= deliverLocal.dependsOn(forceUpdate)
  ) ++ CrossCompat.extraSettings

  def scriptedSettings = SbtScriptedSupport.scriptedSettings

  val Version = """(\d+)\.(\d+)(?:\.(\d+))?(?:-(.*))?""".r
  def groupIdByVersion(version: String): String = version match {
    case Version("0", "11", fix, _) if fix.toInt <= 2 => "org.scala-tools.sbt"
    case Version("0", major, _, _) if major.toInt < 11 => "org.scala-tools.sbt"
    case _ => "org.scala-sbt"
  }
  def scalaVersionByVersion(version: String): String =
    byMajorVersion(version) {
      case (0, 11) => "2.9.1"
      case (0, 12) => "2.9.2"
      case (0, 13) => "2.10.5"
      case (1, _) => "2.11.8"
    }
  def usesCrossBuilding(version: String): Boolean =
    byMajorVersion(version)((epoch, major) => epoch == 0 && major < 12)

  def byMajorVersion[T](version: String)(f: (Int, Int) => T): T = version match {
    case Version(e, m, _, _) => f(e.toInt, m.toInt)
    case _ => throw new IllegalArgumentException("Illegal sbt version: '%s'" format version)
  }
  def currentCompatibleSbtVersion(version: String): String = version match {
    case "0.12" => "0.12.4"
    case "0.13" => "0.13.11"
    case "1.0" => "1.0.0-M4"
    case _ => version
  }
  def chooseDefaultSbtVersion(version: String): String =
    byMajorVersion(version) { (epoch, major) =>
      if (epoch > 0 || major >= 12) epoch + "." + major else version
    }

  def crossedName(name: String, version: String): String =
    if (usesCrossBuilding(version)) name + "_" + scalaVersionByVersion(version) else name

  def sbtModuleDependencyInit(moduleName: String) =
    (pluginSbtVersion, latestCompatibleVersionMapper)(sbtModuleDependency(moduleName))
  def sbtModuleDependency(moduleName: String)(version: String, compatibleVersion: String => String): ModuleID =
    groupIdByVersion(version) % crossedName(moduleName, version) % compatibleVersion(version)

  def extraSourceFolders(version: String, sourceFolder: File): Seq[File] = version match {
    case Version(epoch, major, minor, _) =>
      Seq(sourceFolder / ("scala-sbt-" + epoch + "." + major)) ++
        Option(minor).map(min => sourceFolder / "scala-sbt-%s.%s.%s".format(epoch, major, min)).toSeq
  }

  def pluginProjectID = (sbtVersion in sbtPlugin, scalaBinaryVersion, projectID, sbtPlugin) {
    (sbtV, scalaV, pid, isPlugin) =>
      if (isPlugin) Defaults.sbtPluginExtra(pid, sbtV, scalaV) else pid
  }
}
