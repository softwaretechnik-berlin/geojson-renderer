package berlin.softwaretechnik.geojsonrenderer

/**
 * Latitude and Longitude representing a point on the surface of the earth.
 */
final case class GeoCoord(lat: Double, lon: Double) extends Vector2D[GeoCoord] {
  override protected def x: Double = lon
  override protected def y: Double = lat
  override protected def create(x: Double, y: Double): GeoCoord = GeoCoord(lat = y, lon = x)
}
