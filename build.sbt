lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, DockerPlugin)
name := "AkkaHttpDemo"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-Xfuture",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused"
)

packageName in Docker := "akka-http-docker"
dockerExposedPorts := Seq(8000)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9-RC2",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.9-RC2"
)

unmanagedResourceDirectories in Compile += {
  baseDirectory.value / "src/main/resources"
}