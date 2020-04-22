package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.geojson.GeoJsonSpatialOps
import org.scalatest.funsuite.AnyFunSuite

class GeoBoundingBoxTest extends AnyFunSuite {

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
      assert(GeoBoundingBox(west = west, south = 0, east = east, north = 0).centralLongitude === central)
    }
  }
  
  test("computes a straightforward bounding box") {
    assert(GeoBoundingBox(Seq(
      GeoCoord(-1, 2),
      GeoCoord(3, -4)
    )) == GeoBoundingBox(
      west = -4,
      south = -1,
      east = 2,
      north = 3
    ))
  }

  test("computes a bounding box when longitudes cross 180 degrees") {
    assert(GeoBoundingBox(Seq(
      GeoCoord(0, 160),
      GeoCoord(0, 200)
    )) == GeoBoundingBox(
      west = 160,
      south = 0,
      east = -160,
      north = 0
    ))
  }

  test("computes a bounding box when longitudes exceed 180 degrees") {
    assert(GeoBoundingBox(Seq(
      GeoCoord(0, 200),
      GeoCoord(0, 240)
    )) == GeoBoundingBox(
      west = -160,
      south = 0,
      east = -120,
      north = 0
    ))
  }

  test("computes a bounding box when longitudes cross -180 degrees") {
    assert(GeoBoundingBox(Seq(
      GeoCoord(0, -160),
      GeoCoord(0, -200)
    )) == GeoBoundingBox(
      west = 160,
      south = 0,
      east = -160,
      north = 0
    ))
  }

  test("computes a bounding box when longitudes are less than -180 degrees") {
    assert(GeoBoundingBox(Seq(
      GeoCoord(0, -200),
      GeoCoord(0, -240)
    )) == GeoBoundingBox(
      west = 120,
      south = 0,
      east = 160,
      north = 0
    ))
  }

  test("computes a bounding box when longitudes span most of the world") {
    assert(GeoBoundingBox(Seq(
      GeoCoord(0, -160),
      GeoCoord(0, 160)
    )) == GeoBoundingBox(
      west = -160,
      south = 0,
      east = 160,
      north = 0
    ))
  }


}
