/* sbt -- Simple Build Tool
 * Copyright 2011 Artyom Olshevskiy
 *
 * Copyright 2012 Johannes Rudolph
 *
 * The settings were imported mostly from 2422df23c601089aa275b1bd04f28defb438d07f
 * and then adapted to use the new version dependent launcher.
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
  import ScriptedPlugin._

  val sbtLauncher = TaskKey[File]("sbt-launcher")
  val scriptedLaunchOpts = SettingKey[Seq[String]]("scripted-launch-opts", "options to pass to jvm launching scripted tasks")
  val scriptedRunnerModule = SettingKey[ModuleID]("scripted-runner-module", "The scripted runner to use")

  def scriptedTask: Initialize[InputTask[Unit]] = InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { args =>
    (scriptedRun, scriptedTests, sbtTestDirectory, scriptedBufferLog, args, sbtLauncher, scriptedLaunchOpts) map {
      (r, tests, testdir, bufferlog, args, launcher, launchOpts) =>
        val params =
          Seq(
            testdir, bufferlog: java.lang.Boolean,
            args.toArray[String], launcher, launchOpts.toArray[String])

        try { r.invoke(tests, params: _*) }
        catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
        finally { jline.Terminal.getTerminal.initializeTerminal() }
    }
  }

  def sbtLaunchUrl(version: String) =
    "http://typesafe.artifactoryonline.com/typesafe/ivy-releases/%s/sbt-launch/%s/sbt-launch.jar" format (groupIdByVersion(version), version)

  val scriptedSettings = seq(
    ivyConfigurations += scriptedConf,
    scriptedSbt <<= pluginSbtVersion(CrossBuilding.currentCompatibleSbtVersion),
    scalaVersion in scripted := "2.10.2", // TODO: infer from resolved module
    scriptedRunnerModule := "org.scala-sbt" % "scripted-sbt" % "0.13.0-RC3" % scriptedConf.toString,
    libraryDependencies <++= (scriptedSbt, scriptedRunnerModule) { (version, scriptedRunnerModule) =>
      Seq(
        scriptedRunnerModule,
        groupIdByVersion(version) % "sbt-launch" % version % scriptedConf.toString from sbtLaunchUrl(version)
      )
    },
    resolvers += Resolver.url("Typesafe repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns),
    sbtTestDirectory <<= sourceDirectory / "sbt-test",
    scriptedBufferLog := true,
    scriptedClasspath <<= (classpathTypes, update) map { (ct, report) => PathFinder(Classpaths.managedJars(scriptedConf, ct, report).map(_.data)) },
    scriptedTests <<= scriptedTestsTask,
    scriptedRun <<= scriptedRunTask,
    scriptedDependencies <<= (compile in Test, publishLocal) map { (analysis, pub) => Unit },
    scripted <<= scriptedTask,
    scriptedLaunchOpts := Seq(),
    scalaInstance in scripted <<= (appConfiguration, scalaVersion in scripted).map((app, version) =>
      ScalaInstance(version, app.provider.scalaProvider.launcher)
    ),

    sbtLauncher <<= (update in scriptedConf) map { updateReport =>
      val mr = updateReport.configuration(scriptedConf.toString).get.modules.find(_.module.name == "sbt-launch").get
      mr.artifacts.head._2
    }
  )

  def scriptedRunTask: Initialize[Task[Method]] = (scriptedTests, scriptedSbt) map { (m, version) =>
    val paramTypes =
      Seq(classOf[File], classOf[Boolean], classOf[Array[String]], classOf[File], classOf[Array[String]])

    m.getClass.getMethod("run", paramTypes: _*)
  }

  def scriptedTestsTask: Initialize[Task[AnyRef]] = (scriptedClasspath, scalaInstance in scripted) map {
    (classpath, scala) =>
      val loader = ClasspathUtilities.toLoader(classpath, scala.loader)
      ModuleUtilities.getObject("sbt.test.ScriptedTests", loader)
  }
}
