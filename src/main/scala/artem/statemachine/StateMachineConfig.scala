package artem.statemachine

import artem.statemachine.model.Model._
import cats.effect.IO
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource
import pureconfig.{ConfigReader, ConfigSource}

case class HttpConfig(port: Int)

case class DbConfig(url: String, user: String, password: String)

case class StateMachineConfig(
                               http: HttpConfig,
                               db: DbConfig,
                               transitions: Seq[Transition]
                             )

object StateMachineConfig {
  implicit val TransitionsReader: ConfigReader[Transition] =
    ConfigReader[Array[String]].map {
      case Array(from, to) => Transition(EntityStatus(from), EntityStatus(to))
    }

  def load: IO[StateMachineConfig] = {
    ConfigSource.defaultApplication.loadF[IO, StateMachineConfig]()
  }
}
