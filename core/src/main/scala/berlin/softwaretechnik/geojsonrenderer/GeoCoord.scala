package berlin.softwaretechnik.geojsonrenderer

case class GeoCoord(lat: Double, lon: Double) extends Vector2DOps[GeoCoord] {
  override protected def x: Double = lon
  override protected def y: Double = lat
  override protected def v(x: Double, y: Double): GeoCoord = GeoCoord(lat = y, lon = x)
}
