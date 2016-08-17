lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging)
name := "AkkaHttpDemo"

version := "1.0"

scalaVersion := "2.11.8"

packageName in Docker := "akka-http-docker"
dockerExposedPorts := Seq(5000)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9-RC2",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.9-RC2"
)

unmanagedResourceDirectories in Compile += {
  baseDirectory.value / "src/main/resources"
}