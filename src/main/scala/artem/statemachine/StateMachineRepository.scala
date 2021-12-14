package artem.statemachine

import artem.statemachine.model.EntityAlreadyExists
import artem.statemachine.model.Model._
import cats.effect.IO
import doobie.Transactor
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.util.transactor.Transactor.Aux

trait StateMachineRepository {
  def prepareTables: IO[Unit]

  def createEntity(name: EntityName, status: EntityStatus): IO[Entity]

  def findEntity(id: EntityId): IO[Option[Entity]]

  def updateStatus(id: EntityId, from: EntityStatus, to: EntityStatus): IO[Boolean]

  def insertHistory(id: EntityId, from: EntityStatus, to: EntityStatus): IO[Boolean]

  def getHistory(id: EntityId): IO[Seq[Transition]]
}

class StateMachineRepositoryImpl(dbConfig: DbConfig) extends StateMachineRepository {
  private val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    dbConfig.url,
    dbConfig.user,
    dbConfig.password
  )

  override def createEntity(name: EntityName, status: EntityStatus): IO[Entity] = {
    val resultEntity = for {
      _ <- sql"insert into entity (name, status) values ($name, $status)".update.run
      id <- sql"select lastval()".query[Long].unique
      entity <- sql"select id, name, status from entity where id = $id".query[Entity].unique
    } yield entity

    resultEntity.transact(xa).exceptSomeSqlState {
      case sqlstate.class23.UNIQUE_VIOLATION => IO.raiseError[Entity](EntityAlreadyExists(name))
    }
  }

  override def findEntity(id: EntityId): IO[Option[Entity]] =
    sql"select id, name, status from entity where id = $id"
      .query[Entity]
      .option
      .transact(xa)

  override def updateStatus(id: EntityId, from: EntityStatus, to: EntityStatus): IO[Boolean] =
    sql"update entity set status = $to where id = $id and status = $from"
      .update
      .run
      .transact(xa)
      .map(_ == 1)

  def insertHistory(id: EntityId, from: EntityStatus, to: EntityStatus): IO[Boolean] =
    sql"insert into history (id, fromStatus, toStatus) values ($id, $from, $to)"
      .update
      .run
      .transact(xa)
      .map(_ == 1)


  override def getHistory(id: EntityId): IO[Seq[Transition]] =
    sql"select fromStatus, toStatus from history where id = $id"
      .query[Transition]
      .stream
      .compile
      .toList
      .transact(xa)

  override def prepareTables: IO[Unit] =
    for {
      _ <- prepareEntityTable
      _ <- prepareHistoryTable
    } yield ()

  private def prepareEntityTable: IO[Unit] =
    sql"""
    CREATE TABLE IF NOT EXISTS entity (
      id      SERIAL,
      name    VARCHAR NOT NULL UNIQUE,
      status  VARCHAR
    )
  """.update.run.transact(xa).void

  private def prepareHistoryTable: IO[Unit] =
    sql"""
    CREATE TABLE IF NOT EXISTS history (
      id            INTEGER,
      fromStatus    VARCHAR,
      toStatus      VARCHAR
    )
  """.update.run.transact(xa).void
}
