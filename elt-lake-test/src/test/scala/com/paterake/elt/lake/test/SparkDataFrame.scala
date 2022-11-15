package com.paterake.elt.lake.test

import org.apache.spark.sql.SparkSession

object SparkDataFrame extends App {

  val sparkSession = SparkSession.builder
    .master("local[*]")
    .appName("SparkDataFrameTest")
    .config("spark.driver.bindAddress", "127.0.0.1")
    .getOrCreate()

  import sparkSession.implicits._

  val df = Seq(
    ("a", "1"),
    ("b", "2"),
    ("c", "3"),
    ("d", "4")
  ).toDF("foo/bar", "colb")
  df.printSchema()


}
