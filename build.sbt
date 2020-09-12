name := "CayenneScala"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.apache.cayenne" % "cayenne-server" % "4.2.M1",
  "org.typelevel" %% "cats-core" % "2.2.0",

  "org.slf4j" % "slf4j-simple" % "1.7.30" % Test,
  "org.apache.derby" % "derby" % "10.14.2.0" % Test,
  "com.lihaoyi" %% "utest" % "0.7.5" % Test,

)

testFrameworks += new TestFramework("utest.runner.Framework")

scalacOptions ++= Seq(
  "-encoding", "utf8", // Option and arguments on same line
  "-Xfatal-warnings",  // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)