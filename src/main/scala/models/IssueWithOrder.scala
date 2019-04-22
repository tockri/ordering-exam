package models

case class IssueWithOrder(issue:Issue, order:BoardIssueOrder) {
  val id = issue.id
  val subject = issue.subject
  val arrangeOrder = order.arrangeOrder
  val boardId = order.boardId
}

object IssueWithOrder {

}
