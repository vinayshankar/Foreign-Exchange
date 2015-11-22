name := "Foreign Exchange Scala"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.5.1"
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "1.5.0-M2"