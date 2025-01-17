import org.apache.log4j.{Logger, Level}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors

/**
  * Created by Manikanta on 1/31/2017.
  */
object kMeansClustering {

  def main(args: Array[String]): Unit = {

    //System.setProperty("hadoop.home.dir","C:\\Users\\Manikanta\\Documents\\UMKC Subjects\\PB\\hadoopforspark");

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc=new SparkContext(sparkConf)

    // Turn off Info Logger for Consolexxx
    Logger.getLogger("org").setLevel(Level.OFF);
    Logger.getLogger("akka").setLevel(Level.OFF);
    // Load and parse the data
    val data = sc.textFile("C:\\Users\\MuthaNagaVenkataSaty\\Desktop\\BDA\\Tutorial and Labs\\CS5542-Tutorial2A-SourceCode\\CS5542-Tutorial2A-SourceCode\\kMeans\\data\\3D_spatial_network.txt")
    val parsedData = data.map(s => Vectors.dense(s.split(',').map(_.toDouble))).cache()

    //Look at how training data is!
    parsedData.foreach(f=>println(f))

    // Cluster the data into two classes using KMeans
    val numClusters = 4
    val numIterations = 20
    val clusters = KMeans.train(parsedData, numClusters, numIterations)

    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = clusters.computeCost(parsedData)
    println("Within Set Sum of Squared Errors = " + WSSSE)

    //Look at how the clusters are in training data by making predictions
    println("Clustering on training data: ")
    clusters.predict(parsedData).zip(parsedData).foreach(f=>println(f._2,f._1))

    // Save and load model
    clusters.save(sc, "data/KMeansModel4")
    val sameModel = KMeansModel.load(sc, "data/KMeansModel4")


  }


}
