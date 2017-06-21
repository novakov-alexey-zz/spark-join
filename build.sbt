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

initialCommands :="""import org.alexeyn.sparkjoin.JoinStat._"""
