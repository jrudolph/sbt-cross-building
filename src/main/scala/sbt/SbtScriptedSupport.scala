/* sbt -- Simple Build Tool
 * Copyright 2011 Artyom Olshevskiy
 *
 * Copyright 2012 Johannes Rudolph
 *
 * The plugin was imported fully from the xsbt sources revision
 * 2422df23c601089aa275b1bd04f28defb438d07f
 *
 * The main adaptation is to use the correct launcher for the sbt version
 * to test against.
 */
package sbt

import classpath.ClasspathUtilities
import Keys._
import java.lang.reflect.Method
import Project.Initialize

object SbtScriptedSupport {
  import CrossBuilding._

  def scriptedConf = config("scripted-sbt") hide

  val scriptedSbt = SettingKey[String]("scripted-sbt")
  val sbtLauncher = TaskKey[File]("sbt-launcher")
  val sbtTestDirectory = SettingKey[File]("sbt-test-directory")
  val scriptedBufferLog = SettingKey[Boolean]("scripted-buffer-log")
  final case class ScriptedScalas(build: String, versions: String)
  val scriptedScalas = SettingKey[ScriptedScalas]("scripted-scalas")

  val scriptedClasspath = TaskKey[PathFinder]("scripted-classpath")
  val scriptedTests = TaskKey[AnyRef]("scripted-tests")
  val scriptedRun = TaskKey[Method]("scripted-run")
  val scriptedDependencies = TaskKey[Unit]("scripted-dependencies")
  val scripted = InputKey[Unit]("scripted")

  def scriptedTestsTask: Initialize[Task[AnyRef]] = (scriptedClasspath, scalaInstance) map {
    (classpath, scala) =>
      val loader = ClasspathUtilities.toLoader(classpath, scala.loader)
      ModuleUtilities.getObject("sbt.test.ScriptedTests", loader)
  }

  def scriptedRunTask: Initialize[Task[Method]] = scriptedTests map {
    m => m.getClass.getMethod("run", classOf[File], classOf[Boolean], classOf[String], classOf[String], classOf[String], classOf[Array[String]], classOf[File])
  }

  def scriptedTask: Initialize[InputTask[Unit]] = InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
    (scriptedDependencies, scriptedTests, scriptedRun, sbtTestDirectory, scriptedBufferLog, scriptedSbt, scriptedScalas, sbtLauncher, result) map {
      (deps, m, r, testdir, bufferlog, version, scriptedScalas, launcher, args) =>
        try { r.invoke(m, testdir, bufferlog: java.lang.Boolean, version.toString, scriptedScalas.build, scriptedScalas.versions, args.toArray, launcher) }
        catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
    }
  }

  def sbtLaunchUrl(version: String) =
    "http://typesafe.artifactoryonline.com/typesafe/ivy-releases/%s/sbt-launch/%s/sbt-launch.jar" format (groupIdByVersion(version), version)

  val scriptedSettings = seq(
    ivyConfigurations += scriptedConf,
    scriptedSbt <<= pluginSbtVersion,
    scriptedScalas <<= (scalaVersion) { (scala) => ScriptedScalas(scala, scala) },
    libraryDependencies <++= (scriptedScalas, scriptedSbt) { (scalas, version) =>
      Seq(
        groupIdByVersion(version) % ("scripted-sbt_" + scalas.build) % version % scriptedConf.toString,
        groupIdByVersion(version) % "sbt-launch" % version % scriptedConf.toString from sbtLaunchUrl(version)
      )
    },
    resolvers += Resolver.url("Typesafe repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns),
    //sbtLauncher <<= (appConfiguration)(app => IO.classLocationFile(app.provider.scalaProvider.launcher.getClass)),
    sbtTestDirectory <<= sourceDirectory / "sbt-test",
    scriptedBufferLog := true,
    scriptedClasspath <<= (classpathTypes, update) map { (ct, report) => PathFinder(Classpaths.managedJars(scriptedConf, ct, report).map(_.data)) },
    scriptedTests <<= scriptedTestsTask,
    scriptedRun <<= scriptedRunTask,
    scriptedDependencies <<= (compile in Test, publishLocal) map { (analysis, pub) => Unit },
    scripted <<= scriptedTask,

    sbtLauncher <<= (update in scriptedConf) map { updateReport =>
      val mr = updateReport.configuration(scriptedConf.toString).get.modules.find(_.module.name == "sbt-launch").get
      mr.artifacts.head._2
    }
  )
}
