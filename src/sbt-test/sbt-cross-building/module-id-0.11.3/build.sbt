sbtVersion in sbtPlugin := "0.11.3"

sbtPlugin := true

TaskKey[Unit]("check") <<= projectID map { pId =>
  println(pId)
  val sV = pId.extraAttributes("e:scalaVersion")
  if (sV != "2.9.1")
    error("Wrong scala version: "+sV)
}