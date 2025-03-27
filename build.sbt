val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "TestScalaJS",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    // for an application with a main method
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0", 
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19"
  )
