import sbt._
object Dependencies {

  object V {
    val ScalaScraper = "2.1.0"
    val Poi = "3.17"
  }

  val scalaScrapper = Seq(
    "net.ruippeixotog" %% "scala-scraper" % V.ScalaScraper
  )
  val poi = Seq(
    "org.apache.poi" % "poi" % V.Poi,
    "org.apache.poi" % "poi-ooxml" % "3.17"
  )
}
