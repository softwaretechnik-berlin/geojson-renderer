package org.buildobjects.mapr.tiling
import org.scalactic._
import org.buildobjects.mapr.{GeoCoord, IntVector2D}
import org.scalatest.{FunSuite}

class TilingSchemeTest extends FunSuite with Tolerance {
  val charlottenburgPalace = GeoCoord(52.520789, 13.295727)
  val shibuyaCrossing = GeoCoord(35.659486, 139.700470)
  val gettyCentre = GeoCoord(34.076848, -118.473919)
  val ayersRock = GeoCoord(-25.344649, 131.036802)

  test("It should project geopositions onto the map") {
    val tiling = TilingScheme.osm()
    assert(tiling.zoomLevels(0).bitmapPosition(charlottenburgPalace).toInt == IntVector2D(137, 83))
    assert(tiling.zoomLevels(4).bitmapPosition(charlottenburgPalace).toInt == IntVector2D(2199, 1343))

    assert(tiling.zoomLevels(0).bitmapPosition(gettyCentre).toInt == IntVector2D(43,102))
    assert(tiling.zoomLevels(0).bitmapPosition(ayersRock).toInt == IntVector2D(221,146))
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
