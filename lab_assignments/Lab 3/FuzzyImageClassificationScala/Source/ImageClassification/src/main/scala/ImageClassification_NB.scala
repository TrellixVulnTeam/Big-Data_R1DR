import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Naga on 19-09-2016.
  */
object ImageClassification_NB {
  def main(args: Array[String]) {
    val IMAGE_CATEGORIES = Array("Pizza", "Fries", "Hamburger")
    System.setProperty("hadoop.home.dir", "C:\\winutils")
    // Turn off Info Logger for Consolexxx
    Logger.getLogger("org").setLevel(Level.OFF);
    Logger.getLogger("akka").setLevel(Level.OFF);
    val sparkConf = new SparkConf().setAppName("ImageClassification").setMaster("local[*]")
    val sc = new SparkContext(sparkConf)
    val train = sc.textFile("data/train")
    val test = sc.textFile("data/test")
    val parsedData = train.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble.abs)))
    }
    val testData1 = test.map(line => {
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble.abs)))
    })


    val trainingData = parsedData

    val model = NaiveBayes.train(trainingData, lambda = 1.0, modelType = "multinomial")

    val classify1 = testData1.map { line =>
      val prediction = model.predict(line.features)
      (line.label, prediction)
    }

    val prediction1 = classify1.groupBy(_._1).map(f => {
      var fuzzy_Pred = Array(0, 0, 0)
      f._2.foreach(ff => {
        fuzzy_Pred(ff._2.toInt) += 1
      })
      var count = 0.0
      fuzzy_Pred.foreach(f => {
        count += f
      })
      var i = -1
      var maxIndex = 3
      val max = fuzzy_Pred.max
      val pp = fuzzy_Pred.map(f => {
        val p = f * 100 / count
        i = i + 1
        if(f == max)
          maxIndex=i
        (i, p)
      })
      (f._1, pp, maxIndex)
    })
    prediction1.foreach(f => {
      println("\n\n\n" + f._1 + " : " + f._2.mkString(";\n"))
    })
    val y = prediction1.map(f => {
      (f._3.toDouble,f._1 )
    })

    y.collect().foreach(println(_))

    val metrics = new MulticlassMetrics(y)

    println("Accuracy:" + metrics.accuracy)

    println("Confusion Matrix:")
    println(metrics.confusionMatrix)
  }
}
