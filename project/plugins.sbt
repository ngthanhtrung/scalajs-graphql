addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.20")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.14")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin(
  "ch.epfl.scala" % "sbt-scalajs-bundler" % "0.9.0"
    exclude ("org.scala-js", "sbt-scalajs")
)

scalafmtShowDiff in scalafmt in ThisBuild := true
