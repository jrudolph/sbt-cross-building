sbtVersion in sbtPlugin := "0.12"

sbtPlugin := true

crossBuildingSettings

TaskKey[Unit]("check") <<= projectID map { pId =>
  val sV = pId.extraAttributes("e:scalaVersion")
  if (sV != "2.9.2")
    error("Wrong scala version: "+sV)
}
