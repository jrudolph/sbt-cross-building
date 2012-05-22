seq(lsSettings :_*)

libraryDependencies += "org.specs2" %% "specs2" % "1.9" % "test"

seq(ScriptedPlugin.scriptedSettings: _*)

CrossBuilding.crossSbtVersions := Seq("0.11.1", "0.11.2", "0.11.3")
