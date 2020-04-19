package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer.{GeoCoord, Position2D}

trait GeoProjection {
  def bitmapPosition(geoCoord: GeoCoord): Position2D
  def geoCoords(pixelPosition: Position2D): GeoCoord
}

case class SomeKindOfGeoProjection(mapWidth: Int) extends GeoProjection {

  def mapCenter: Position2D =
    Position2D(mapWidth / 2, mapWidth / 2)

  override def bitmapPosition(geoCoord: GeoCoord): Position2D = {
    val x = mapCenter.x + geoCoord.lon * mapWidth / 360.0
    val e = Math.sin(geoCoord.lat * (Math.PI / 180.0)) min 0.9999 max -0.9999
    val y = mapCenter.y + 0.5 * Math.log((1 + e) / (1 - e)) * -1 * mapWidth / (Math.PI * 2)
    Position2D(x, y)
  }

  override def geoCoords(pixelPosition: Position2D): GeoCoord = {
    val lon = (pixelPosition.x - mapCenter.x) / (mapWidth / 360.0)
    val e1 = (pixelPosition.y - mapCenter.y) / (-1 * mapWidth / (2 * Math.PI))
    val e2 = (2 * Math.atan(Math.exp(e1)) - Math.PI / 2) / (Math.PI / 180.0)
    val lat = e2
    GeoCoord(lat, lon)
  }

}
