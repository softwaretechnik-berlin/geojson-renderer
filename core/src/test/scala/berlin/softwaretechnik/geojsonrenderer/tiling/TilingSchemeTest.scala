package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer.{GeoCoord, Vector2D}
import org.scalactic._
import org.scalatest.funsuite.AnyFunSuite

class TilingSchemeTest extends AnyFunSuite with Tolerance {
  val charlottenburgPalace = GeoCoord(52.520789, 13.295727)
  val shibuyaCrossing = GeoCoord(35.659486, 139.700470)
  val gettyCentre = GeoCoord(34.076848, -118.473919)
  val ayersRock = GeoCoord(-25.344649, 131.036802)

  val vectorElementTolerance = 0.005

  implicit def vectorEq[A <: Vector2D]: Equality[A] =
    (a: A, b: Any) => b match {
      case b: Vector2D =>
        a.x === b.x +- vectorElementTolerance &&
          a.y === b.y +- vectorElementTolerance
      case _ => false
    }

  test("It should project geopositions onto the map") {
    val tiling = TilingScheme.osm()
    assert(tiling.zoomLevels(0).bitmapPosition(charlottenburgPalace) === Vector2D(137.45, 83.96))
    assert(tiling.zoomLevels(4).bitmapPosition(charlottenburgPalace) === Vector2D(2199.28, 1343.29))

    assert(tiling.zoomLevels(0).bitmapPosition(gettyCentre) === Vector2D(43.75, 102.20))
    assert(tiling.zoomLevels(0).bitmapPosition(ayersRock) === Vector2D(221.18, 146.64))
  }

  test("It should get tile and offset") {
    val tiling = TilingScheme.osm()

    val zoomLevel = tiling.zoomLevels(4)
    val mapPosition = zoomLevel.bitmapPosition(charlottenburgPalace)

    val (tile, offset) = zoomLevel.tileAndOffset(mapPosition)
    assert(tile.x == 8)
    assert(offset.x == 151)
    assert(tile.y == 5)
    assert(offset.y == 63)
  }

  test("It should roundtrip geo positions via the map") {
    testRoundTrip(charlottenburgPalace)
    testRoundTrip(shibuyaCrossing)
    testRoundTrip(ayersRock)
    testRoundTrip(gettyCentre)
  }


  private def testRoundTrip(originalPosition: GeoCoord) = {
    val tiling = TilingScheme.osm()
    val mapPosition = tiling.zoomLevels(4).bitmapPosition(originalPosition)

    val roundTrippedGeoPosition = tiling.zoomLevels(4).geoCoords(mapPosition)

    assert(roundTrippedGeoPosition.lat === (originalPosition.lat +- 0.00001))
    assert(roundTrippedGeoPosition.lon === (originalPosition.lon +- 0.00001))
  }
}
