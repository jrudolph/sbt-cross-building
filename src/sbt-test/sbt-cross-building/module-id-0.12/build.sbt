sbtVersion in sbtPlugin := "0.12"

TaskKey[Unit]("check") <<= projectID map { pId =>
  val sV = pId.extraAttributes("scalaVersion")
  if (sV != "2.9.2")
    error("Wrong scala version: "+sV)
}