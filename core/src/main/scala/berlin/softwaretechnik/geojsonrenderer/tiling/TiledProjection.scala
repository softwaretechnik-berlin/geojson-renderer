package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer.map._

import scala.math._

class TiledProjection(val zoomLevel: Int, val tileSize: Int, tileUrl: TileId => String, centralMeridianLongitude: Double) {

  def mapProjection: MapProjection =
    WebMercatorProjection(zoomLevel, tileSize).withCentralMeridian(centralMeridianLongitude)

  def tileCover(viewport: MapBox): Seq[PositionedTile] =
    tileCoordinates(viewport.left, viewport.right).flatMap { tileX =>
      tileCoordinates(viewport.top, viewport.bottom).map { tileY =>
        val tileId = TileId(tileX, tileY, zoomLevel)
        PositionedTile(tileId, tileSize, tileUrl(tileId))
      }
    }

  private def tileCoordinates(minMapCoordinate: Int, maxMapCoordinate: Int): Range.Inclusive =
    floorDiv(minMapCoordinate, tileSize) to floorDiv(maxMapCoordinate - 1, tileSize)
}
