package models

import scalikejdbc._

case class IssueWithOrder(issue:Issue, order:BoardIssueOrder) {
  val id = issue.id
  val subject = issue.subject
  val arrangeOrder = order.arrangeOrder
  val boardId = order.boardId
}

object IssueWithOrder {

  def entity(i: ResultName[Issue], bio: ResultName[BoardIssueOrder])(rs: WrappedResultSet): IssueWithOrder = {
    IssueWithOrder(Issue.apply(i)(rs), BoardIssueOrder.apply(bio)(rs))
  }

  def findAll(boardId:Int)(implicit session:DBSession):Seq[IssueWithOrder] = {
    import models.Issue.i
    import models.BoardIssueOrder.bio
    withSQL {
      select.from(Issue as i)
        .join(BoardIssueOrder as bio).on(bio.issueId, i.id)
        .where.eq(bio.boardId, boardId)
        .orderBy(bio.arrangeOrder)
    }.map(entity(i.resultName, bio.resultName)).list.apply()
  }

  def findById(issueId: Int)(implicit session:DBSession):Option[IssueWithOrder] = {
    import models.Issue.i
    import models.BoardIssueOrder.bio
    withSQL {
      select.from(Issue as i)
        .join(BoardIssueOrder as bio).on(bio.issueId, i.id)
        .where.eq(i.id, issueId)
    }.map(entity(i.resultName, bio.resultName)).single.apply()
  }
}
