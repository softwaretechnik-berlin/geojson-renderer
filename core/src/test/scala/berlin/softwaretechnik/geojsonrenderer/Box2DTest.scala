package berlin.softwaretechnik.geojsonrenderer

import org.scalatest.funsuite.AnyFunSuite

class Box2DTest extends AnyFunSuite {

  test(s"Should round outward with positive coordinates") {
    assert(Box2D.covering(
      upperLeft = MapCoordinates(1.1, 2.2),
      lowerRight = MapCoordinates(3.3, 4.4)
    ) === Box2D(
      left = 1,
      bottom = 5,
      right = 4,
      top = 2
    ))
  }

  test(s"Should round outward with negative coordinates") {
    assert(Box2D.covering(
      upperLeft = MapCoordinates(-3.3, -4.4),
      lowerRight = MapCoordinates(-1.1, -2.2)
    ) === Box2D(
      left = -4,
      bottom = -2,
      right = -1,
      top = -5
    ))
  }

}
