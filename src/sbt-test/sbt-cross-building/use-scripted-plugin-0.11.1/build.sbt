sbtVersion in sbtPlugin := "0.11.1"

crossBuildingSettings

seq(CrossBuilding.scriptedSettings: _*)
