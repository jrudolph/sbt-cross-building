seq(lsSettings :_*)

libraryDependencies += "org.specs2" %% "specs2" % "1.9" % "test"

CrossBuilding.scriptedSettings

CrossBuilding.crossSbtVersions := Seq("0.11.2", "0.11.3", "0.12")

libraryDependencies <+= CrossBuilding.sbtModuleDependencyInit("scripted-plugin")
