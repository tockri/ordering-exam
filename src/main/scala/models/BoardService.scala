package models

import scalikejdbc.DBSession

object BoardService {

  def locateBetween(boardIssueOrderId:Int, bet:Option[BigDecimal], ween:Option[BigDecimal])(implicit session:DBSession): Seq[IssueWithOrder] = {
    val bio = BoardIssueOrder.find(boardIssueOrderId).getOrElse(throw new IllegalArgumentException())
    val affected = reorderInner(bio, bet, ween)
    val issueIds = affected.map(_.issueId)
    IssueWithOrder.findAllByIds(bio.boardId, issueIds)
  }

  private def reorderInner(bio:BoardIssueOrder, bet: Option[BigDecimal], ween: Option[BigDecimal])(implicit session: DBSession): Seq[BoardIssueOrder] = {
    val lower: BigDecimal = bet.getOrElse(BoardIssueOrder.MinOrder)
    val higher: BigDecimal = ween.getOrElse(BoardIssueOrder.MaxOrder)
    if (lower > higher) {
      throw new IllegalArgumentException("arguments must be 'bet' <= 'ween'.")
    } else {
      if (higher - lower <= BigDecimal(1)) {
        rebalance(Vector(bio), lower, higher)
      } else {
        val center = (higher + lower) / 2
        List(bio.copy(arrangeOrder = center).save())
      }
    }
  }

  import BoardIssueOrder.RearrangeDistance

  private def max(a:BigDecimal, b:BigDecimal):BigDecimal = if (a < b) b else a
  private def min(a:BigDecimal, b:BigDecimal):BigDecimal = if (a > b) b else a

  private def rebalance(bios: Vector[BoardIssueOrder], lower: BigDecimal, higher: BigDecimal)(implicit session: DBSession): Seq[BoardIssueOrder] = {

  }

}
