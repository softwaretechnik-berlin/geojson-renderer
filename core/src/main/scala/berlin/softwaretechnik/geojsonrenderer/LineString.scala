package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.geojson.Point

case class LineString(points: Array[GeoCoord])

object LineString {
  // TODO use Point instead of Seq[Double]
  def apply(points: Seq[Seq[Double]]): LineString = LineString(points.map(p => GeoCoord(Point(p))).toArray)
}
