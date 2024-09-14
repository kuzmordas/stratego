lazy val domain = (project in file("domain"))
  .settings(
    scalaVersion := "2.13.14"
  )

lazy val httpServer = (project in file("http-server"))
  .settings(
    scalaVersion := "2.13.14",
    assembly / assemblyJarName := "strategoServer.jar",
    libraryDependencies ++= Seq(
      Dependencies.zio,
      Dependencies.zioHttp,
      Dependencies.zioJson,
      Dependencies.zioMagnolia,
      Dependencies.zioTypesafe,
      Dependencies.tofuZioLogging,
      Dependencies.tofuLogback
    )
  )
  .dependsOn(domain)

ThisBuild / assemblyMergeStrategy := {
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x if x.endsWith("module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
  
