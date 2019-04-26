package example

import models.{BoardIssueOrder, BoardService, Issue, IssueWithOrder, KanbanBoard}
import scalikejdbc.config.DBs
import scalikejdbc.{DB, DBSession}

object OrderExam {
  def main(args: Array[String]): Unit = {
    if (args.length > 0) {
      DBs.setupAll()
      if (args(0) == "create") {
        create()
      } else if (args(0) == "test" && args.length > 2) {
        val from = args(1).toInt
        val to = args(2).toInt
        test(from, to)
      } else if (args(0) == "clear") {
        clear()
      } else {
        println(s"Unknown command: ${args(0)}")
      }
    }
  }


  def create(): Unit = {
    clear()
    DB.localTx { implicit session =>
      val board = KanbanBoard.create(1).save()
      val count = 6
      val distance = BigDecimal(10)
      var order = BoardIssueOrder.MinOrder + distance
      (1 to count).foreach(i => {
        val issue = Issue.create(s"Issue ($i)", board.id)
        val biOrder = BoardIssueOrder.create(board.id, issue.id, order)
        p(IssueWithOrder(issue, biOrder), "Created: ")
        order = order + distance
      })
    }
  }

  private def withBoard[T](func: DBSession => KanbanBoard => T): T = {
    DB.localTx { implicit session =>
      val board = KanbanBoard.findAll().head
      func(session)(board)
    }
  }

  private def p(io:IssueWithOrder, message:String):Unit = {
    println(s"${message}${io.subject}:${io.order.arrangeOrder}")
  }

  private def p(io:IssueWithOrder):Unit = p(io, "")

  def test(from:Int, to:Int): Unit = {
    withBoard { implicit session => board =>
      val before = IssueWithOrder.findAll(board.id).toVector
      println("###################################")
      println("--- Operation: ---")
      before.zipWithIndex.foreach{case (io, idx) =>
        if (idx == to) {
          println("--->")
        }
        p(io, if (idx == from) " *<-" else "    ")
      }
      val bet = if (to > 0) Some(before(to - 1).arrangeOrder) else None
      val ween = if (to < before.length) Some(before(to).arrangeOrder) else None
      val affected = BoardService.locateBetween(before(from), bet, ween)
      println("--- Affected Issues: ---")
      affected.foreach(p)
      val after = IssueWithOrder.findAll(board.id)
      println("--- After operation: ---")
      after.foreach(p)
    }
  }

  def clear(): Unit = {
    DB.localTx { implicit session =>
      BoardIssueOrder.clear()
      Issue.clear()
      KanbanBoard.clear()
      println("All clear.")
    }
  }
}


