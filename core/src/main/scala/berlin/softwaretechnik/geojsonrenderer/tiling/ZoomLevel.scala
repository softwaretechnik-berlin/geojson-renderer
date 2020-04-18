package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer._

class ZoomLevel(val zoomLevel: Int, val tileSize: Int) {

  def geoProjection: GeoProjection = SomeKindOfGeoProjection(tileSize << zoomLevel)

  def tileAndOffset(position2D: Position2D): (TileId, Position2D) =
    (
      TileId(
        (position2D.x.toLong / tileSize).toInt,
        (position2D.y.toLong / tileSize).toInt,
        zoomLevel
      ),
      Position2D(
        position2D.x.toLong % tileSize,
        position2D.y.toLong % tileSize,
      )
    )

  def tileCover(projectedBox: Box2D): Seq[PositionedTile] = {
    val (upperLeftTile, tileOffset) =
      tileAndOffset(projectedBox.upperLeft)
    val (lowerRightTile, _) =
      tileAndOffset(projectedBox.lowerRight)

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
