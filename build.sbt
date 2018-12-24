val Scala_212 = "2.12.8"
val Scala_211 = "2.11.11"

val catsEffectVersion          = "1.1.0"
val catsTaglessVersion         = "0.2.0"
val catsParVersion             = "0.2.0"
val doobieVersion              = "0.6.0"
val catsVersion                = "1.5.0"
val scalacheckShapelessVersion = "1.1.6"
val scalatestVersion           = "3.0.4"
val simulacrumVersion          = "0.14.0"
val scalacacheVersion          = "0.27.0"
val macroParadiseVersion       = "2.1.1"
val kindProjectorVersion       = "0.9.8"
val refinedVersion             = "0.9.3"

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
  scalaVersion := Scala_211,
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
  ) ++ compilerPlugins
)

val crossBuiltCommonSettings = commonSettings ++ Seq(crossScalaVersions := Seq(Scala_211, Scala_212))

def module(moduleName: String): Project =
  Project(moduleName, file(moduleName)).settings(crossBuiltCommonSettings).settings(name += s"-$moduleName")

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
  .aggregate(core)

val doobie = module("doobie")
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "eu.timepit"   %% "refined"     % refinedVersion
    )
  )
  .dependsOn(core)
  .aggregate(core)

val allModules = List(core, scalacache, doobie)

val microsite = project
  .settings(
    scalaVersion := Scala_211,
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
    skip in publish := true
  )
  .enablePlugins(MicrositesPlugin)
  .dependsOn(allModules.map(x => x: ClasspathDep[ProjectReference]): _*)
  .aggregate(allModules.map(x => x: ProjectReference): _*)

val sup =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(skip in publish := true, crossScalaVersions := List())
    .aggregate((microsite :: allModules).map(x => x: ProjectReference): _*)
