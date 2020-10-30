package org.alexeyn.sparkjoin

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

import plotly._, element._, layout._, Plotly._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.reflect.ClassTag

case class LoanPercent(quarterDate: String, percent: Float)

object LoanPercent {
  def apply(line: Array[String]): LoanPercent =
    LoanPercent(line.head, line(4).toFloat)
}

case class PropertyPrice(quarterDate: String, index2010: Float)

object PropertyPrice {
  def apply(line: Array[String]): PropertyPrice = {
    val date = LocalDate.parse(line.head)
    val month = date.getMonth.getDisplayName(TextStyle.SHORT, Locale.getDefault)
    PropertyPrice(date.getYear + month, line(1).toFloat)
  }
}

object JoinStat {
  def fire = {
    val spark = SparkSession.builder
      .appName("eu-stats")
      .config("spark.master", "local[*]")
      .getOrCreate()

    val loanDs =
      spark.createDataFrame(createRdd(spark, "data.csv", 5, LoanPercent.apply))
    val priceDs =
      spark.createDataFrame(createRdd(spark, "QDEN628BIS.csv", 1, PropertyPrice.apply))

    val key = "quarterDate"
    val joined = loanDs
      .join(priceDs.alias("p"), loanDs.col(key) === priceDs.col(key), "inner")
      .select(loanDs.col(key), loanDs.col("percent"), priceDs.col("index2010"))
      .sort(loanDs.col(key))
      .collect()

    val date = joined.map(_.getString(0)).toSeq
    val loan = joined.map(_.getFloat(1).toDouble)
    val maxLoan = loan.max
    val normLoan = loan.map(_ / maxLoan).toSeq

    val price = joined.map(_.getFloat(2).toDouble)
    val maxPrice = price.max
    val normPrice = price.map(_ / maxPrice).toSeq

    val plot = Seq(
      Scatter(
        date,
        normLoan,
        name = "% Loan for House Purchase",
        text = loan.map(_.toString).toSeq
      ),
      Scatter(
        date,
        normPrice,
        name = "Residential Property Price (quarterly)",
        text = price.map(_.toString).toSeq
      )
    )
    plot.plot(
      title = "Loan vs. Property Price",
      openInBrowser = false
    )
    spark.stop
  }

  def createRdd[T: ClassTag](
      spark: SparkSession,
      path: String,
      linesToSkip: Int,
      parse: Array[String] => T
  ): RDD[T] = {
    spark.sparkContext
      .textFile(path)
      .mapPartitionsWithIndex((i, it) =>
        if (i == 0) it.drop(linesToSkip) else it
      )
      .map(_.split(",").to[Array])
      .map(parse)
  }
}

object Main extends App {
  JoinStat.fire
}
