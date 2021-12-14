package artem.statemachine

import artem.statemachine.model.Model._
import artem.statemachine.model._
import cats.effect.IO
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s.Method.POST
import org.http4s.Uri.Path.Root
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.io._
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

object StateMachineService {
  implicit val EntityIdEncoder: Encoder[EntityId] = Encoder.encodeLong.contramap(_.id)
  implicit val EntityNameEncoder: Encoder[EntityName] = Encoder.encodeString.contramap(_.name)
  implicit val EntityStatusEncoder: Encoder[EntityStatus] = Encoder.encodeString.contramap(_.status)

  implicit val EntityNameDecoder: EntityDecoder[IO, EntityName] = jsonOf[IO, EntityName]
  implicit val EntityEncoder: EntityEncoder[IO, Entity] = jsonEncoderOf[IO, Entity]
  implicit val TransitionEncoder: EntityEncoder[IO, Seq[Transition]] = jsonEncoderOf[IO, Seq[Transition]]

  def routes(controller: StateMachineController): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req@POST -> Root / "entities" =>
        for {
          entityName <- req.as[EntityName]
          resp <- Ok(controller.createEntity(entityName))
            .handleErrorWith {
              case EntityAlreadyExists(_) => Conflict(s"Entity already exists: [${entityName.name}]")
            }
        } yield resp

      case POST -> Root / "entities" / LongVar(entityId) / "transit" / newStatus =>
        Ok(controller.transitEntity(EntityId(entityId), EntityStatus(newStatus))).handleErrorWith {
          case EntityNotFound(_) => NotFound(s"Entity not found: [$entityId]")
          case UnknownStatus(status) => BadRequest(s"Unknown status: [${status.status}]")
          case IllegalTransition(from, to) => NotAcceptable(s"Transition is not allowed: [${from.status} -> ${to.status}]")
        }

      case GET -> Root / "entities" / LongVar(entityId) / "history" =>
        Ok(controller.history(EntityId(entityId)))
    }
}
