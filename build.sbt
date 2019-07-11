val Scala_212 = "2.12.8"
val Scala_211 = "2.11.11"

val catsEffectVersion          = "1.3.1"
val catsTaglessVersion         = "0.8"
val catsParVersion             = "0.2.1"
val doobieVersion              = "0.7.0"
val catsVersion                = "1.6.1"
val scalacheckShapelessVersion = "1.1.8"
val scalatestVersion           = "3.0.8"
val simulacrumVersion          = "0.19.0"
val scalacacheVersion          = "0.28.0"
val macroParadiseVersion       = "2.1.1"
val kindProjectorVersion       = "0.9.10"
val refinedVersion             = "0.9.8"
val fs2RedisVersion            = "0.7.0"
val h2Version                  = "1.4.199"
val log4CatsVersion            = "0.3.0"
val http4sVersion              = "0.20.4"
val circeVersion               = "0.11.1"
val sttpVersion                = "1.6.2"

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
  ))

val compilerPlugins = List(
  compilerPlugin("org.scalamacros" % "paradise" % macroParadiseVersion).cross(CrossVersion.full),
  compilerPlugin("org.spire-math" %% "kind-projector" % kindProjectorVersion)
)

val commonSettings = Seq(
  scalaVersion := Scala_212,
  scalacOptions ++= Options.all,
  fork in Test := true,
  name := "sup",
  updateOptions := updateOptions.value.withGigahorse(false), //may fix publishing bug
  libraryDependencies ++= Seq(
    "org.typelevel"              %% "cats-tagless-laws"         % catsTaglessVersion         % Test,
    "org.typelevel"              %% "cats-effect-laws"          % catsEffectVersion          % Test,
    "org.typelevel"              %% "cats-testkit"              % catsVersion                % Test,
    "org.typelevel"              %% "cats-laws"                 % catsVersion                % Test,
    "org.typelevel"              %% "cats-kernel-laws"          % catsVersion                % Test,
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % scalacheckShapelessVersion % Test,
    "org.scalatest"              %% "scalatest"                 % scalatestVersion           % Test
  ) ++ compilerPlugins,
  mimaPreviousArtifacts := Set(organization.value %% name.value.toLowerCase % "0.2.0")
)

val crossBuiltCommonSettings = commonSettings ++ Seq(crossScalaVersions := Seq(Scala_211, Scala_212))

def module(moduleName: String): Project =
  Project(moduleName, file("modules/" + moduleName))
    .settings(crossBuiltCommonSettings)
    .settings(name += s"-$moduleName")

val core = module("core").settings(
  libraryDependencies ++= Seq(
    "com.github.mpilquist" %% "simulacrum"        % simulacrumVersion,
    "org.typelevel"        %% "cats-effect"       % catsEffectVersion,
    "io.chrisdavenport"    %% "cats-par"          % catsParVersion,
    "org.typelevel"        %% "cats-tagless-core" % catsTaglessVersion
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
      "org.tpolecat"   %% "doobie-core" % doobieVersion,
      "eu.timepit"     %% "refined"     % refinedVersion,
      "com.h2database" % "h2"           % h2Version % Test
    )
  )
  .dependsOn(core % "compile->compile;test->test")

val redis = module("redis")
  .settings(
    crossScalaVersions := List(Scala_212),
    libraryDependencies ++= Seq(
      "com.github.gvolpe" %% "fs2-redis-effects" % fs2RedisVersion
    )
  )
  .dependsOn(core)

val log4cats = module("log4cats")
  .settings(
    libraryDependencies ++= Seq(
      "io.chrisdavenport" %% "log4cats-core"   % log4CatsVersion,
      "io.chrisdavenport" %% "log4cats-extras" % log4CatsVersion % Test
    )
  )
  .dependsOn(core)

val http4s = module("http4s")
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-dsl"  % http4sVersion
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

val circe = module("circe")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-generic" % circeVersion
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

val allModules = List(core, scalacache, doobie, redis, log4cats, http4s, http4sClient, circe, sttp)

val microsite = project
  .settings(
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
    scalacOptions ++= Options.all,
    scalacOptions --= Seq("-Ywarn-unused:imports"),
    libraryDependencies ++= compilerPlugins,
    libraryDependencies ++= Seq(
      "io.chrisdavenport" %% "log4cats-extras" % log4CatsVersion,
      "org.http4s"        %% "http4s-circe"    % http4sVersion
    ),
    skip in publish := true,
    buildInfoPackage := "sup.buildinfo",
    micrositeAnalyticsToken := "UA-55943015-9",
    buildInfoKeys := Seq[BuildInfoKey](version)
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
