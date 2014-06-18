import sbt._
import Keys._

object Build extends Build {

  val commonSettings = Defaults.defaultSettings ++ Seq(
      scalaVersion := "2.11.0",
      //crossScalaVersions := Seq("2.10.0", "2.11.0"),
      scalacOptions := Seq(
        "-encoding",
        "utf8",
        "-feature",
        "-unchecked",
        "-deprecation",
        "-target:jvm-1.7"
      ),
      javacOptions := Seq(
        "-source:1.7",
        "-target:1.7"
      ),
      resolvers ++= Seq(
        "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/"
      )
    ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  lazy val main = Project("tests", file("."))
    .dependsOn(macrosSub)
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.wordnik"       %   "swagger-core_2.10"     % "1.3.+"   % "test"
                exclude("com.fasterxml.jackson.core",     "jackson-annotations")
                exclude("com.fasterxml.jackson.jaxrs",    "jackson-jaxrs-json-provider")
                exclude("com.fasterxml.jackson.module",   "jackson-module-jsonSchema")
                exclude("com.fasterxml.jackson.module",   "jackson-module-scala_2.10")
                exclude("com.wordnik",                    "swagger-annotations")
                exclude("commons-lang",                   "commons-lang")
                exclude("org.json4s",                     "json4s-ext_2.10")
                exclude("org.json4s",                     "json4s-jackson_2.10")
                exclude("org.json4s",                     "json4s-native_2.10"),
        "org.scalatra"      %   "scalatra-swagger_2.10" % "2.3.+"   % "provided"
                exclude("com.typesafe.akka", "akka-actor")
                exclude("com.wordnik",                    "swagger-core_2.9.1"),
        "org.scalatest"     %%  "scalatest"             % "2.1.7"   % "test"
      )
    )

  lazy val macrosSub = Project("macros", file("macros"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _),
      libraryDependencies ++= Seq(
        "com.wordnik"       %   "swagger-core_2.10"     % "1.3.+"   % "provided"
                exclude("com.fasterxml.jackson.core",     "jackson-annotations")
                exclude("com.fasterxml.jackson.jaxrs",    "jackson-jaxrs-json-provider")
                exclude("com.fasterxml.jackson.module",   "jackson-module-jsonSchema")
                exclude("com.fasterxml.jackson.module",   "jackson-module-scala_2.10")
                exclude("com.wordnik",                    "swagger-annotations")
                exclude("commons-lang",                   "commons-lang")
                exclude("org.json4s",                     "json4s-ext_2.10")
                exclude("org.json4s",                     "json4s-jackson_2.10")
                exclude("org.json4s",                     "json4s-native_2.10"),
        "org.scalatra"      %   "scalatra-swagger_2.10" % "2.3.+"   % "provided"
                exclude("com.typesafe.akka", "akka-actor")
                exclude("com.wordnik",                    "swagger-core_2.9.1")
      ),
      unmanagedSourceDirectories in Compile <+= (scalaVersion, sourceDirectory in Compile) {
        case (v, dir) if v startsWith "2.10" => dir / "scala_2.10"
        case (v, dir) if v startsWith "2.11" => dir / "scala_2.11"
      }
    )
}
