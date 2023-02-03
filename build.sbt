ThisBuild / scalaVersion := "2.13.1"

Compile / run / mainClass := Option("main.Main")

libraryDependencies ++= Seq(
  "dev.zio"       %% "zio"            % "2.0.4",
  "dev.zio"       %% "zio-http"       % "0.0.3",
  "dev.zio"       %% "zio-json"       % "0.4.2",
  "dev.zio"       %% "zio-actors"     % "0.1.0",
  "io.getquill"   %% "quill-jdbc-zio" % "4.6.0",
  "io.getquill"   %% "quill-jdbc"     % "4.6.0",
  "org.postgresql" % "postgresql"     % "42.2.8",
  "io.estatico"   %% "newtype"        % "0.4.4"
)

scalacOptions ++= Seq(
  "-Ymacro-annotations",
)
