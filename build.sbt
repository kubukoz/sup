val Scala_212 = "2.12.13"
val Scala_213 = "2.13.5"

val catsEffectVersion = "3.1.0"
val catsTaglessVersion = "0.14.0"
val doobieVersion = "1.0.0-M2"
val catsVersion = "2.6.0"
val scalacacheVersion = "1.0.0-M2"
val kindProjectorVersion = "0.11.3"
val redis4catsVersion = "1.0.0-RC3"
val h2Version = "1.4.200"
val log4CatsVersion = "2.1.0"
val http4sVersion = "1.0.0-M21"
val circeVersion = "0.13.0"
val sttpVersion = "3.3.1"

val GraalVM11 = "graalvm-ce-java11@21.0.0"

ThisBuild / scalaVersion := Scala_212
ThisBuild / crossScalaVersions := Seq(Scala_212, Scala_213)
ThisBuild / githubWorkflowJavaVersions := Seq(GraalVM11)
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("test", "mimaReportBinaryIssues"))
)

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.StartsWith(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublishPreamble := Seq(
  WorkflowStep.Use(UseRef.Public("olafurpg", "setup-gpg", "v3"))
)
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List(
  "PGP_PASSPHRASE",
  "PGP_SECRET",
  "SONATYPE_PASSWORD",
  "SONATYPE_USERNAME"
).map(envKey => envKey -> s"$${{ secrets.$envKey }}").toMap

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
    ),
    versionScheme := Some("early-semver")
  )
)

val compilerPlugins = List(
  compilerPlugin(("org.typelevel" % "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)),
  compilerPlugin(("com.kubukoz" % "better-tostring" % "0.3.0").cross(CrossVersion.full))
)

val commonSettings = Seq(
  scalacOptions --= Seq("-Xfatal-warnings"),
  name := "sup",
  updateOptions := updateOptions.value.withGigahorse(false), //may fix publishing bug
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-tagless-laws" % catsTaglessVersion % Test,
    "org.typelevel" %% "cats-effect-laws" % catsEffectVersion % Test,
    "org.typelevel" %% "cats-effect-testkit" % catsEffectVersion % Test,
    "org.typelevel" %% "cats-testkit-scalatest" % "2.1.4" % Test,
    "org.typelevel" %% "cats-laws" % catsVersion % Test,
    "org.typelevel" %% "cats-kernel-laws" % catsVersion % Test
  ) ++ compilerPlugins,
  mimaPreviousArtifacts := Set.empty
  // mimaPreviousArtifacts := Set(organization.value %% name.value % "0.7.0")
)

def module(moduleName: String): Project =
  Project(moduleName, file("modules") / moduleName).settings(commonSettings).settings(name += s"-$moduleName")

val core = module("core").settings(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion,
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
      "dev.profunktor" %% "redis4cats-effects" % redis4catsVersion
    )
  )
  .dependsOn(core)

val log4cats = module("log4cats")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "log4cats-core" % log4CatsVersion
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
      "com.typesafe.akka" %% "akka-http" % "10.2.4",
      "org.typelevel" %% "cats-effect-std" % catsEffectVersion
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
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion
    )
  )
  .dependsOn(core)

val allModules =
  List(core, scalacache, doobie, redis, log4cats, http4s, http4sClient, akkaHttp, circe, sttp)

val lastStableVersion = settingKey[String]("Last tagged version")

def dropMinor(version: String): String = version.split("\\.").init.mkString(".")

def enumerateAnd(values: List[String]): String = {
  val init = values.init
  val last = values.last

  if (values.length > 1) {
    init.mkString(", ") + " and " + last
  } else values.mkString
}

/*
val microsite = project
  .settings(
    scalaVersion := Scala_213,
    crossScalaVersions := Seq(),
    micrositeName := "sup",
    micrositeDescription := "Functional healthchecks in Scala",
    micrositeDocumentationUrl := "/guide",
    micrositeUrl := "https://sup.kubukoz.com",
    micrositeAuthor := "Jakub Kozłowski",
    micrositeTwitterCreator := "@kubukoz",
    micrositeGithubOwner := "kubukoz",
    micrositeGithubRepo := "sup",
    micrositeGitterChannel := false,
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    scalacOptions --= Seq("-Xfatal-warnings"),
    libraryDependencies ++= compilerPlugins,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "com.typesafe.akka" %% "akka-stream" % "2.6.12",
      "de.heikoseeberger" %% "akka-http-circe" % "1.36.0"
    ),
    publish / skip := true,
    buildInfoPackage := "sup.buildinfo",
    micrositeAnalyticsToken := "UA-55943015-9",
    buildInfoKeys := Seq[BuildInfoKey](lastStableVersion),
    lastStableVersion := dynverGitDescribeOutput
      .value
      .map(_.ref.value.tail)
      .getOrElse(throw new Exception("There's no output from dynver!")),
    mdocVariables := Map(
      "SCALA_VERSIONS" -> enumerateAnd((core / crossScalaVersions).value.toList.map(dropMinor))
    )
  )
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(allModules.map(x => x: ClasspathDep[ProjectReference]): _*)
 */
val sup =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(publish / skip := true, crossScalaVersions := List(), mimaPreviousArtifacts := Set.empty)
    .aggregate( /* microsite ::  */ allModules.map(x => x: ProjectReference): _*)
