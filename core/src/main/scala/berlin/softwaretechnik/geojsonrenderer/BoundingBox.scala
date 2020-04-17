package berlin.softwaretechnik.geojsonrenderer

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
