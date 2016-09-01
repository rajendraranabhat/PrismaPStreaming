import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.cassandra.CassandraSQLContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils

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

class TopicMessage(
  val year: Int,
  val month: Int,
  val day: Int,
  val hour: Int,
  val minute: Int,
  val second: Int,
  val millis: Int,
  val msg: String // What happened yo!?
)

object CassandraKafka {
  
  // Our simple case classes for human types.
  case class SubHuman(id:String, firstname:String, lastname:String, isgoodperson:Boolean)
  
  /*case class Human(
    id: String,
    firstname: Option[String],
    lastname: Option[String],
    isGood: Boolean
  )*/
  
  // Our simple case classes for human.
  case class Human(id:String, firstname:String, lastname:String)
  case class SimpleHuman(firstname:String, lastname:String)
  case class CallableHuman(fname:Option[String], lname:Option[String], gender:Option[String], phone:Option[String])

  
  def main(args: Array[String]) {
    
     // Enable logging in $SPARK_HOME/CONF/log4j.properties - logs to console by default.
     //val log:Logger = LogManager.getLogger("Streaming Kafka Consumer - test!")
    
     // 1. Create a conf for the spark context
     // In this example, spark master and cassandra nodes info are provided in a separate count.conf file.
      
     val conf = new SparkConf(true).set("spark.cassandra.connection.host", "deepc04.acis.ufl.edu").setAppName("KafkaCassandraStreaming")
     
     // 2. Create a spark context
     val sc = new SparkContext(conf)
     
     val ssc:StreamingContext = new StreamingContext(sc, Seconds(3))
     
     // http://kafka.apache.org/08/configuration.html -> See section 3.2 Consumer Configs
     val kafkaParams = Map(
        "zookeeper.connect" -> "localhost:2181",
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
    messages.foreachRDD { rdd => 
      val message:RDD[(String, String)] = rdd.map { y => (randomString(25), y._2) }
      message.saveToCassandra("things", "messages", SomeColumns("key","msg"))
    }
     
    // Listen for SIGTERM and shutdown gracefully.
    /*sys.ShutdownHookThread {
      //log.info("Gracefully stopping Spark Streaming Application")
      //ssc.stop(stopSparkContext = true, stopGracefully = true)
      ssc.stop(true, true)
      //log.info("Application stopped")
    } */
    
    ssc.start()             // Start the computation
    ssc.awaitTermination()  // Wait for the computation to terminate (manually or due to any error)
     
     /*val props:Properties = new Properties()
     props.put("metadata.broker.list", "localhost:9092")
     props.put("serializer.class", "kafka.serializer.StringEncoder")
 
     val config = new ProducerConfig(props)
     val producer = new Producer[String, String](config)*/
  
     // 3. Create an rdd that connect to the cassandra table "schema_keyspaces" of the keyspace "system"
     //val rdd = sc.cassandraTable("people", "person")
  
     // 4. Count the number of row
     //val num_row = rdd.count()
     //println("\n\n <<<<<<<<<>>>><<<<<<<<<Number of rows in system.schema_keyspaces: " + num_row + "\n\n")
     
     //val csc = new CassandraSQLContext(sc)
     //csc.sql("SELECT * from people.person").collect().foreach(println)
     
     // Get ALL of my things.
     /*val readRDD = sc.cassandraTable[(String,Integer)]("things", "mythings")
     val thingsThatMatter = readRDD.filter(_._2> 0) //Filter
     thingsThatMatter.saveToCassandra("things", "thingsthatmatter", SomeColumns("key", "value"))
     //Fetch back the things that matter that we just wrote to Cassandra.
     val writtenRDD = sc.cassandraTable[(String,Integer)]("things", "thingsthatmatter")
     // Now tell the world only about the things that matter.
     println("<<<<<<<<<<<<<<<<<<<<<<"+writtenRDD.collect())
     writtenRDD.toArray.foreach(println)
     // Clean up
     // Comment this out if you'd like to validate your results inside Cassandra.
     CassandraConnector(conf).withSessionDo { session =>
       session.execute("TRUNCATE things.thingsthatmatter")
     }*/
     
     
     /*
     //
     // We will fetch all humans from Cassandra, but we wil only be fetching a subset of the human table by limiting with
     // the use of 'select'--------------------------------------------------------->
     val subHumans:Array[SubHuman] = sc.cassandraTable[SubHuman]("things", "human").
                                        select("id","firstname","lastname","isgoodperson").toArray
     
     // Now that we have an array of 'sub-humans' we can quickly rip through and find who is good and who is not.
     subHumans.foreach { subHuman:SubHuman =>
      if (subHuman.isgoodperson) 
        println("%s %s IS a good person.".format(subHuman.firstname, subHuman.lastname))
      else
        println("%s %s IS NOT a good person.".format(subHuman.firstname, subHuman.lastname))
     }
     
     
     * In the following section we are going to perform the same operation, but we are going to do so in a bit more
     * verbose way.  For more complex operations, filtering out bad data, dealing with missing bits (null), and other 
     * things of that nature, being able to do things like this is not only good, but will likely be necessary in the 
     * world of real data.
     
     val rdd:CassandraRDD[CassandraRow] = sc.cassandraTable("things", "human")
    
     val humans = rdd.toArray.map { row:CassandraRow =>
       // To display your column names for this row uncomment the following:
       //println(row.columnNames) // You can imagine there will be some cool things to do with the column names for a NoSQL row.
       // ...or show them individually:
       //row.columnNames.toArray.foreach(println)
        
       // As most of our fields are nullable we'll fetch Options in most cases.  ...and do that a couple different ways.
       val id:String                    = row.getString("id")
       val firstname:Option[String]     = row.get[Option[String]]("firstname") // One way to get String Option
       val lastname:Option[String]      = row.getStringOption("lastname") // Another way to get String Option
       val isGoodPerson:Option[Boolean] = row.getBooleanOption("isgoodperson")
       // Here we'll make sure we have a boolean to work with.  Operations like this may also enforce any other client 
       // side rules (e.g. validate numbers stored as Strings)
       val isGood = isGoodPerson match {
          case Some(x) => x
          case _ => false
       }
       
       // And now we create our human...
       Human(id,firstname,lastname,isGood)
     }
     
     // Now, again, let us crudely see who is good and who is not...
    humans.foreach { human:Human =>
      // Null safety first
      val fname:String = human.firstname match {
        case Some(name) => name
        case _ => "Unknown"
      }
      
      val lname:String = human.lastname match {
        case Some(name) => name
        case _ => "Unknown"
      }
      
      if (human.isGood)
        println("%s %s is a good person.".format(fname, lname))
      else
        println("%s %s is not a good person.".format(fname, lname))
    }*/
    
    /*
    // Fetch only the details we need and only for good people.
    val goodHumans:CassandraRDD[Human] = sc.cassandraTable[Human]("things", "human1").select("id", "firstname", "lastname").where("isgoodperson = True")
    
    // Let's transform our "complex" humans to a RDD full of simplified humans 
    val goodSimpleHumans:RDD[SimpleHuman] = goodHumans.map(h =>  SimpleHuman(h.firstname, h.lastname))
    
    // Now let's save our collection of good, simple humans off to Cassandra.
    val x:Unit = goodSimpleHumans.saveToCassandra("things", "goodhuman1", SomeColumns("firstname", "lastname"))
    
    // Verfiy by fetching our list of good humans and printing out their names.
    val theGoodOnes:CassandraRDD[SimpleHuman] = sc.cassandraTable[SimpleHuman]("things", "goodhuman1").select("firstname", "lastname")
    
    theGoodOnes.toArray.foreach { goodHuman:SimpleHuman =>
        println("%s %s is a good, simple person.".format(goodHuman.firstname, goodHuman.lastname))
    }
    
    ////////////////////////
    // Wide tuple example //
    ////////////////////////
    
    // Fetch all but the kitchen sink...
    // Also note we omit 'country' as our target table does not have that.
    // Alternatively you could perform a transformation to remove 'country' - but, let's keep it simple for now.
    val wideTuple: CassandraRDD[(Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], 
        Option[String], Option[String], Option[String], Option[String])] = 
      sc.cassandraTable[(Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], 
          Option[String], Option[String], Option[String], Option[String])]("things", "human1")
      .select("id", "firstname", "lastname", "gender", "address0", "address1", "city", "stateprov", "zippostal", "phone")
      .where("isgoodperson = True")
      
    // We'll omit any clever transformations for now as all we really want to illustrate is how to map this wide tuple
    // onto 'humanfromtuple' table which has many different column names from our 'human' table.
    // HUMAN     -> HUMANFROMTUPLE
    // id        -> id
    // firstname -> fname *
    // lastname  -> lname *
    // gender    -> gender
    // address0  -> address1 *
    // address1  -> address2 *
    // city      -> city
    // stateprov -> state *
    // zippostal -> zip *
    // country   -> country
    // phone     -> phone
    
    // So let's save our wide tuple of good humans off to Cassandra.
    val y:Unit = wideTuple.saveToCassandra("things", "humanfromtuple", SomeColumns(
      // NOTE: Name, number of elements, and order must align precisely between your tuple and your target table!!!
      "id", "fname", "lname", "gender", "address1", "address2", "city", "state", "zip", "phone")
    )
    
    // ...and since we cannot just assume that all worked - let's pull some of those records back and print them out.
    val personsToCall:CassandraRDD[CallableHuman] = sc.cassandraTable[CallableHuman]("things", "humanfromtuple").select("fname", "lname", "gender", "phone")
    
    personsToCall.toArray.foreach { ch:CallableHuman =>
      val genderVerbose:String = ch.gender match {
        case Some("m") => "male"
        case Some("f") => "female"
        case Some("t") => "transgender"
        case _ => "unknown"
      }
      
      println("%s %s is %s and can be reached at %s.".format(ch.fname, ch.lname, genderVerbose, ch.phone))
    }
    
    // Clean up
    // Comment this out if you'd like to validate your results inside Cassandra.
    CassandraConnector(conf).withSessionDo { session =>
      session.execute("TRUNCATE things.goodhuman1")
      session.execute("TRUNCATE things.humanfromtuple")
    }*/   
     
    /*val msg0 = new TopicMessage(year=2015, month=1, day=20, hour=9, minute=32, second=27, millis=666, msg="What the foo?")
    val msg1 = new TopicMessage(year=2015, month=1, day=20, hour=9, minute=32, second=27, millis=667, msg="What the bar?")
    val msg2 = new TopicMessage(year=2015, month=1, day=20, hour=9, minute=32, second=27, millis=668, msg="What the baz?")
    val msg3 = new TopicMessage(year=2015, month=1, day=20, hour=9, minute=32, second=27, millis=669, msg="No! No! NO! What the FU!")
    
    val msgs:List[TopicMessage] = msg0 :: msg1 :: msg2 :: msg3 :: Nil
	
  	for (evt:TopicMessage <- msgs) {
  	  producer.send(new KeyedMessage[String, String]("test", evt.msg))
  	}*/
    
    /*val topic:String = "test"
    val secondsToRun = 10
    val printProgress:Boolean = false
    blather(printProgress, s"This will run for: ${secondsToRun}s")
    
    val start:DateTime = DateTime.now()
    val startMillis:Long = start.getMillis()
    val then:DateTime = start.plusSeconds(secondsToRun)
    
    var cnt:Int = 0
    var remaining:Int = secondsToRun
    
    while (then.isAfterNow()) {
      val nowMillis = DateTime.now().getMillis()
      if (printProgress && cnt % 5000 == 0) {
        val nowRemaining = (secondsToRun - (nowMillis - startMillis)/1000).toInt
        if (nowRemaining < remaining) {
          remaining = nowRemaining
          if (remaining > 0) println(s"${cnt} * ${remaining} seconds left...")
          else println("${cnt} * Less than one second remaining...")
        }
      }
      // Some fake "processing"...
      val rnd500:String = randomString(500)
      val rnd50:String = rnd500.take(50)
      val rnd25:String = rnd50.takeRight(25)
	    val msg = s"${nowMillis},${rnd25},${rnd50},${rnd500}" // Add millis as an "id".
      producer.send(new KeyedMessage[String, String](topic, msg))
      cnt += 1
    }
    
    val mps:Float = cnt/secondsToRun
    blather(printProgress, s"Produced ${cnt} msgs in ${secondsToRun}s -> ${mps}m/s.")*/
    
    
     
     // 5. Stop the spark context.
     //sc.stop
   }
  
  def blather(p:Boolean, msg:String):Unit = {
    if (p) println(msg)
  }

  @tailrec
  def doRandomString(n: Int, charSet:Seq[Char], list: List[Char]): List[Char] = {
	val rndPosition = Random.nextInt(charSet.length)
	val rndChar = charSet(rndPosition)
    if (n == 1) rndChar :: list
    else doRandomString(n - 1, charSet, rndChar :: list)
  }

  def randomString(n: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    doRandomString(n, chars, Nil).mkString
  }
  
 }




