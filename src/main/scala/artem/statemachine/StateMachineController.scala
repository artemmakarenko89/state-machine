package artem.statemachine

import artem.statemachine.model.Model._
import artem.statemachine.model.{EntityNotFound, IllegalTransition, UnknownStatus}
import cats.effect.IO

class StateMachineController(availableTransitions: Seq[Transition],
                             repository: StateMachineRepository
                            ) {
  private val initStatus = availableTransitions.head.from
  private val transitionMap = availableTransitions.groupMap(_.from)(_.to)
  private val allStatuses = availableTransitions.flatMap(tr => Seq(tr.from, tr.to))

  def createEntity(name: EntityName): IO[Entity] = repository.createEntity(name, initStatus)

  def validateTransition(from: EntityStatus, to: EntityStatus): IO[Unit] = {
    if (!allStatuses.contains(to))
      IO.raiseError(UnknownStatus(to))
    else {
      transitionMap.get(from) match {
        case Some(statuses) if statuses.contains(to) => IO.unit
        case _ => IO.raiseError(IllegalTransition(from, to))
      }
    }
  }

  def transitEntity(id: EntityId, newStatus: EntityStatus): IO[Entity] = {
    for {
      entityOpt <- repository.findEntity(id)
      entity <- IO.fromOption(entityOpt)(EntityNotFound(id))
      resEntity <-
        if (entity.status == newStatus) IO.pure(entity)
        else {
          for {
            _ <- validateTransition(entity.status, newStatus)
            _ <- repository.updateStatus(id, entity.status, newStatus)
            _ <- repository.insertHistory(id, entity.status, newStatus)
          } yield entity.copy(status = newStatus)
        }
    } yield resEntity
  }

  def history(id: EntityId): IO[Seq[Transition]] = repository.getHistory(id)
}
