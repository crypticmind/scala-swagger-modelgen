import sbt._
import Keys._

object Build extends Build {

  val commonSettings = Defaults.defaultSettings ++ Seq(
      scalaVersion := "2.11.0",
      //crossScalaVersions := Seq("2.10.0", "2.11.0"),
      scalacOptions := Seq(
        "-encoding",
        "utf8",
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
    )

  lazy val main = Project("main", file("."))
    .dependsOn(macrosSub)
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.wordnik"       %   "swagger-core_2.10"     % "1.3.6",
        "org.scalatest"     %%  "scalatest"             % "2.1.7"   % "test"
      )
    )

  lazy val macrosSub = Project("macros", file("macros"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _),
      libraryDependencies ++= Seq(
        "com.wordnik"       %   "swagger-core_2.10"     % "1.3.6"
      ),
      unmanagedSourceDirectories in Compile <+= (scalaVersion, sourceDirectory in Compile) {
        case (v, dir) if v startsWith "2.10" => dir / "scala_2.10"
        case (v, dir) if v startsWith "2.11" => dir / "scala_2.11"
      }
    )
}
