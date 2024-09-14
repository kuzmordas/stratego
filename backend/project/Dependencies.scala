import sbt._

object Dependencies {
  val zio            = "dev.zio" %% "zio"             % "2.1.4"
  val zioHttp        = "dev.zio" %% "zio-http"        % "3.0.0-RC9"
  val zioJson        = "dev.zio" %% "zio-json"        % "0.7.1"
  val zioSchema      = "dev.zio" %% "zio-schema"      % "1.3.0"
  val zioSchemJson   = "dev.zio" %% "zio-schema-json" % "1.3.0"
  val zioMagnolia    = "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC14"
  val zioTypesafe    = "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC14"
  val tofuZioLogging = "tf.tofu" %% "tofu-zio2-logging" % "0.12.0.1"
  val tofuLogback    = "tf.tofu" %% "tofu-logging-logstash-logback" % "0.12.0.1"
}
