package berlin.softwaretechnik.geojsonrenderer

case class GeoCoord(lat: Double, lon: Double)

object GeoCoord {
  def apply(doubles: Seq[Double]): GeoCoord = GeoCoord(doubles(1), doubles(0))
}
