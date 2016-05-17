seq(lsSettings :_*)

libraryDependencies <+= scalaBinaryVersion {
  case x if x startsWith "2.9" => "org.specs2" %% "specs2" % "1.9" % "test"
  case "2.10" =>"org.specs2" %% "specs2" % "2.2" % "test"
}

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.11.2", "0.11.3", "0.12", "0.13")

libraryDependencies <+= CrossBuilding.sbtModuleDependencyInit("scripted-plugin")

crossBuildingSettings

CrossBuilding.latestCompatibleVersionMapper ~= { mapper => version => version match {
    case "0.13" => "0.13.9"
    case x => mapper(x)
  }
}

// Replace hard coded reference to defunct typesafe artifactory repository
libraryDependencies ~= { oldDeps =>
  oldDeps.map {
    case sbtLaunch if sbtLaunch.name == "sbt-launch" =>
      sbtLaunch.copy(explicitArtifacts = sbtLaunch.explicitArtifacts.map { artifact =>
        artifact.copy(url = artifact.url.map { theUrl =>
          url(theUrl.toString.replace("http://typesafe.artifactoryonline.com", "https://dl.bintray.com"))
        })
      })
    case other => other
  }
}