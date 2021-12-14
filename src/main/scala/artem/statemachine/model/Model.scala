package artem.statemachine.model

object Model {
  final case class EntityName(name: String) extends AnyVal

  final case class EntityId(id: Long) extends AnyVal

  final case class EntityStatus(status: String) extends AnyVal

  final case class Entity(id: EntityId, name: EntityName, status: EntityStatus)

  final case class Transition(from: EntityStatus, to: EntityStatus)
}
