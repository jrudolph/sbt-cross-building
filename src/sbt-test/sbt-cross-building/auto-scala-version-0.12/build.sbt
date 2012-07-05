sbtVersion in sbtPlugin := "0.12"

sbtPlugin := true

TaskKey[Unit]("check") <<= scalaVersion map { sV =>
  if (sV != "2.9.2")
    error("Wrong scala version: "+sV)
}