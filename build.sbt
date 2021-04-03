val Scala_213 = "2.13.5"
val Scala_3 = "3.0.0-RC1"

val catsEffectVersion = "2.4.1"
val catsTaglessVersion = "0.12"
val doobieVersion = "0.12.1"
val catsVersion = "2.5.0"
val scalacacheVersion = "0.28.0"
val kindProjectorVersion = "0.11.3"
val fs2RedisVersion = "0.13.1"
val h2Version = "1.4.200"
val log4CatsVersion = "2.0.1"
val http4sVersion = "0.21.21"
val circeVersion = "0.13.0"
val sttpVersion = "1.7.2"
val akkaHttpVersion = "10.2.4"

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

val commonSettings = Seq(
  scalaVersion := Scala_213,
  scalacOptions --= Seq("-Xfatal-warnings"),
  name := "sup",
  updateOptions := updateOptions.value.withGigahorse(false), //may fix publishing bug
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-tagless-laws" % catsTaglessVersion % Test,
    "org.typelevel" %% "cats-effect-laws" % catsEffectVersion % Test,
    "org.typelevel" %% "cats-testkit-scalatest" % "2.1.1" % Test,
    "org.typelevel" %% "cats-laws" % catsVersion % Test,
    "org.typelevel" %% "cats-kernel-laws" % catsVersion % Test
  ) ++ {
    if (isDotty.value) Nil else compilerPlugin(("org.typelevel" % "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)) :: Nil

  },
  mimaPreviousArtifacts := Set(organization.value %% name.value % "0.7.0")
)

val crossBuiltCommonSettings = commonSettings ++ Seq(crossScalaVersions := Seq(Scala_213, Scala_3))

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
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
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

def dropMinor(version: String): String = version.split("\\.").init.mkString(".")

def enumerateAnd(values: List[String]): String = {
  val init = values.init
  val last = values.last

  if (values.length > 1) {
    init.mkString(", ") + " and " + last
  } else values.mkString
}

val microsite = project
  .settings(
    scalaVersion := Scala_213,
    crossScalaVersions := List(),
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
    libraryDependencies ++= {
      if (isDotty.value) Nil else compilerPlugin(("org.typelevel" % "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)) :: Nil

    },
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.29.1"
    ),
    skip in publish := true,
    buildInfoPackage := "sup.buildinfo",
    micrositeAnalyticsToken := "UA-55943015-9",
    buildInfoKeys := Seq[BuildInfoKey](lastStableVersion),
    lastStableVersion := dynverGitDescribeOutput
      .value
      .map(_.ref.value.tail)
      .getOrElse(throw new Exception("There's no output from dynver!")),
    mdocVariables := Map(
      "SCALA_VERSIONS" -> enumerateAnd((crossScalaVersions in core).value.toList.map(dropMinor))
    )
  )
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(allModules.map(x => x: ClasspathDep[ProjectReference]): _*)

val sup =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(skip in publish := true, crossScalaVersions := List(), mimaPreviousArtifacts := Set.empty)
    .aggregate((microsite :: allModules).map(x => x: ProjectReference): _*)
