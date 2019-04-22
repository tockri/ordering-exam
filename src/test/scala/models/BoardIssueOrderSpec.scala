package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import scalikejdbc._


class BoardIssueOrderSpec extends Specification {

  "BoardIssueOrder" should {

    val bio = BoardIssueOrder.syntax("bio")

    "find by primary keys" in new AutoRollback {
      val maybeFound = BoardIssueOrder.find(123)
      maybeFound.isDefined should beTrue
    }
    "find by where clauses" in new AutoRollback {
      val maybeFound = BoardIssueOrder.findBy(sqls.eq(bio.id, 123))
      maybeFound.isDefined should beTrue
    }
    "find all records" in new AutoRollback {
      val allResults = BoardIssueOrder.findAll()
      allResults.size should be_>(0)
    }
    "count all records" in new AutoRollback {
      val count = BoardIssueOrder.countAll()
      count should be_>(0L)
    }
    "find all by where clauses" in new AutoRollback {
      val results = BoardIssueOrder.findAllBy(sqls.eq(bio.id, 123))
      results.size should be_>(0)
    }
    "count by where clauses" in new AutoRollback {
      val count = BoardIssueOrder.countBy(sqls.eq(bio.id, 123))
      count should be_>(0L)
    }
    "create new record" in new AutoRollback {
      val created = BoardIssueOrder.create(boardId = 123, issueId = 123, arrangeOrder = new java.math.BigDecimal("1"))
      created should not beNull
    }
    "save a record" in new AutoRollback {
      val entity = BoardIssueOrder.findAll().head
      // TODO modify something
      val modified = entity
      val updated = BoardIssueOrder.save(modified)
      updated should not equalTo(entity)
    }
    "destroy a record" in new AutoRollback {
      val entity = BoardIssueOrder.findAll().head
      val deleted = BoardIssueOrder.destroy(entity) == 1
      deleted should beTrue
      val shouldBeNone = BoardIssueOrder.find(123)
      shouldBeNone.isDefined should beFalse
    }
    "perform batch insert" in new AutoRollback {
      val entities = BoardIssueOrder.findAll()
      entities.foreach(e => BoardIssueOrder.destroy(e))
      val batchInserted = BoardIssueOrder.batchInsert(entities)
      batchInserted.size should be_>(0)
    }
  }

}
