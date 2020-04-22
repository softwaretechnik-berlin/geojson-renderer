package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.map.{MapBox, MapCoordinates, MapSize}
import org.scalatest.funsuite.AnyFunSuite

class MapBoxTest extends AnyFunSuite {

  test(s"Should round outward with positive coordinates") {
    assert(MapBox.covering(
      upperLeft = MapCoordinates(1.1, 2.2),
      lowerRight = MapCoordinates(3.3, 5.4)
    ) === MapBox(
      left = 1,
      top = 2,
      size = MapSize(3, 4)
    ))
  }

  test(s"Should round outward with negative coordinates") {
    assert(MapBox.covering(
      upperLeft = MapCoordinates(-3.3, -5.4),
      lowerRight = MapCoordinates(-1.1, -2.2)
    ) === MapBox(
      left = -4,
      top = -6,
      size = MapSize(3, 4)
    ))
  }

  test(s"Should handle zero size") {
    assert(MapBox.covering(
      upperLeft = MapCoordinates(42, -12),
      lowerRight = MapCoordinates(42, -12)
    ) === MapBox(
      left = 42,
      top = -12,
      size = MapSize(0, 0)
    ))
  }

}
