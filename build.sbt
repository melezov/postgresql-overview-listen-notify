scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc4"
, "io.spray" %% "spray-routing" % "1.3.3"
, "io.spray" %% "spray-can" % "1.3.3"
, "com.typesafe.akka" %% "akka-actor" % "2.3.14"
, "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
)
