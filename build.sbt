val Scala_212 = "2.12.10"
val Scala_213 = "2.13.1"

val catsEffectVersion = "2.0.0"
val catsTaglessVersion = "0.10"
val doobieVersion = "0.8.4"
val catsVersion = "2.0.0"
val scalacacheVersion = "0.28.0"
val kindProjectorVersion = "0.11.0"
val fs2RedisVersion = "0.9.1"
val h2Version = "1.4.200"
val log4CatsVersion = "1.0.1"
val http4sVersion = "0.21.0-M5"
val circeVersion = "0.12.3"
val sttpVersion = "1.7.2"

inThisBuild(
  List(
    organization := "com.kubukoz",
    homepage := Some(url("https://github.com/kubukoz/sup")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "kubukoz",
        "Jakub Kozłowski",
        "kubukoz@gmail.com",
        url("https://kubukoz.com")
      )
    )
  )
)

val compilerPlugins = List(
  compilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full)
)

val commonSettings = Seq(
  scalaVersion := Scala_212,
  scalacOptions ++= Options.all(scalaVersion.value),
  fork in Test := true,
  name := "sup",
  updateOptions := updateOptions.value.withGigahorse(false), //may fix publishing bug
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-tagless-laws" % catsTaglessVersion % Test,
    "org.typelevel" %% "cats-effect-laws" % catsEffectVersion % Test,
    "org.typelevel" %% "cats-testkit-scalatest" % "1.0.0-RC1" % Test,
    "org.typelevel" %% "cats-laws" % catsVersion % Test,
    "org.typelevel" %% "cats-kernel-laws" % catsVersion % Test
  ) ++ compilerPlugins,
  mimaPreviousArtifacts := Set()
)

val crossBuiltCommonSettings = commonSettings ++ Seq(crossScalaVersions := Seq(Scala_212, Scala_213))

def module(moduleName: String): Project =
  Project(moduleName, file("modules") / moduleName).settings(crossBuiltCommonSettings).settings(name += s"-$moduleName")

val core = module("core").settings(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "org.typelevel" %% "cats-tagless-core" % catsTaglessVersion
  )
)

val scalacache = module("scalacache")
  .settings(
    libraryDependencies ++= Seq(
      "com.github.cb372" %% "scalacache-core" % scalacacheVersion
    )
  )
  .dependsOn(core)

val doobie = module("doobie")
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "com.h2database" % "h2" % h2Version % Test
    )
  )
  .dependsOn(core % "compile->compile;test->test")

val redis = module("redis")
  .settings(
    libraryDependencies ++= Seq(
      "dev.profunktor" %% "redis4cats-effects" % fs2RedisVersion
    )
  )
  .dependsOn(core)

val log4cats = module("log4cats")
  .settings(
    libraryDependencies ++= Seq(
      "io.chrisdavenport" %% "log4cats-core" % log4CatsVersion
    )
  )
  .dependsOn(core)

val http4s = module("http4s")
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion
    )
  )
  .dependsOn(core)

val http4sClient = module("http4s-client")
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-client" % http4sVersion
    )
  )
  .dependsOn(core)

val akkaHttp = module("akka-http")
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.10"
    )
  )
  .dependsOn(core)

val circe = module("circe")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion
    )
  )
  .dependsOn(core)

val sttp = module("sttp")
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp" %% "core" % sttpVersion
    )
  )
  .dependsOn(core)

val allModules = List(core, scalacache, doobie, redis, log4cats, http4s, http4sClient, akkaHttp, circe, sttp)

val lastStableVersion = settingKey[String]("Last tagged version")

val microsite = project
  .settings(
    scalaVersion := Scala_212,
    crossScalaVersions := List(),
    micrositeName := "sup",
    micrositeDescription := "Functional healthchecks in Scala",
    micrositeUrl := "https://sup.kubukoz.com",
    micrositeAuthor := "Jakub Kozłowski",
    micrositeTwitterCreator := "@kubukoz",
    micrositeGithubOwner := "kubukoz",
    micrositeGithubRepo := "sup",
    micrositeGitterChannel := false,
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    //doesn't fork anyway though
    fork in makeMicrosite := true,
    scalacOptions ++= Options.all(scalaVersion.value),
    scalacOptions --= Seq("-Ywarn-unused:imports"),
    libraryDependencies ++= compilerPlugins,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-circe" % http4sVersion
    ),
    skip in publish := true,
    buildInfoPackage := "sup.buildinfo",
    micrositeAnalyticsToken := "UA-55943015-9",
    buildInfoKeys := Seq[BuildInfoKey](lastStableVersion),
    lastStableVersion := dynverGitDescribeOutput
      .value
      .map(_.ref.value.tail)
      .getOrElse(throw new Exception("There's no output from dynver!"))
  )
  .enablePlugins(MicrositesPlugin)
  .dependsOn(allModules.map(x => x: ClasspathDep[ProjectReference]): _*)
  .enablePlugins(BuildInfoPlugin)

val sup =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(skip in publish := true, crossScalaVersions := List(), mimaPreviousArtifacts := Set.empty)
    .aggregate((microsite :: allModules).map(x => x: ProjectReference): _*)
