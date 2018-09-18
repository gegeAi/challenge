import sbt.Keys.mainClass

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.11.6",
  libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7",
  libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.7",
  libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  libraryDependencies += "org.scalamock" %% "scalamock" % "4.1.0" % Test,
  libraryDependencies += "org.mockito" % "mockito-all" % "1.8.4" % Test
)

lazy val root = project.in(sbt.file("root"))
  .dependsOn(aggregator, indexer, top)
  .settings(
    commonSettings,
    mainClass in assembly := Some("com.jliermann.root.Boot"),
    assemblyJarName in assembly := "phenix-challenge.jar",
    unmanagedClasspath in Test += baseDirectory.value / "resources",
    libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"
  ).enablePlugins(AssemblyPlugin)

lazy val util = project.in(sbt.file("util"))
    .settings(
      commonSettings,
      libraryDependencies += "com.jsuereth" %% "scala-arm" % "2.0"
    ).enablePlugins(AssemblyPlugin)

lazy val top = project.in(sbt.file("top"))
  .dependsOn(parser, util, aggregator)
    .settings(
      commonSettings,
      mainClass in assembly := Some("com.jliermann.top.TopBoot"),
      assemblyJarName in assembly := "top100.jar",
      libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"
    ).enablePlugins(AssemblyPlugin)

lazy val aggregator = project.in(sbt.file("aggregator"))
  .dependsOn(parser, util, indexer)
  .settings(
    commonSettings,
    mainClass in assembly := Some("com.jliermann.aggregator.AggregatorBoot"),
    assemblyJarName in assembly := "aggregator.jar",
    libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"
  ).enablePlugins(AssemblyPlugin)

lazy val indexer = project.in(sbt.file("indexer"))
  .dependsOn(parser, util)
  .settings(
    commonSettings,
    mainClass in assembly := Some("com.jliermann.indexer.IndexerBoot"),
    assemblyJarName in assembly := "indexer.jar",
    libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"
  ).enablePlugins(AssemblyPlugin)

lazy val parser = Project("parser", sbt.file("parser"))
  .settings(
    commonSettings
  ).enablePlugins(AssemblyPlugin)