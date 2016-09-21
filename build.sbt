lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging, DockerPlugin)
name := "akka-http-docker"

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

mainClass in (Compile, run) := Some("Boot")

daemonUser.in(Docker) := "root"
maintainer.in(Docker) := "Simun Romic"
version.in(Docker)    := "latest"
dockerBaseImage       := "java:8"
dockerExposedPorts    := Vector(2552, 8080)
dockerRepository      := Some("sromic")

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9-RC2",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.9-RC2",
  "com.typesafe.akka" % "akka-stream_2.11" % "2.4.10",
  "com.typesafe.akka" % "akka-persistence-cassandra_2.11" % "0.18"
)

unmanagedResourceDirectories in Compile += {
  baseDirectory.value / "src/main/resources"
}