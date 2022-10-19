import sbt.internal.ProjectMatrix

Global / onChangedBuildSource := ReloadOnSourceChanges

val Scala_212 = "2.12.14"
val Scala_213 = "2.13.8"
val Scala_3 = "3.1.3"

val scala2Only = Seq(Scala_212, Scala_213)
val scala2And3 = scala2Only :+ Scala_3

val catsEffectVersion = "3.3.14"
val catsTaglessVersion = "0.14.0"
val doobieVersion = "1.0.0-RC2"
val catsVersion = "2.8.0"
val scalacacheVersion = "1.0.0-M6"
val fs2KafkaVersion = "2.5.0"
val kindProjectorVersion = "0.13.2"
val redis4catsVersion = "1.2.0"
val h2Version = "1.4.200"
val log4CatsVersion = "2.4.0"
val http4sVersion = "0.23.15"
val akkaHttpVersion = "10.2.10"
val circeVersion = "0.14.2"
val sttpVersion = "3.7.6"
val cassandraVersion = "4.12.0"
val testcontainersScalaVersion = "0.40.11"

val GraalVM11 = "graalvm-ce-java11@21.0.0"

ThisBuild / scalaVersion := Scala_212
ThisBuild / crossScalaVersions := Seq(Scala_212, Scala_213, Scala_3)
// Using sbt-projectmatrix, Scala versions are mapped to different modules, hence we only need to run CI on one
ThisBuild / githubWorkflowScalaVersions := Seq(Scala_212)
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

val scala2CompilerPlugins = List(
  compilerPlugin(("org.typelevel" % "kind-projector" % kindProjectorVersion).cross(CrossVersion.full))
)

val compilerPlugins = List(
  compilerPlugin(("org.polyvariant" % "better-tostring" % "0.3.16").cross(CrossVersion.full))
)

val commonSettings = Seq(
  scalacOptions --= Seq("-Xfatal-warnings"),
  scalacOptions ++= (if(scalaVersion.value.startsWith("3")) Seq("-Yscala-release", "3.1") else Nil),
  name := "sup",
  updateOptions := updateOptions.value.withGigahorse(false), //may fix publishing bug
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect-laws" % catsEffectVersion % Test,
    "org.typelevel" %% "cats-effect-testkit" % catsEffectVersion % Test,
    "org.typelevel" %% "cats-testkit-scalatest" % "2.1.5" % Test,
    "org.typelevel" %% "cats-laws" % catsVersion % Test,
    "org.typelevel" %% "cats-kernel-laws" % catsVersion % Test
  ) ++ compilerPlugins ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) => scala2CompilerPlugins :+ ("org.typelevel" %% "cats-tagless-laws" % catsTaglessVersion % Test)
    case _            => Seq.empty
  }),
  mimaPreviousArtifacts := Set.empty
  // mimaPreviousArtifacts := Set(organization.value %% name.value % "0.7.0")
)

def module(moduleName: String) =
  ProjectMatrix(moduleName, file("modules") / moduleName).settings(commonSettings).settings(name += s"-$moduleName")

val core = module("core")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect-kernel" % catsEffectVersion
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => Seq("org.typelevel" %% "cats-tagless-core" % catsTaglessVersion)
      case _            => Seq.empty
    })
  )
  .jvmPlatform(scala2And3)

val scalacache = module("scalacache")
  .settings(
    libraryDependencies ++= Seq("com.github.cb372" %% "scalacache-core" % scalacacheVersion)
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core)

val doobie = module("doobie")
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "com.h2database" % "h2" % h2Version % Test
    )
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core % "compile->compile;test->test")

val cassandra = module("cassandra")
  .settings(
    libraryDependencies ++= Seq(
      "com.datastax.oss" % "java-driver-core" % cassandraVersion,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testcontainersScalaVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-cassandra" % testcontainersScalaVersion % Test
    )
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core % "compile->compile;test->test")

val redis = module("redis")
  .settings(
    libraryDependencies ++= Seq(
      "dev.profunktor" %% "redis4cats-effects" % redis4catsVersion
    )
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core)

val log4cats = module("log4cats")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "log4cats-core" % log4CatsVersion
    )
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core)

val http4s = module("http4s")
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion
    )
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core)

val http4sClient = module("http4s-client")
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-client" % http4sVersion
    )
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core)

val akkaHttp = module("akka-http")
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "org.typelevel" %% "cats-effect-std" % catsEffectVersion
    )
  )
  .jvmPlatform(scala2Only)
  .dependsOn(core)

val circe = module("circe")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion
    )
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core)

val sttp = module("sttp")
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion
    )
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core)

val kafka = module("fs2-kafka")
  .settings(
    libraryDependencies ++= Seq(
      "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testcontainersScalaVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-kafka" % testcontainersScalaVersion % Test
    ),
    mimaPreviousArtifacts := Set()
  )
  .jvmPlatform(scala2And3)
  .dependsOn(core % "compile->compile;test->test")

val allModules = List(
  core,
  scalacache,
  doobie,
  redis,
  log4cats,
  http4s,
  http4sClient,
  akkaHttp,
  circe,
  sttp,
  cassandra,
  kafka
)

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
    .aggregate( /* microsite ::  */ allModules.flatMap(_.projectRefs): _*)
