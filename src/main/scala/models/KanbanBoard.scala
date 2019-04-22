package models

import scalikejdbc._

case class KanbanBoard(
                        id: Int,
                        projectId: Int) {

  def save()(implicit session: DBSession = KanbanBoard.autoSession): KanbanBoard = KanbanBoard.save(this)(session)

  def destroy()(implicit session: DBSession = KanbanBoard.autoSession): Int = KanbanBoard.destroy(this)(session)

}


object KanbanBoard extends SQLSyntaxSupport[KanbanBoard] {

  override val tableName = "kanban_board"

  override val columns = Seq("id", "project_id")

  def apply(kb: SyntaxProvider[KanbanBoard])(rs: WrappedResultSet): KanbanBoard = apply(kb.resultName)(rs)

  def apply(kb: ResultName[KanbanBoard])(rs: WrappedResultSet): KanbanBoard = new KanbanBoard(
    id = rs.get(kb.id),
    projectId = rs.get(kb.projectId)
  )

  val kb = KanbanBoard.syntax("kb")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[KanbanBoard] = {
    withSQL {
      select.from(KanbanBoard as kb).where.eq(kb.id, id)
    }.map(KanbanBoard(kb.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[KanbanBoard] = {
    withSQL(select.from(KanbanBoard as kb)).map(KanbanBoard(kb.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(KanbanBoard as kb)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[KanbanBoard] = {
    withSQL {
      select.from(KanbanBoard as kb).where.append(where)
    }.map(KanbanBoard(kb.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[KanbanBoard] = {
    withSQL {
      select.from(KanbanBoard as kb).where.append(where)
    }.map(KanbanBoard(kb.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(KanbanBoard as kb).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              projectId: Int)(implicit session: DBSession = autoSession): KanbanBoard = {
    val generatedKey = withSQL {
      insert.into(KanbanBoard).namedValues(
        column.projectId -> projectId
      )
    }.updateAndReturnGeneratedKey.apply()

    KanbanBoard(
      id = generatedKey.toInt,
      projectId = projectId)
  }

  def batchInsert(entities: collection.Seq[KanbanBoard])(implicit session: DBSession = autoSession): List[Int] = {
    val params: collection.Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'projectId -> entity.projectId))
    SQL(
      """insert into kanban_board(
      project_id
    ) values (
      {projectId}
    )""").batchByName(params: _*).apply[List]()
  }

  def save(entity: KanbanBoard)(implicit session: DBSession = autoSession): KanbanBoard = {
    withSQL {
      update(KanbanBoard).set(
        column.id -> entity.id,
        column.projectId -> entity.projectId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: KanbanBoard)(implicit session: DBSession = autoSession): Int = {
    withSQL {
      delete.from(KanbanBoard).where.eq(column.id, entity.id)
    }.update.apply()
  }

  def clear()(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(KanbanBoard)
    }.update.apply()
  }

}
