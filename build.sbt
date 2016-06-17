
val commonSettings =
  Seq(
    organization := "ar.com.crypticmind",
    scalaVersion := "2.11.8",
    scalaBinaryVersion := "2.11",
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
    ),
    version := "0.3-SNAPSHOT"
  )

val noPublishing = Seq(
  publish := (),
  publishLocal := ()
)

val publishing = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra :=
    <url>https://github.com/crypticmind/scala-swagger-modelgen</url>
      <licenses>
        <license>
          <name>MIT License</name>
          <url>http://opensource.org/licenses/MIT</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:crypticmind/scala-swagger-modelgen.git</url>
        <connection>scm:git:git@github.com:crypticmind/scala-swagger-modelgen.git</connection>
      </scm>
      <developers>
        <developer>
          <id>crypticmind</id>
          <name>Carlos Ferreyra</name>
          <url>http://crypticmind.com.ar</url>
        </developer>
      </developers>
)

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
      "joda-time"         % "joda-time"               % "2.3"     % "provided",
      "org.scalatest"     %%  "scalatest"             % "2.1.7"   % "test"
    )
  )
  .settings(noPublishing: _*)

lazy val macrosSub = Project("scala-swagger-modelgen", file("macros"))
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
        exclude("com.wordnik",                    "swagger-core_2.9.1"),
      "joda-time"         % "joda-time"               % "2.3"     % "provided"
    )
  )
  .settings(publishing: _*)
