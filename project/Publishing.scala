import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

object Publishing {

  val artifactoryUserName = Def.task(sys.props.get("iOfficeBartifactoryUsername"))
  val artifactoryPassword = Def.task(sys.props.get("iOfficeBartifactoryPassword"))
  val artifactoryHost = "http://bartifactory.corp.iofficecorp.com:8081"

  val PublishSettings = Seq(
    publishArtifact in Test := false,
    publishArtifact in (Compile, packageDoc) := true,
    publishArtifact in (Compile, packageSrc) := true,
    publishTo := Some(Resolver.url("ioffice-sbt-plugins", new URL(s"$artifactoryHost/artifactory/sbt-plugins/"))(Resolver.ivyStylePatterns)),
    credentials += Credentials(
      "Artifactory",
      "localhost",
      artifactoryUserName.value.getOrElse("no userame set"),
      artifactoryPassword.value.getOrElse("no password set")
    )
  )

  val CrossPublishSettings = PublishSettings ++ Seq(
    crossScalaVersions := Dependencies.SupportedScalaVersions
  )

  /* `publish` performs a no-op */
  val NoopPublishSettings = Seq(
    packagedArtifacts in file(".") := Map.empty,
    publish := (),
    publishLocal := (),
    publishArtifact := false,
    publishTo := None
  )

  val PluginPublishSettings = PublishSettings ++ Seq()

  val LibraryPublishSettings = CrossPublishSettings ++ Seq(
    publishTo := Some("ioffice-sbt-plugins" at s"$artifactoryHost/artifactory/sbt-plugins")
  )

  val ReleaseSettings = Seq(
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}
