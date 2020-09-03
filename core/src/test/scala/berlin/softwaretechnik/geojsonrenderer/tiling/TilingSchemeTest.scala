package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer._
import berlin.softwaretechnik.geojsonrenderer.map._
import org.scalactic._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite

class TilingSchemeTest extends AnyFunSuite with TypeCheckedTripleEquals with Tolerance {
  val charlottenburgPalace = GeoCoord(52.520789, 13.295727)
  val shibuyaCrossing = GeoCoord(35.659486, 139.700470)
  val gettyCentre = GeoCoord(34.076848, -118.473919)
  val uluru = GeoCoord(-25.344649, 131.036802)

  val vectorElementTolerance = 0.005

  implicit def vectorEq[A <: Vector2D[A]]: Equivalence[A] =
    (a: A, b: A) =>
      Vector2D.x(a) === Vector2D.x(b) +- vectorElementTolerance &&
        Vector2D.y(a) === Vector2D.y(b) +- vectorElementTolerance

  test("It should project geopositions onto the map") {
    val tiling = TilingScheme.template("")
    assert(tiling.projection(0)(charlottenburgPalace) === MapCoordinates(137.45, 83.96))
    assert(tiling.projection(4)(charlottenburgPalace) === MapCoordinates(2199.28, 1343.29))

    assert(tiling.projection(0)(gettyCentre) === MapCoordinates(43.75, 102.20))
    assert(tiling.projection(0)(uluru) === MapCoordinates(221.18, 146.64))
  }

  test("It should give covering tiles") {
    val tiling = TilingScheme.template("")

    val projection = tiling.projection(4)
    val mapPosition = projection(charlottenburgPalace)
    val viewport = Viewport(4, projection, MapBox.covering(
      mapPosition - MapCoordinates(256, 256),
      mapPosition + MapCoordinates(256, 256)
    ))
    val tiles = tiling.tileCover(viewport).map(_.id)

    assert(tiles === Seq(
      TileId(x = 7, y = 4, z = 4),
      TileId(x = 7, y = 5, z = 4),
      TileId(x = 7, y = 6, z = 4),
      TileId(x = 8, y = 4, z = 4),
      TileId(x = 8, y = 5, z = 4),
      TileId(x = 8, y = 6, z = 4),
      TileId(x = 9, y = 4, z = 4),
      TileId(x = 9, y = 5, z = 4),
      TileId(x = 9, y = 6, z = 4),
    ))
  }

  test("It should give no more than enough tiles to cover") {
    val tiling = TilingScheme.template("")
    val projection = tiling.projection(6)
    val viewport = Viewport(6, projection, MapBox(left = -512, top = 256, size = MapSize(768, 512)))
    val tiles = tiling.tileCover(viewport).map(_.id)

    assert(tiles === Seq(
      TileId(x = -2, y = 1, z = 6),
      TileId(x = -2, y = 2, z = 6),
      TileId(x = -1, y = 1, z = 6),
      TileId(x = -1, y = 2, z = 6),
      TileId(x = 0, y = 1, z = 6),
      TileId(x = 0, y = 2, z = 6),
    ))
  }

  test("It should roundtrip geo positions via the map") {
    testRoundTrip(charlottenburgPalace)
    testRoundTrip(shibuyaCrossing)
    testRoundTrip(uluru)
    testRoundTrip(gettyCentre)
  }

  private def testRoundTrip(originalPosition: GeoCoord): Assertion = {
    val tiledProjection = TilingScheme.template("").projection(4)

    val mapCoordinates = tiledProjection(originalPosition)
    val roundTrippedGeoPosition = tiledProjection.invert(mapCoordinates)

    assert(roundTrippedGeoPosition.lat === (originalPosition.lat +- 0.00001))
    assert(roundTrippedGeoPosition.lon === (originalPosition.lon +- 0.00001))
  }
}
