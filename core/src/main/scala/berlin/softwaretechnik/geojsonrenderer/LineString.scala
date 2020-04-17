package berlin.softwaretechnik.geojsonrenderer

case class LineString(points: Array[GeoCoord])

object LineString {
  // TODO why sometimes Seq sometimes Array?
  def apply(points: Seq[GeoCoord]): LineString = LineString(points.toArray)
}
