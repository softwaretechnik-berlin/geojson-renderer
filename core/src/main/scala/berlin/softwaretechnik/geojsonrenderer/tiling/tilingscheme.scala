package berlin.softwaretechnik.geojsonrenderer
package tiling

import java.awt.geom.Point2D

object TilingScheme {

  def osm() = new TilingScheme(0, 19, 256, tileId =>
    s"http://tile.openstreetmap.org/${tileId.z}/${tileId.x}/${tileId.y}.png"
  )
  def rrze() = new TilingScheme(0, 19, 256, tileId =>
    s"https://c.osm.rrze.fau.de/osmhd/${tileId.z}/${tileId.x}/${tileId.y}.png"
  )

  def here(appId: String, appCode: String) = new TilingScheme(0, 19, 512, tileId =>
    s"https://1.base.maps.api.here.com/maptile/2.1/maptile/844fb05a40/normal.day/${tileId.z}/${tileId.x}/${tileId.y}/512/png8?app_id=${appId}&app_code=${appCode}&lg=eng&ppi=320&pview=DEF"
  )
}
class TilingScheme(minZoom: Int, maxZoom: Int, tileSize: Int, tileUrl: TileId => String) {

  def zoomLevels: IndexedSeq[ZoomLevel] =
    (minZoom to maxZoom)
      .map(zoomLevel => {
        new ZoomLevel(
          zoomLevel,
          tileSize << zoomLevel,
          tileSize
        )
      })

  def url(tileId: TileId): String = tileUrl(tileId)
}

class ZoomLevel(val zoomLevel: Int,
                mapWidth: Int,
                tileSizeStatic: Int) {

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

  def tileAndOffset(position2D: Vector2D): (TileId, Vector2D) =
    (
      TileId(
        (position2D.x.toLong / tileSize).toInt,
        (position2D.y.toLong / tileSize).toInt,
        zoomLevel
      ),
      Vector2D(
        position2D.x.toLong % tileSize,
        position2D.y.toLong % tileSize,
      )
    )

  def tileCover(projectedBox: Box2D): Seq[TilesWithOffset] = {
    val (upperLeftTile, tileOffset) =
      tileAndOffset(projectedBox.upperLeft)
    val (lowerRightTile, _) =
      tileAndOffset(projectedBox.lowerRight)

    val tiles: Seq[TilesWithOffset] =
      (upperLeftTile.x to lowerRightTile.x).zipWithIndex
        .flatMap {
          case (tileX, indexX) =>
            (upperLeftTile.y to lowerRightTile.y).zipWithIndex
              .map {
                case (tileY, indexY) =>
                  TilesWithOffset(
                    TileId(tileX, tileY, upperLeftTile.z),
                    (Vector2D(1, 0) * indexX + Vector2D(0, 1) * indexY) * tileSize - tileOffset
                  )
              }
        }
    tiles
  }

  def tileSize: Int = tileSizeStatic

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

case class TilesWithOffset(tileId: TileId, offset: Vector2D)

case class TileId(x: Int, y: Int, z: Int)
