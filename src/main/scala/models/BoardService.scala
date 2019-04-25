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
      val center = (higher + lower) / 2
      if (higher - lower <= BigDecimal(1)) {
        rebalance(bio, center)
      } else {
        List(bio.copy(arrangeOrder = center).save())
      }
    }
  }

  import BoardIssueOrder.RearrangeDistance


  private def rebalance(bio: BoardIssueOrder, center: BigDecimal)(implicit session: DBSession): Seq[BoardIssueOrder] = {
    val range = findAllAffected(AffectedBuffer(bio, List(bio)))
    range.bios.zipWithIndex.map {case (bio, idx) =>
      bio.copy(arrangeOrder = range.lowerBoundary + RearrangeDistance * (idx + 1)).save()
    }
  }

  private def findAllAffected(buf:AffectedBuffer)(implicit session:DBSession): AffectedBuffer = {
    val found = BoardIssueOrder.findBetween(buf.boardId, buf.lowerBoundary, buf.higherBoundary)
    val newBuf = buf.update(found)
    if (buf.needsExpand(newBuf)) {
      findAllAffected(newBuf)
    } else {
      newBuf
    }
  }


  case class DecimalRange(lower:BigDecimal, higher:BigDecimal) {
    private def max(a:BigDecimal, b:BigDecimal):BigDecimal = if (a < b) b else a
    private def min(a:BigDecimal, b:BigDecimal):BigDecimal = if (a > b) b else a
    def union(other: DecimalRange): DecimalRange = {
      DecimalRange(min(lower, other.lower), max(higher, other.higher))
    }
  }
  object DecimalRange {
    def empty(center:BigDecimal) = DecimalRange(center, center)
  }

  case class AffectedBuffer(moving:BoardIssueOrder, bios:Seq[BoardIssueOrder]) {
    val length:Int = bios.length
    val (lowerBoundary, higherBoundary) = {
      val width = (length + 1) * RearrangeDistance / 2
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

    def needsExpand(found:AffectedBuffer):Boolean =
      lowerBoundary <= found.lowerBoundary && found.higherBoundary <= higherBoundary

    def update(found:List[BoardIssueOrder]):AffectedBuffer =
      AffectedBuffer(moving, found.filter(bio => bio.id != moving.id && bio.arrangeOrder < moving.arrangeOrder) ++
        (moving :: found.filter(bio => bio.id != moving.id && moving.arrangeOrder < bio.arrangeOrder)))
  }

}
