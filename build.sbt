name := "spark-join"

organization := "org.alexeyn"

version := "0.0.1"

scalaVersion := "2.11.11"

val sparkVersion = "2.1.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided" withSources() withJavadoc() exclude("org.spark-project.spark", "unused"),
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided" exclude("org.spark-project.spark", "unused"),
  "co.theasi" %% "plotly" % "0.2.0"
)

libraryDependencies += "com.lihaoyi" % "ammonite" % "0.9.9" % "test" cross CrossVersion.full

sourceGenerators in Test += Def.task {
  val file = (sourceManaged in Test).value / "amm.scala"
  IO.write(file, s"""object amm extends App { ammonite.Main().run() }""")
  Seq(file)
}.taskValue

initialCommands in Test := """import org.alexeyn.sparkjoin.JoinStat._"""
initialCommands :="""import org.alexeyn.sparkjoin.JoinStat._"""
