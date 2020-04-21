package berlin.softwaretechnik.geojsonrenderer

import org.scalatest.funsuite.AnyFunSuite

class BoundingBoxTest extends AnyFunSuite {

  Seq(
    (-20, 0, 20),
    (-170, 0, 170),
    (0, 10, 20),
    (-20, -10, 0),
    (160, 170, 180),
    (170, -180, -170),
    (180, -170, -160),
    (-180, -170, -160),
  ).foreach { case (west, central, east) =>
    test(s"Should compute central longitude between $west and $east") {
      assert(BoundingBox(west = west, south = 0, east = east, north = 0).centralLongitude === central)
    }
  }

}
