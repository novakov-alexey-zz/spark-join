name := "spark-join"

organization := "org.alexeyn"

version := "0.0.1"

scalaVersion := "2.12.12"

val sparkVersion = "2.4.7"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion, //% "provided" exclude("org.spark-project.spark", "unused"),
  "org.apache.spark" %% "spark-sql" % sparkVersion,// % "provided" exclude("org.spark-project.spark", "unused"),   
  "org.plotly-scala" %% "plotly-render" % "0.8.0"
)

initialCommands :="""import org.alexeyn.sparkjoin.JoinStat._"""
