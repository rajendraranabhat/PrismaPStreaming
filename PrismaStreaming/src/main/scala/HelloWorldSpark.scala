import org.apache.spark.SparkContext

import org.apache.spark.SparkConf
import org.apache.spark.sql.cassandra.CassandraSQLContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
// this is used to implicitly convert an RDD to a DataFrame.

import org.json4s._
import org.json4s.native.JsonParser

import com.datastax.spark.connector._
import org.apache.spark.rdd._
import com.datastax.spark.connector.rdd._
import com.datastax.spark.connector.cql._

import java.util.Properties

import kafka.serializer.StringDecoder
import kafka.producer.KeyedMessage
import kafka.producer.Producer
import kafka.producer.ProducerConfig

import scala.annotation.tailrec
import scala.util.Random
import org.joda.time.DateTime

import org.apache.log4j.Logger
import org.apache.log4j.LogManager

object HelloWorldSpark {

  def main(args: Array[String]) {
    // set spark context
    val conf = new SparkConf().setAppName("wordcount").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val distFile = sc.textFile("readme.txt")

    val words = distFile.flatMap(value => value.split("\\s+"))

    words.foreach { println }
  }
}

