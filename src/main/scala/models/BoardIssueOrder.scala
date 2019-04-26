package models

import scalikejdbc._

case class BoardIssueOrder(
                            id: Int,
                            boardId: Int,
                            issueId: Int,
                            arrangeOrder: BigInt) {

  def save()(implicit session: DBSession = BoardIssueOrder.autoSession): BoardIssueOrder = BoardIssueOrder.save(this)(session)

  def destroy()(implicit session: DBSession = BoardIssueOrder.autoSession): Int = BoardIssueOrder.destroy(this)(session)

  private def lpad(digit: Int, bd: BigInt): String = {
    val buf = new StringBuilder(bd.toString())
    while (buf.length < digit) {
      buf.insert(0, "0")
    }
    buf.toString()
  }

  lazy val arrangeOrderString: String = lpad(BoardIssueOrder.BigIntDigit, arrangeOrder)
}


object BoardIssueOrder extends SQLSyntaxSupport[BoardIssueOrder] {
  val BigIntDigit = 65
  val MinOrder = BigInt(0)
  val MaxOrder = BigInt((0 until BoardIssueOrder.BigIntDigit).map(_ => '9').mkString(""))
  val DefaultDistance = BigInt(Math.pow(2, 4).toInt)
  val RearrangeDistance = BigInt(Math.pow(2, 2).toInt)

  override val tableName = "board_issue_order"

  override val columns = Seq("id", "board_id", "issue_id", "arrange_order")

  def apply(bio: SyntaxProvider[BoardIssueOrder])(rs: WrappedResultSet): BoardIssueOrder = apply(bio.resultName)(rs)

  def apply(bio: ResultName[BoardIssueOrder])(rs: WrappedResultSet): BoardIssueOrder = new BoardIssueOrder(
    id = rs.get(bio.id),
    boardId = rs.get(bio.boardId),
    issueId = rs.get(bio.issueId),
    arrangeOrder = rs.get(bio.arrangeOrder)
  )

  val bio = BoardIssueOrder.syntax("bio")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[BoardIssueOrder] = {
    withSQL {
      select.from(BoardIssueOrder as bio).where.eq(bio.id, id)
    }.map(BoardIssueOrder(bio.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[BoardIssueOrder] = {
    withSQL(select.from(BoardIssueOrder as bio)).map(BoardIssueOrder(bio.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(BoardIssueOrder as bio)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[BoardIssueOrder] = {
    withSQL {
      select.from(BoardIssueOrder as bio).where.append(where)
    }.map(BoardIssueOrder(bio.resultName)).single.apply()
  }

  def findBetween(boardId:Int, lower:BigInt, higher:BigInt)(implicit session:DBSession = autoSession): List[BoardIssueOrder] =
    findAllBy(sqls.eq(bio.boardId, boardId)
      .and.gt(bio.arrangeOrder, lower)
      .and.lt(bio.arrangeOrder, higher))

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[BoardIssueOrder] = {
    withSQL {
      select.from(BoardIssueOrder as bio).where.append(where).orderBy(bio.arrangeOrder)
    }.map(BoardIssueOrder(bio.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(BoardIssueOrder as bio).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              boardId: Int,
              issueId: Int,
              arrangeOrder: BigInt)(implicit session: DBSession = autoSession): BoardIssueOrder = {
    val generatedKey = withSQL {
      insert.into(BoardIssueOrder).namedValues(
        column.boardId -> boardId,
        column.issueId -> issueId,
        column.arrangeOrder -> arrangeOrder
      )
    }.updateAndReturnGeneratedKey.apply()

    BoardIssueOrder(
      id = generatedKey.toInt,
      boardId = boardId,
      issueId = issueId,
      arrangeOrder = arrangeOrder)
  }

  def batchInsert(entities: collection.Seq[BoardIssueOrder])(implicit session: DBSession = autoSession): List[Int] = {
    val params: collection.Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'boardId -> entity.boardId,
        'issueId -> entity.issueId,
        'arrangeOrder -> entity.arrangeOrder))
    SQL(
      """insert into board_issue_order(
      board_id,
      issue_id,
      arrange_order
    ) values (
      {boardId},
      {issueId},
      {arrangeOrder}
    )""").batchByName(params: _*).apply[List]()
  }

  def save(entity: BoardIssueOrder)(implicit session: DBSession = autoSession): BoardIssueOrder = {
    withSQL {
      update(BoardIssueOrder).set(
        column.id -> entity.id,
        column.boardId -> entity.boardId,
        column.issueId -> entity.issueId,
        column.arrangeOrder -> entity.arrangeOrder
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: BoardIssueOrder)(implicit session: DBSession = autoSession): Int = {
    withSQL {
      delete.from(BoardIssueOrder).where.eq(column.id, entity.id)
    }.update.apply()
  }

  def batchDelete(ids: Seq[Int])(implicit session: DBSession = autoSession): Int = {
    withSQL {
      delete.from(BoardIssueOrder).where.in(column.id, ids)
    }.update.apply()
  }

  def clear()(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(BoardIssueOrder)
    }.update.apply()
  }
}
