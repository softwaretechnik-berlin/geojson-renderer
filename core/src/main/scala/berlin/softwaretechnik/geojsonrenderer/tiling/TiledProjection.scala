package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer._

class TiledProjection(val zoomLevel: Int, val tileSize: Int, centralLongitude: Double) {

  def geoProjection: GeoProjection =
    WebMercatorProjection(zoomLevel, tileSize).shiftLongitude(centralLongitude)

  private val xOfLongitudeMinus180: Double =
    geoProjection.bitmapPosition(GeoCoord(lat = 0, lon = -180)).x

  def tile(position2D: Position2D): TileId = {
    TileId(
      Math.floor((position2D.x - xOfLongitudeMinus180) / tileSize).toInt,
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
            tileId,
            Position2D(
              x = tileId.x * tileSize - projectedBox.upperLeft.x + xOfLongitudeMinus180,
              y = tileId.y * tileSize - projectedBox.upperLeft.y,
            )
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
