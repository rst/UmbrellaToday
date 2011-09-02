import sbt._

trait Defaults {
  def androidPlatformName = "android-4"
}
class Parent(info: ProjectInfo) extends ParentProject(info) {
  override def shouldCheckOutputDirectories = false
  override def updateAction = task { None }

  lazy val main  = project(".", "Umbrella Today", new MainProject(_))
  lazy val tests = project("tests",  "tests", new TestProject(_), main)

  class MainProject(info: ProjectInfo) extends AndroidProject(info) with Defaults with MarketPublish with TypedResources {
    val keyalias  = "change-me"
    val scalatest = "org.scalatest" % "scalatest" % "1.3" % "test"
    val positroniclib = "org.positronicnet" %% "positronicnetlib" % "0.3-SNAPSHOT"
  }

  class TestProject(info: ProjectInfo) extends AndroidTestProject(info) with Defaults
}
