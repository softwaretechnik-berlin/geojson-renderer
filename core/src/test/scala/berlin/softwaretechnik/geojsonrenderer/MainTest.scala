package berlin.softwaretechnik.geojsonrenderer

import java.io.ByteArrayOutputStream
import java.nio.file.{Files, Paths}

import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {

  test("Exit status 1 when file not found") {
    intercept[GeoJsonRendererError] {
      Main.run(new Main.Conf(Seq("this-file-does-not-exist.json")))
    }
  }

  test("The output can be set to stdout") {
    val out = new ByteArrayOutputStream()
    Console.withOut(out) {
      Main.run(new Main.Conf(Seq("-o", "-", "examples/berlin-walk.json")))
    }

    assert(out.toString() == Files.readString(Paths.get("examples/berlin-walk.svg")))
  }

  test("The output format can be a png") {
    val out = new ByteArrayOutputStream()
    Console.withOut(out) {
      Main.run(new Main.Conf(Seq("-o", "-", "-f", "png", "examples/berlin-walk.json")))
    }

    val expectedPngSignature = Array(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A).map(_.toByte)
    assert(expectedPngSignature.zip(out.toByteArray).forall { case (a, b) => a == b })
  }

}
