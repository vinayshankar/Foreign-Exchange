package main.scala;

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.rdd.RDD
import java.util.Calendar

import com.datastax.spark.connector._

object RandomForestScala {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Foreign-Exchange-Big-Data-Analytics")
    conf.setMaster("local[1]")
    conf.set("spark.cassandra.connection.host", "localhost")
    val sc = new SparkContext(conf)

    val numberOfTrees = 100

    //set up cassandra connnection
    val trainingDataTable = sc.cassandraTable("trainingdata", "records")
    val features = Array[String]("askprice", "bidprice", 
        "avgaskprice", "maxaskprice", "minaskprice",
        "avgbidprice", "maxbidprice", "minbidprice",
        "avgspread", "maxspread", "minspread")
    val feature_index = Array[Int](0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    
    //convert the data to rdd format
    val trainData: RDD[LabeledPoint] = trainingDataTable.map { row =>
      {
        val v: Array[Double] = new Array[Double](11)
        val label = (row.get[String]("askdirectionality")).toDouble
        for (i <- 0 to 10) {
          v(i) = (row.get[String](features(i))).toDouble
        }
        (LabeledPoint(label, Vectors.sparse(11, feature_index, v)))
      }
    }

    //Train a RandomForest model.
    val numClasses = 2 //0 or 1
    val categoricalFeaturesInfo = Map[Int, Int]()

    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val impurity = "gini"
    val maxDepth = 5 // feature number
    val maxBins = 32

    val model = RandomForest.trainClassifier(trainData, numClasses, categoricalFeaturesInfo,
      numberOfTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    //get and convert data in test set
    val testDataTable = sc.cassandraTable("testingrecords", "records")
    val testData: RDD[LabeledPoint] = testDataTable.map { row =>
      {
        val v: Array[Double] = new Array[Double](11)
        val label = (row.get[String]("askdirectionality")).toDouble
        for (i <- 0 to 10) {
          v(i) = (row.get[String](features(i))).toDouble
        }
        (LabeledPoint(label, Vectors.sparse(11, feature_index, v)))
      }
    }

    //Evaluate model on test instances and compute test error, precision, recall
    val labelAndPrediction = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val testErrors = labelAndPrediction.filter(r => r._1 != r._2).count.toDouble / testData.count()
    val testRecall = labelAndPrediction.filter(r => r._1 == 0 && r._2 == 0).count.toDouble / labelAndPrediction.filter(r => r._1 == 0).count.toDouble
    println("Test Error = " + testErrors)
    println("Test Precision = " + (1 - testErrors))

    println("Test Recall = " + testRecall)
    println("Random Forest:\n" + model.toDebugString)

    //save results into cassandra
    val collection = sc.parallelize(Seq((Calendar.getInstance().getTime(), (1 - testErrors), testRecall)))
    collection.saveToCassandra("randomforestscala", "results", SomeColumns("time", "precision", "recall"))

    // Save and load model
    model.save(sc, "myModelPath")
    val sameModel = RandomForestModel.load(sc, "myModelPath")
  }
}
