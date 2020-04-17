package berlin.softwaretechnik.geojsonrenderer

case class LineString(points: Array[GeoCoord])

object LineString {
  def apply(doubles: Seq[Seq[Double]]): LineString = LineString(doubles.map(GeoCoord(_)).toArray)
}
