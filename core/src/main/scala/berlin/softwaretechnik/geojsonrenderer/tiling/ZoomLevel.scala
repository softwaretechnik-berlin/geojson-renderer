package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer._

class ZoomLevel(val zoomLevel: Int, val tileSize: Int) {

  def geoProjection: GeoProjection = WebMercatorProjection(zoomLevel, tileSize)

  def tileAndOffset(position2D: Position2D): (TileId, Position2D) = {
    val tileId = TileId(
      Math.floor(position2D.x / tileSize).toInt,
      Math.floor(position2D.y / tileSize).toInt,
      zoomLevel
    )
    (
      tileId,
      Position2D(
        position2D.x - tileId.x * tileSize,
        position2D.y - tileId.y * tileSize,
      )
    )
  }

  def tileCover(projectedBox: Box2D): Seq[PositionedTile] = {
    val (upperLeftTile, tileOffset) = tileAndOffset(projectedBox.upperLeft)
    val (lowerRightTile, _) = tileAndOffset(projectedBox.lowerRight)

    val tiles: Seq[PositionedTile] =
      (upperLeftTile.x to lowerRightTile.x).zipWithIndex
        .flatMap {
          case (tileX, indexX) =>
            (upperLeftTile.y to lowerRightTile.y).zipWithIndex
              .map {
                case (tileY, indexY) =>
                  PositionedTile(
                    TileId(tileX, tileY, upperLeftTile.z),
                    Position2D(indexX, indexY) * tileSize - tileOffset
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
