val Http4sVersion = "0.23.7"
val CirceVersion = "0.14.1"
val CatsEffectVersion = "3.2.9"
val PureConfigVersion = "0.17.1"
val DoobieVersion = "1.0.0-RC1"
val LogbackVersion = "1.2.5"

lazy val root = (project in file("."))
  .settings(
    organization := "artem",
    name := "state_machine",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.full),
  )
