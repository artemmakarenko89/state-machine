package artem.statemachine

import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.blaze.server._
import org.http4s.implicits._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      appConfig <- StateMachineConfig.load
      repo = new StateMachineRepositoryImpl(appConfig.db)
      _ <- repo.prepareTables
      controller = new StateMachineController(appConfig.transitions, repo)
      routes = StateMachineService.routes(controller)
      _ <- startHttpServer(appConfig.http, routes)
    } yield ExitCode.Success


  private def startHttpServer(httpConfig: HttpConfig, routes: HttpRoutes[IO]) =
    BlazeServerBuilder[IO]
      .bindHttp(httpConfig.port)
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
}
