package berlin.softwaretechnik.geojsonrenderer.geojson

import berlin.softwaretechnik.geojsonrenderer.{BoundingBox, GeoCoord}
import org.scalatest.funsuite.AnyFunSuite

class GeoJsonSpatialOpsTest extends AnyFunSuite {

  test("computes a straightforward bounding box") {
    assert(GeoJsonSpatialOps.boundingBox(Seq(
      GeoCoord(-1, 2),
      GeoCoord(3, -4)
    )) == BoundingBox(
      west = -4,
      south = -1,
      east = 2,
      north = 3
    ))
  }

  test("computes a bounding box when longitudes cross 180 degrees") {
    assert(GeoJsonSpatialOps.boundingBox(Seq(
      GeoCoord(0, 160),
      GeoCoord(0, 200)
    )) == BoundingBox(
      west = 160,
      south = 0,
      east = -160,
      north = 0
    ))
  }

  test("computes a bounding box when longitudes exceed 180 degrees") {
    assert(GeoJsonSpatialOps.boundingBox(Seq(
      GeoCoord(0, 200),
      GeoCoord(0, 240)
    )) == BoundingBox(
      west = -160,
      south = 0,
      east = -120,
      north = 0
    ))
  }

  test("computes a bounding box when longitudes cross -180 degrees") {
    assert(GeoJsonSpatialOps.boundingBox(Seq(
      GeoCoord(0, -160),
      GeoCoord(0, -200)
    )) == BoundingBox(
      west = 160,
      south = 0,
      east = -160,
      north = 0
    ))
  }

  test("computes a bounding box when longitudes are less than -180 degrees") {
    assert(GeoJsonSpatialOps.boundingBox(Seq(
      GeoCoord(0, -200),
      GeoCoord(0, -240)
    )) == BoundingBox(
      west = 120,
      south = 0,
      east = 160,
      north = 0
    ))
  }

  test("computes a bounding box when longitudes span most of the world") {
    assert(GeoJsonSpatialOps.boundingBox(Seq(
      GeoCoord(0, -160),
      GeoCoord(0, 160)
    )) == BoundingBox(
      west = -160,
      south = 0,
      east = 160,
      north = 0
    ))
  }

}
