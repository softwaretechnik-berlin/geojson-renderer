package berlin.softwaretechnik.geojsonrenderer.tiling

import java.awt.geom.Point2D

import berlin.softwaretechnik.geojsonrenderer._

class ZoomLevel(val zoomLevel: Int,
                mapWidth: Int,
                val tileSize: Int) {

  def mapCenter: Position2D = {
    new Position2D(mapWidth / 2, mapWidth / 2)
  }

  private def getBitmapCoordinate(latitude: Double,
                                  longitude: Double): Point2D = {

    val x = mapCenter.x + longitude * mapWidth / 360.0
    var e = Math.sin(latitude * (Math.PI / 180.0))
    if (e > 0.9999) e = 0.9999
    if (e < -0.9999) e = -0.9999
    val y = mapCenter.y + 0.5 * Math.log((1 + e) / (1 - e)) * -1 * mapWidth / (Math.PI * 2)
    new Point2D.Double(x, y)
  }

  def bitmapPosition(geoCoord: GeoCoord): Position2D = {
    val p2d = getBitmapCoordinate(geoCoord.lat, geoCoord.lon)
    new Position2D(p2d.getX, p2d.getY)
  }

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

  def bitmapBox(boundingBox: BoundingBox): Box2D = {
    new Box2D(
      upperLeft = bitmapPosition(boundingBox.upperLeft()),
      lowerRight = bitmapPosition(boundingBox.lowerRight())
    )
  }

  def geoCoords(pixelPosition: Position2D): GeoCoord = {
    val lon = (pixelPosition.x - mapCenter.x) / (mapWidth / 360.0)
    val e1 = (pixelPosition.y - mapCenter.y) / (-1 * mapWidth / (2 * Math.PI))
    val e2 = (2 * Math.atan(Math.exp(e1)) - Math.PI / 2) / (Math.PI / 180.0)
    val lat = e2
    new GeoCoord(lat, lon)
  }

}
