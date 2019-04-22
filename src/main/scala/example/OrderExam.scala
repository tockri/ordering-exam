package example

import models.{BoardIssueOrder, Issue, KanbanBoard}
import scalikejdbc.DB
import scalikejdbc.config.DBs

object OrderExam {
  def main(args: Array[String]): Unit = {
    if (args.length > 0) {
      DBs.setupAll()
      if (args(0) == "create") {
        create()
      } else if (args(0) == "test") {
        test()
      } else if (args(0) == "clear") {
        clear()
      } else {
        println(s"Unknown command: ${args(0)}")
      }
    }
  }

  private def value999(digit:Int):Array[Char] = (0 until digit).map(_ => '9').toArray

  private def lpad(digit:Int, bd:BigDecimal):String = {
    val buf = new StringBuilder(bd.toString())
    while (buf.length < digit) {
      buf.insert(0, "0")
    }
    buf.toString()
  }

  private val ARRANGE_ORDER_DIGIT = 65

  def create(): Unit = {
    clear()
    DB.localTx {implicit session =>
      val board = KanbanBoard.create(1).save()
      val min = BigDecimal(0)
      val max = BigDecimal(value999(ARRANGE_ORDER_DIGIT))
      val count = 10
      val distance = ((max - min) / (count + 1)).setScale(0, BigDecimal.RoundingMode.CEILING)
      var order = min + distance
      (1 to count).foreach(i => {
        val issue = Issue.create(s"Issue ($i)", board.id).save()
        val biOrder = BoardIssueOrder.create(board.id, issue.id, order).save()
        println(s"Issue created: $issue, Order: ${lpad(ARRANGE_ORDER_DIGIT, biOrder.arrangeOrder)}")
        order = order + distance
      })
    }
  }

  def test(): Unit = {

  }

  def clear(): Unit = {
    DB.localTx {implicit session =>
      BoardIssueOrder.clear()
      Issue.clear()
      KanbanBoard.clear()
      println("All clear.")
    }
  }
}


