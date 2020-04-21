package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.geojson.GeoJsonSpatialOps

case class BoundingBox(west: Double,
                       south: Double,
                       east: Double,
                       north: Double) {
  assert(-90 <= south && south <= north && north <= 90, s"Invalid or inconsistent latitudes: $this")
  assert(-180 <= west && west <= 180, s"Invalid west longitude: $this")
  assert(-180 <= east && east <= 180, s"Invalid east longitude: $this")

  def upperLeft(): GeoCoord = GeoCoord(north, west)
  def lowerRight(): GeoCoord = GeoCoord(south, east)

  def centralLongitude: Double =
    GeoJsonSpatialOps.normalizeLongitude(
      west + GeoJsonSpatialOps.floorMod(east - west, 360) / 2
    )
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
