package models

import scalikejdbc.DBSession

object BoardService {

  def reorder(issueId:Int, bet:Option[BigDecimal], ween:Option[BigDecimal])(implicit session:DBSession): Seq[IssueWithOrder] = {
    val lower:BigDecimal = bet.getOrElse(BoardIssueOrder.MinOrder)
    val higher:BigDecimal = ween.getOrElse(BoardIssueOrder.MaxOrder)
    if (lower > higher) {
      throw new IllegalArgumentException("bet must be smaller than ween.")
    } else {
      val issueWithOrder = IssueWithOrder.findById(issueId).getOrElse(throw new IllegalArgumentException(s"IssueId(${issueId}) is not found."))
      if (higher - lower <= BigDecimal(1)) {
        rearrange(issueWithOrder, lower, higher)
      } else {
        val center = (higher + lower) / 2
        issueWithOrder.order.copy(arrangeOrder = center).save()
        List(IssueWithOrder.findById(issueId).getOrElse(throw new IllegalStateException()))
      }
    }
  }

  private def rearrange(issue:IssueWithOrder, lower:BigDecimal, higher:BigDecimal): Seq[IssueWithOrder] = {
    ???
  }
}
