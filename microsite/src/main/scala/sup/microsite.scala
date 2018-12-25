package sup

object microsite {

  def sbtDependencies(module1: String, rest: String*): Unit = {
    val prefix = "libraryDependencies"

    val remaining =
      if (rest.isEmpty) s" += ${sbtDependency(module1)}"
      else
        s""" ++= Seq(
         |  ${sbtDependency(module1)},
         |  ${rest.map(sbtDependency).mkString(",\n  ")}
         |)""".stripMargin

    println("```scala\n" + prefix + remaining + "\n```")
  }

  def sbtDependency(moduleName: String): String =
    s""""com.kubukoz" %% "sup-$moduleName" % "${sup.buildinfo.BuildInfo.version}""""

  def ammDependency(moduleName: String): Unit =
    println(s"""```
       |import $$ivy.`com.kubukoz::sup-$moduleName:${sup.buildinfo.BuildInfo.version}`
       |```""".stripMargin)
}
