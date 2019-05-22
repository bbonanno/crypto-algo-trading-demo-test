name := "JBCN"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-Ypartial-unification",
  "-language:higherKinds,implicitConversions",
  "-Xfatal-warnings"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

scalafmtOnCompile := true

libraryDependencies += "io.estatico" %% "newtype" % "0.4.2"

libraryDependencies += "org.mockito"   %% "mockito-scala-scalatest" % "1.4.6" % Test
libraryDependencies += "org.scalatest" %% "scalatest"               % "3.0.7" % Test
