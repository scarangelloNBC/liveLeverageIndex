import java.util.Calendar

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Node
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import java.io.File

import org.apache.poi.ss.usermodel.WorkbookFactory

import scala.collection.mutable.ArrayBuffer
import scala.xml.{NodeSeq, XML}
object Utils {

  val file = new File("leverageTable.xlsx")
  val levTable = WorkbookFactory.create(file)
  def yearMonthAndDay: Tuple3[String, String, String] = {
    val cal = Calendar.getInstance
    val monthNum = "%02d".format(cal.get(Calendar.MONTH) + 1)
    val dayNum = "%02d".format(cal.get(Calendar.DAY_OF_MONTH))
    val yearNum = cal.get(Calendar.YEAR).toString
    (monthNum, dayNum, yearNum)
  }

  def todaysGamesLinks(date: Tuple3[String, String, String]): Seq[String]  = {
    val urlString = "http://gd2.mlb.com/components/game/mlb/year_" + date._3 + "/month_" + date._1 + "/day_" + date._2
    val browser = JsoupBrowser()
    val doc = browser.get(urlString)
    val allLinks = doc >> elementList("a")
    val gameIds = allLinks.flatMap(_ >> elementList("a").map(_ >> allText("a"))).filter(_.contains("gid"))
    val gameLinks = ArrayBuffer[String]()
    for(elem <- gameIds) {
      gameLinks += urlString + "/" + elem
    }
    gameLinks
  }

  def getGameInfo(xmlLink: String) = {
    try {
      val xmlTeamNames = XML.load(xmlLink + "game.xml")
      val homeName = (xmlTeamNames \\ "@name_full").head
      val awayName = (xmlTeamNames \\ "@name_full")(1)
      val xmlDoc = XML.load(xmlLink + "plays.xml")
      val bases = xmlDoc \\ "@bnum"
      val outs = xmlDoc \\ "@o"
      val inning  = xmlDoc \\ "@inning"
      val runDif = (xmlDoc \\ "@hr").head.toString.toInt - (xmlDoc \\ "@ar").head.toString.toInt
      val gameStatus = xmlDoc \\ "@status"
      val inningStatus = (xmlDoc \\ "@inning_state").toString
      if((inningStatus == "Top" || inningStatus == "Bottom") && gameStatus.toString == "In Progress") {
        val info = "Home: " + homeName + " | Away: " + awayName
        println(info)
        val bnum = bases.filter(_.toString.toInt < 4).sortBy(_.toString.toInt)
        if(bnum.isEmpty) {
          val index = indexVal(0, outs.toString.toInt, inning.toString.toInt, inningStatus, runDif.toString.toInt)
          println(index + "\n")
          (info, index)
        }
        else {
          val index = indexVal(bnum.toString.toInt, outs.toString.toInt, inning.toString.toInt, inningStatus, runDif.toString.toInt)
          println(index + "\n")
          (info, index)
        }
      }
    }
    catch {
      case _: Throwable =>
    }
  }

  def indexVal(bases: Int, outs: Int, inning: Int, status: String, runDif: Int): Double = {
    val col = 6 + runDif
    val outTranslated = outs * 9
    val basesTranslated = basesValue(bases)
    val sheetnum = inning - 1
    val sheet = levTable.getSheetAt(sheetnum)
    if(status == "Top")
      {
        val rowVal = outTranslated + basesTranslated
        sheet.getRow(rowVal).getCell(col).getNumericCellValue
      }
    else {
      val rowVal = outTranslated + basesTranslated + 29
      sheet.getRow(rowVal).getCell(col).getNumericCellValue
    }
  }

  def basesValue(baseStatus: Int): Int = {
    if(baseStatus == 0 || baseStatus == 1 || baseStatus == 2 || baseStatus == 3) {
      baseStatus
    }
    else if(baseStatus == 12) {
      4
    }
    else if(baseStatus == 13) {
      5
    }
    else if(baseStatus == 23) {
      6
    }
    else {
      7
    }
  }
}
