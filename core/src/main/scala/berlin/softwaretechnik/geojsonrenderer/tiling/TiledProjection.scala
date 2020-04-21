package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer._

class TiledProjection(val zoomLevel: Int, val tileSize: Int, tileUrl: TileId => String, centralLongitude: Double) {

  def geoProjection: GeoProjection =
    WebMercatorProjection(zoomLevel, tileSize).withCentralLongitude(centralLongitude)

  def tile(position2D: Position2D): TileId = {
    TileId(
      Math.floor(position2D.x / tileSize).toInt,
      Math.floor(position2D.y / tileSize).toInt,
      zoomLevel
    )
  }

  def tileCover(projectedBox: Box2D): Seq[PositionedTile] = {
    val upperLeftTile = tile(projectedBox.upperLeft)
    val lowerRightTile = tile(projectedBox.lowerRight)

    val tiles: Seq[PositionedTile] =
      (upperLeftTile.x to lowerRightTile.x).flatMap { tileX =>
        (upperLeftTile.y to lowerRightTile.y).map { tileY =>
          val tileId = TileId(tileX, tileY, upperLeftTile.z)
          PositionedTile(
            id = tileId,
            position = Position2D(
              x = tileId.x * tileSize - projectedBox.upperLeft.x,
              y = tileId.y * tileSize - projectedBox.upperLeft.y,
            ),
            size = tileSize,
            url = tileUrl(tileId)
          )
        }
      }
    tiles
  }

  def bitmapBox(boundingBox: BoundingBox): Box2D =
    Box2D(
      upperLeft = geoProjection.bitmapPosition(boundingBox.upperLeft()),
      lowerRight = geoProjection.bitmapPosition(boundingBox.lowerRight())
    )

}
