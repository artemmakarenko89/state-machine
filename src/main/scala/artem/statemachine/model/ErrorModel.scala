package artem.statemachine.model

import artem.statemachine.model.Model._

sealed trait CustomError extends Exception

case class EntityAlreadyExists(name: EntityName) extends CustomError

case class EntityNotFound(id: EntityId) extends CustomError

case class UnknownStatus(status: EntityStatus) extends CustomError

case class IllegalTransition(from: EntityStatus, to: EntityStatus) extends CustomError
