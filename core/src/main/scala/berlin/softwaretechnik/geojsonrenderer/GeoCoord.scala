package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.Math._

/**
 * Latitude and Longitude representing a point on the surface of the earth.
 */
final case class GeoCoord(lat: Double, lon: Double) extends Vector2D[GeoCoord] {

  def normalizeLongitude(minimumLongitude: Double): GeoCoord =
    copy(lon = GeoCoord.normalizeLongitude(lon, minimumLongitude))

  override protected def x: Double = lon
  override protected def y: Double = lat
  override protected def create(x: Double, y: Double): GeoCoord = GeoCoord(lat = y, lon = x)
}

object GeoCoord {

  def normalizeLongitude(longitude: Double, minimumLongitude: Double = -180): Double =
    floorMod(longitude - minimumLongitude, 360) + minimumLongitude

}
