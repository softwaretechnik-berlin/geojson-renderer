package berlin.softwaretechnik.geojsonrenderer

trait GeoGeometry

case class BoundingBox(west: Double,
                       south: Double,
                       east: Double,
                       north: Double) {
  def upperLeft(): GeoCoord = GeoCoord(north, west)
  def lowerRight(): GeoCoord = GeoCoord(south, east)
}

object BoundingBox {
  def apply(string: String): BoundingBox = {
    string
      .split(",")
      .map(_.toDouble) match {
      case Array(west, south, east, north) =>
        BoundingBox(west, south, east, north)
      case _ => throw new IllegalArgumentException("")
    }
  }
}



case class GeoCoord(lat: Double, lon: Double)

object GeoCoord {
  def apply(doubles: Seq[Double]): GeoCoord = GeoCoord(doubles(1), doubles(0))
}

case class LineString(points: Array[GeoCoord])

object LineString {
  def apply(doubles: Seq[Seq[Double]]): LineString = LineString(doubles.map(GeoCoord(_)).toArray)
}

