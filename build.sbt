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
    case "0.13" => "0.13.2"
    case x => mapper(x)
  }
}