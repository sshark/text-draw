name := "Text Draw"

ThisBuild / scalaVersion := "3.0.1"

lazy val commonSettings = Seq(
  organization := "org.teckhooi",
  version := "1.3-SNAPSHOT",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-source:future", "-rewrite"),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect"                   % "3.2.2",
    "org.scalactic" %% "scalactic"                     % "3.2.9",
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.2.0" % Test
  )
)

lazy val root = project.aggregate(core, app) in file(".")

lazy val core = (project in file("text-draw-core"))
  .settings(commonSettings)

lazy val app = (project in file("text-draw-app"))
  .settings(commonSettings)
  .settings(name := "text-draw")
  .settings(
    // native-image flag "--initialize-at-build-time" is required for Cats Effect applications
    nativeImageOptions ++= List("--initialize-at-build-time", "--no-fallback"))
  .enablePlugins(NativeImagePlugin)
  .settings(Compile / mainClass := Some("org.teckhooi.textdraw.Main"))
  .dependsOn(core % "test -> test;compile -> compile")
