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
      } else if (args(0) == "test") {
        test()
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
      val count = 10
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
    println(s"${message}${io.subject}:${io.order.arrangeOrderString}")
  }

  private def p(io:IssueWithOrder):Unit = p(io, "")

  def test(): Unit = {
    withBoard { implicit session => board =>
      val issues = IssueWithOrder.findAll(board.id).toVector
      println("Before operation:")
      issues.foreach(p)
      println(s"Operation: move '${issues.last.subject}' into between '${issues(0).subject}' and '${issues(1).subject}'")
      val effected = BoardService.reorder(issues.last.id, Some(issues(0).arrangeOrder), Some(issues(1).arrangeOrder))
      println("Effected Issues:")
      effected.foreach(p)
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


