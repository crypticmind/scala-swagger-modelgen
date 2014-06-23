import sbt._
import Keys._

object Publish {

  val noPublishing = Seq(
    publish := (),
    publishLocal := ()
  )

  val settings = Seq(
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
}
