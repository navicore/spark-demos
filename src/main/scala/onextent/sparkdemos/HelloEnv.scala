package onextent.sparkdemos

import org.apache.spark.{SparkConf, SparkContext}

object HelloEnv {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().set("spark.cores.max", "2")
      .setIfMissing("spark.master", "local[*]")
      .setAppName("Hiya")

    val sc = new SparkContext(conf)
    val NUM_SAMPLES = 100
    val count = sc.parallelize(1 to NUM_SAMPLES).map{i =>
      val x = Math.random()
      val y = Math.random()
      println(sys.env.getOrElse("EXECUTOR_VAR","Art"))
      if (x*x + y*y < 1) 1 else 0
    }.reduce(_ + _)
    println("Pi is roughly " + 4.0 * count / NUM_SAMPLES)
    println(s"I am driver ${sys.env.getOrElse("DRIVER_VAR", "Jackie")}")
    println(s"I am driver passenger ${sys.env.getOrElse("DRIVER_PASSENGER_VAR", "Alice")}")

  }
}

