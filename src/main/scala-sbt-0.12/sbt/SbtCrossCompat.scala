package sbt

import sbt.Keys._
import scala.Some

object SbtCrossCompat {
  def allDependenciesSetting =
    allDependencies <<= (projectDependencies, libraryDependencies, sbtPlugin, sbtDependency in sbtPlugin) map {
      (projDeps, libDeps, isPlugin, sbtDep) =>
        val base = projDeps ++ libDeps
        if (isPlugin) sbtDep.copy(configurations = Some(Provided.name)) +: base else base
    }
}
