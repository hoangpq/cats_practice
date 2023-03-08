import sbt._
import Keys._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info")

val circeVersion = "0.14.1"
lazy val root = (project in file("."))
  .settings(
    name := "untitled",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.8",
      "io.estatico" %% "newtype" % "0.4.4",
      "eu.timepit" %% "refined" % "0.10.1",
      "dev.optics" %% "monocle-core" % "3.1.0",
      "dev.optics" %% "monocle-macro" % "3.1.0",
      "tf.tofu" %% "derevo-core" % "0.12.0"
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion),
  )
