https://support.instaclustr.com/hc/en-us/articles/213663858-Spark-Streaming-Kafka-and-Cassandra-Tutorial

spark-submit  --class CassandraCount target/scala-2.10/SparkCassandraKafka-assembly-1.0.jar


 name := "SparkCassandraKafka"
 
 version := "1.0"
 
 scalaVersion := "2.10.5"
 
 libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.0" % "provided"
 
 libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.6.0" % "provided"
 
 libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "1.6.0-M1"

 assemblyMergeStrategy in assembly <<= (assemblyMergeStrategy in assembly) {
	 (old) => {
	 case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.last
	 case x => old(x)
	 }
}


###Steps
http://kafka.apache.org/documentation.html
https://kafka.apache.org/08/quickstart.html

1. Start kafka:
bin/zookeeper-server-start.sh config/zookeeper.properties&
bin/kafka-server-start.sh config/server.properties&

1.1. Create test topic
bin/kafka-create-topic.sh --zookeeper localhost:2181 --replica 1 --partition 1 --topic test

2. Start kafka producer
bin/kafka-console-producer.sh --topic test --broker-list localhost:9092

3. Start kafka consumer
bin/kafka-console-consumer.sh --topic test --zookeeper localhost:2181 --from-beginning

Simulation
4. Stop consumer
5. Start Producer
   bin/kafka-console-producer.sh --topic test --broker-list localhost:9092

6. spark-submit  --class KafkaSparkCassandra  target/scala-2.10/SparkCassandraKafka-assembly-1.0.jar


<<<<<<<<<<<<<<<<<<<<
R Code
Run sequentially
1.readdata.R
2.functions1.R
3.functions2.R
4.functions_plot.R
5.main.R



