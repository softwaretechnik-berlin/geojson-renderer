package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer._

import scala.math._

class TiledProjection(val zoomLevel: Int, val tileSize: Int, tileUrl: TileId => String, centralMeridianLongitude: Double) {

  def mapProjection: MapProjection =
    WebMercatorProjection(zoomLevel, tileSize).withCentralMeridian(centralMeridianLongitude)

  def tile(x: Int, y: Int): TileId = {
    TileId(
      floorDiv(x, tileSize),
      floorDiv(y, tileSize),
      zoomLevel
    )
  }

  def tileCover(viewport: Box2D): Seq[PositionedTile] = {
    val upperLeftTile = tile(x = viewport.left, y = viewport.top)
    val lowerRightTile = tile(x = viewport.right - 1, y = viewport.bottom - 1)

    val tiles: Seq[PositionedTile] =
      (upperLeftTile.x to lowerRightTile.x).flatMap { tileX =>
        (upperLeftTile.y to lowerRightTile.y).map { tileY =>
          val tileId = TileId(tileX, tileY, zoomLevel)
          PositionedTile(
            id = tileId,
            size = tileSize,
            url = tileUrl(tileId)
          )
        }
      }
    tiles
  }

}
