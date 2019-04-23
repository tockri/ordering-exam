package models

import scalikejdbc.DBSession

object BoardService {

  def reorder(issueId:Int, bet:Option[BigDecimal], ween:Option[BigDecimal])(implicit session:DBSession): Seq[IssueWithOrder] = {
    val iwo = IssueWithOrder.findById(issueId).getOrElse(throw new IllegalArgumentException())
    val affected = reorderInner(iwo.order.id, bet, ween)
    val issueIds = affected.map(_.issueId)
    IssueWithOrder.findAllByIds(iwo.boardId, issueIds)
  }

  private def reorderInner(boardIssueOrderId: Int, bet: Option[BigDecimal], ween: Option[BigDecimal])(implicit session: DBSession): Seq[BoardIssueOrder] = {
    val lower: BigDecimal = bet.getOrElse(BoardIssueOrder.MinOrder)
    val higher: BigDecimal = ween.getOrElse(BoardIssueOrder.MaxOrder)
    if (lower > higher) {
      throw new IllegalArgumentException("bet must be smaller than ween.")
    } else {
      val bio = BoardIssueOrder.find(boardIssueOrderId).getOrElse(throw new IllegalArgumentException(s"BoardIssueOrderId($boardIssueOrderId) is not found."))
      if (higher - lower <= BigDecimal(1)) {
        rebalance(bio, lower, higher)
      } else {
        val center = (higher + lower) / 2
        bio.copy(arrangeOrder = center).save()
        List(BoardIssueOrder.find(boardIssueOrderId).getOrElse(throw new IllegalStateException()))
      }
    }
  }

  import BoardIssueOrder.RearrangeDistance

  private def max(a:BigDecimal, b:BigDecimal):BigDecimal = if (a < b) b else a

  private def rebalance(bio: BoardIssueOrder, lower: BigDecimal, higher: BigDecimal): Seq[BoardIssueOrder] = {
    val lb = max(BigDecimal(0), lower - RearrangeDistance)
    val ub = higher + RearrangeDistance
    val affected = affectedIssues(bio.boardId, lb, ub).toVector
    var orderValue = max(BigDecimal(0), affected.head.arrangeOrder - RearrangeDistance)
    affected.map { b =>
      orderValue = orderValue + RearrangeDistance
      b.copy(arrangeOrder = orderValue).save()
    }
  }

  private def affectedIssues(boardId:Int, lower:BigDecimal, higher:BigDecimal): Seq[BoardIssueOrder] = {
    if (lower >= higher) {
      Nil
    } else {
      val found = BoardIssueOrder.findBetween(boardId, lower, higher)
      if (found.isEmpty) {
        Nil
      } else {
        affectedIssues(boardId, found.head.arrangeOrder - RearrangeDistance, lower) ++
          found ++
          affectedIssues(boardId, higher, found.last.arrangeOrder + RearrangeDistance)
      }
    }
  }
}
