package models

import scalikejdbc.DBSession

trait BoardIssueOrderRepository {

}

object BoardService {

  def locateBetween(issueWithOrder: IssueWithOrder, bet:Option[BigInt], ween:Option[BigInt])(implicit session:DBSession): Seq[IssueWithOrder] = {
    val bio = issueWithOrder.order
    val affected = reorderInner(bio, bet, ween)
    val issueIds = affected.map(_.issueId)
    IssueWithOrder.findAllByIds(bio.boardId, issueIds)
  }

  private def reorderInner(bio:BoardIssueOrder, bet: Option[BigInt], ween: Option[BigInt])(implicit session: DBSession): Seq[BoardIssueOrder] = {
    val lower: BigInt = bet.getOrElse(BoardIssueOrder.MinOrder - 1)
    val higher: BigInt = ween.getOrElse(BoardIssueOrder.MaxOrder + 1)
    if (lower > higher) {
      throw new IllegalArgumentException("arguments must be 'bet' <= 'ween'.")
    } else {
      val center:BigInt = (higher + lower) / 2
      if ((higher - center).abs <= 1 || (lower - center).abs <= 1) {
        rebalance(bio.copy(arrangeOrder = center))
      } else {
        List(bio.copy(arrangeOrder = center).save())
      }
    }
  }

  import BoardIssueOrder.RearrangeDistance


  private def rebalance(bio: BoardIssueOrder)(implicit session: DBSession): Seq[BoardIssueOrder] = {
    val affected = findAllAffected(AffectedBuffer(bio, List(bio)))
    BoardIssueOrder.batchDelete(affected.bios.map(_.id))

    affected.bios.zipWithIndex.map {case (bio, idx) =>
      val newBoundary = affected.lowerBoundary + RearrangeDistance * (idx + 1)
      println(s"${bio.issueId}:${bio.arrangeOrder}-->$newBoundary")
      BoardIssueOrder.create(bio.boardId, bio.issueId, newBoundary)
    }
  }

  private def findAllAffected(buf:AffectedBuffer)(implicit session:DBSession): AffectedBuffer = {
    val found = BoardIssueOrder.findBetween(buf.boardId, buf.lowerBoundary, buf.higherBoundary)
    val newBuf = buf.update(found)
    print(s"affected(${buf.lowerBoundary}, ${buf.higherBoundary}): [" + newBuf.bios.map(b => s"${b.issueId}:${b.arrangeOrder}").mkString(", ") + "]")
    if (buf.lowerBoundary <= newBuf.lowerBoundary && newBuf.higherBoundary <= buf.higherBoundary) {
      println("*")
      newBuf
    } else {
      println(" --> recursive")
      findAllAffected(newBuf)
    }
  }

  case class AffectedBuffer(target:BoardIssueOrder, bios:Seq[BoardIssueOrder]) {
    val length:Int = bios.length
    val (lowerBoundary, higherBoundary) = {
      val width = RearrangeDistance * (length + 1) / 2
      val center = (bios.head.arrangeOrder + bios.last.arrangeOrder) / 2
      if (center - width < BoardIssueOrder.MinOrder) {
        (BoardIssueOrder.MinOrder, BoardIssueOrder.MinOrder + width * 2)
      } else if (BoardIssueOrder.MaxOrder < center + width) {
        (BoardIssueOrder.MaxOrder - width * 2, BoardIssueOrder.MaxOrder)
      } else {
        (center - width, center + width)
      }
    }

    val boardId:Int = bios.head.boardId

    def update(found:List[BoardIssueOrder]):AffectedBuffer = {
      val left = found.filter(bio => bio.id != target.id && bio.arrangeOrder < target.arrangeOrder)
      val right = found.filter(bio => bio.id != target.id && target.arrangeOrder < bio.arrangeOrder)
      AffectedBuffer(target, left ++ (target :: right))
    }
  }

}
