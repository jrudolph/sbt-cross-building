package sbt

import org.specs2.mutable.Specification

class TestCrossBuilding extends Specification {
  val ScalaTools = "org.scala-tools.sbt"
  val ScalaSbt = "org.scala-sbt"

  "Check that correct group id is selected" in {
    "for an old version" in {
      CrossBuilding.groupIdByVersion("0.10.0") must be_==(ScalaTools)
    }
    "0.11.2" in {
      CrossBuilding.groupIdByVersion("0.11.2") must be_==(ScalaTools)
    }
    "0.11.3" in {
      CrossBuilding.groupIdByVersion("0.11.3") must be_==(ScalaSbt)
    }
    "0.12" in {
      "0.12.0" in {
        CrossBuilding.groupIdByVersion("0.12.0") must be_==(ScalaSbt)
      }
      "0.12.0-Beta2" in {
        CrossBuilding.groupIdByVersion("0.12.0-Beta2") must be_==(ScalaSbt)
      }
    }
    "0.13.0" in {
      CrossBuilding.groupIdByVersion("0.13.0") must be_==(ScalaSbt)
    }
  }
}
