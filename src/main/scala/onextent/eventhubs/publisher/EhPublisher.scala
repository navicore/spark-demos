package onextent.eventhubs.publisher

import com.microsoft.azure.eventhubs.{EventData, EventHubClient}
import com.microsoft.azure.servicebus.ConnectionStringBuilder
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


object EhPublisher extends Serializable with LazyLogging {

  private val config = ConfigFactory.load().getConfig("main")
  private val namespaceName = config.getString("eventhubs.eventHubNamespace")
  private val eventHubName = config.getString("eventhubs.eventHubName")
  private val sasKeyName = config.getString("eventhubs.policyName")
  private val sasKey = config.getString("eventhubs.policyKey")
  private val connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey)
  final val ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load().getConfig("main")

    val sparkConfig = new SparkConf().set("spark.cores.max", "2")
    val ssc = new StreamingContext(new SparkContext(sparkConfig), Seconds(config.getString("kafka.batchDuration").toInt))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> config.getString("kafka.brokerList"),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> config.getString("kafka.consumerGroup"),
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array(config.getString("kafka.topic"))

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )


    stream.map(record => (record.key, record.value)).foreachRDD(rdd => rdd.foreach(o => {
      println(s"key ${o._1} val: ${o._2}")
      val sendEvent = new EventData(o._2.getBytes("UTF8"))
      EhPublisher.ehClient.send(sendEvent)
    }))

    ssc.start()
    ssc.awaitTermination()

  }
}

