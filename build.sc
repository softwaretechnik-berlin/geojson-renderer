import mill._
import mill.api.Loose
import mill.define.Target
import mill.scalalib._

object core extends SbtModule {
  def scalaVersion = "2.12.4"

  override def ivyDeps: Target[Loose.Agg[Dep]] =

    Agg(
      ivy"org.scala-lang.modules::scala-xml::1.2.0",
      ivy"org.rogach::scallop:3.3.2",
      ivy"com.lihaoyi::upickle:0.9.5",
      ivy"org.apache.xmlgraphics:batik-transcoder:1.12",
      ivy"org.apache.xmlgraphics:batik-codec:1.12"
    )

  object test extends Tests {
    def ivyDeps = Agg(ivy"org.scalatest::scalatest::3.0.8")
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

  override def mainClass = Some("org.buildobjects.mapr.Main")
}
