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


object PrismaSparkStreaming {
  
  def main(args: Array[String]) {
    
     // Enable logging in $SPARK_HOME/CONF/log4j.properties - logs to console by default.
     //val log:Logger = LogManager.getLogger("Streaming Kafka Consumer - test!")
    
     // 1. Create a conf for the spark context
     // In this example, spark master and cassandra nodes info are provided in a separate count.conf file.
      
     val conf = new SparkConf(true).set("spark.cassandra.connection.host", "deepc04.acis.ufl.edu").setAppName("PrismaStreaming")
     
     // 2. Create a spark context
     val sc = new SparkContext(conf)
     
     val sqlContext = new org.apache.spark.sql.SQLContext(sc)
     val ssc:StreamingContext = new StreamingContext(sc, Seconds(3))
     
     // http://kafka.apache.org/08/configuration.html -> See section 3.2 Consumer Configs
     val kafkaParams = Map(
        "zookeeper.connect" -> "deepc04.acis.ufl.edu:2181",
        "zookeeper.connection.timeout.ms" -> "6000",
        "group.id" -> "test"
     )

    // Map of (topic_name -> numPartitions) to consume. Each partition is consumed in its own thread
    val topics = Map(
        "test" -> 1
    )
    
    // Assuming very small data volumes for example app - tune as necessary.
    val storageLevel = StorageLevel.MEMORY_ONLY

    val messages = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics, storageLevel)
    
    val msg = messages.map(_._2) // split the message into lines
    
    msg.print()
    
    /*messages.foreachRDD { rdd => 
      val message:RDD[String] = rdd.map { y => (y._2) }
      print("<<<<<<<<<<<"+message.toString())
      //message.saveToCassandra("things", "messages", SomeColumns("key","msg"))
    }*/
    
    //messages.map{ case (_,v) => JsonParser.parse(v).extract[MonthlyCommits]}
    //.saveToCassandra("githubstats","monthly_commits")
    
    
    //print("<<<<<<<<<<<<<<<<class="+msg)
    // this is used to implicitly convert an RDD to a DataFrame.
    //import sqlContext.implicits._

    //val df = sqlContext.read.json(msg)
    
    //println("<<<<<<<<<<<<<<<<<"+msg)
    
    /*messages.foreachRDD { rdd => 
      val message:RDD[(String, String)] = rdd.map { y => (y._2) }
      //message.saveToCassandra("things", "messages", SomeColumns("key","msg"))
      println("<<<<<<<<<<<<<<<<<"+message)
    }*/
     
    ssc.start()             // Start the computation
    ssc.awaitTermination()  // Wait for the computation to terminate (manually or due to any error)
    ssc.stop()
  }
}










