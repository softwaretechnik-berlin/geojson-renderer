package berlin.softwaretechnik.geojsonrenderer

import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {

  test("Exit status 1 when file not found") {
    intercept[GeoJsonRendererError] {
      Main.run(new Main.Conf(Array("this-file-does-not-exist.json")))
    }
  }

}
