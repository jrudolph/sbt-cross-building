sbtPlugin := true

crossBuildingSettings

TaskKey[Unit]("check") <<= (sbtVersion, sbtVersion in sbtPlugin) map { (sbtVersion, sbtPluginVersion) =>
  if (sbtVersion.startsWith("0.12.0") && sbtPluginVersion != "0.12")
    error("Wrong sbt version: "+sbtPluginVersion)
}
