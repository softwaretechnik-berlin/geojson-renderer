package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer._

class TiledProjection(val zoomLevel: Int, val tileSize: Int, tileUrl: TileId => String, centralLongitude: Double) {

  def geoProjection: GeoProjection =
    WebMercatorProjection(zoomLevel, tileSize).withCentralLongitude(centralLongitude)

  def tile(x: Int, y: Int): TileId = {
    TileId(
      Math.floorDiv(x, tileSize),
      Math.floorDiv(y, tileSize),
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
