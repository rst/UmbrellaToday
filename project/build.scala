import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    version := "0.1",
    scalaVersion := "2.9.0-1",
    platformName in Android := "android-7"
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies ++= Seq(
        "org.scalatest"     %% "scalatest"        % "1.6.1"        % "test",
        "org.positronicnet" %% "positronicnetlib" % "0.4-SNAPSHOT"
      ))
}

object AndroidBuild extends Build {
  lazy val proj = Project (
    "Umbrella Today",
    file("."),
    settings = General.fullAndroidSettings )
}
