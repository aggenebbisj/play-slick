name := """play-slick"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc4"
//  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2"
)

val gitHeadCommitSha = settingKey[String]("Determines the current git commit SHA")
gitHeadCommitSha := Process("git rev-parse HEAD").lines.head

version in Docker := gitHeadCommitSha.value
dockerRepository := Some("aggenebbisj")
