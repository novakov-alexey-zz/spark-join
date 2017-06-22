package org.alexeyn.sparkjoin

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

import co.theasi.plotly.{Plot, writer, _}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.reflect.ClassTag

case class LoanPercent(quarterDate: String, percent: Float)

object LoanPercent {
  def apply(line: Array[String]): LoanPercent = LoanPercent(line.head, line(3).toFloat)
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
  val spark = SparkSession.builder().appName("eu-stats").config("spark.master", "local[*]").getOrCreate()

  val loanDs = spark.createDataFrame(createRdd("data.csv", 5, LoanPercent.apply))
  val priceDs = spark.createDataFrame(createRdd("QDEN628BIS.csv", 1, PropertyPrice.apply))

  val key = "quarterDate"
  val joined = loanDs
    .join(priceDs.alias("p"), loanDs.col(key) === priceDs.col(key), "inner")
    .select(loanDs.col(key), loanDs.col("percent"), priceDs.col("index2010"))
    .sort(loanDs.col(key))
    .collect()

  val date = joined.map(_.getString(0))
  val loan = joined.map(_.getFloat(1).toDouble)
  val maxLoan = loan.max
  val normLoan = loan.map(_ / maxLoan)

  val price = joined.map(_.getFloat(2).toDouble)
  val maxPrice = price.max
  val normPrice = price.map(_ / maxPrice)

  val p = Plot()
    .withScatter(date, normLoan, ScatterOptions().name("% Loan for House Purchase").text(loan))
    .withScatter(date, normPrice, ScatterOptions().name("Residential Property Price (quarterly)").text(price))

  def fire = draw(p, "Loan vs. Property Price", writer.FileOptions(overwrite = true))

  def createRdd[T: ClassTag](path: String, linesToSkip: Int, parse: Array[String] => T): RDD[T] = {
    spark.sparkContext.textFile(path)
      .mapPartitionsWithIndex((i, it) => if (i == 0) it.drop(linesToSkip) else it)
      .map(_.split(",").to[Array])
      .map(parse)
  }
}