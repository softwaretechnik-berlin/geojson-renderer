package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.geojson.Point

case class GeoCoord(lat: Double, lon: Double)

object GeoCoord {
  def apply(point: Point): GeoCoord = GeoCoord(point.coordinates(1), point.coordinates(0))
}
