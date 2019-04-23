package models

import scalikejdbc._

case class Issue(
                  id: Int,
                  subject: String,
                  projectId: Int) {

  def save()(implicit session: DBSession = Issue.autoSession): Issue = Issue.save(this)(session)

  def destroy()(implicit session: DBSession = Issue.autoSession): Int = Issue.destroy(this)(session)

}


object Issue extends SQLSyntaxSupport[Issue] {

  override val tableName = "issue"

  override val columns = Seq("id", "subject", "project_id")

  def apply(i: SyntaxProvider[Issue])(rs: WrappedResultSet): Issue = apply(i.resultName)(rs)

  def apply(i: ResultName[Issue])(rs: WrappedResultSet): Issue = new Issue(
    id = rs.get(i.id),
    subject = rs.get(i.subject),
    projectId = rs.get(i.projectId)
  )

  val i = Issue.syntax("i")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Issue] = {
    withSQL {
      select.from(Issue as i).where.eq(i.id, id)
    }.map(Issue(i.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Issue] = {
    withSQL(select.from(Issue as i)).map(Issue(i.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Issue as i)).map(rs => rs.long(1)).single.apply().get
  }

  def findAllByIds(ids:Seq[Int])(implicit session: DBSession = autoSession): List[Issue] = {
    findAllBy(sqls.in(i.id, ids))
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Issue] = {
    withSQL {
      select.from(Issue as i).where.append(where)
    }.map(Issue(i.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Issue] = {
    withSQL {
      select.from(Issue as i).where.append(where)
    }.map(Issue(i.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Issue as i).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              subject: String,
              projectId: Int)(implicit session: DBSession = autoSession): Issue = {
    val generatedKey = withSQL {
      insert.into(Issue).namedValues(
        column.subject -> subject,
        column.projectId -> projectId
      )
    }.updateAndReturnGeneratedKey.apply()

    Issue(
      id = generatedKey.toInt,
      subject = subject,
      projectId = projectId)
  }

  def batchInsert(entities: collection.Seq[Issue])(implicit session: DBSession = autoSession): List[Int] = {
    val params: collection.Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'subject -> entity.subject,
        'projectId -> entity.projectId))
    SQL(
      """insert into issue(
      subject,
      project_id
    ) values (
      {subject},
      {projectId}
    )""").batchByName(params: _*).apply[List]()
  }

  def save(entity: Issue)(implicit session: DBSession = autoSession): Issue = {
    withSQL {
      update(Issue).set(
        column.id -> entity.id,
        column.subject -> entity.subject,
        column.projectId -> entity.projectId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Issue)(implicit session: DBSession = autoSession): Int = {
    withSQL {
      delete.from(Issue).where.eq(column.id, entity.id)
    }.update.apply()
  }

  def clear()(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(Issue)
    }.update.apply()
  }
}
