package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.Math._

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
    GeoCoord.normalizeLongitude(
      west + floorMod(east - west, 360) / 2
    )
}

object BoundingBox {

  def apply(coordinates: Seq[GeoCoord]): BoundingBox = {
    import Ordering.Double.IeeeOrdering
    BoundingBox(
      west = GeoCoord.normalizeLongitude(coordinates.map(_.lon).min),
      south = coordinates.map(_.lat).min,
      east = GeoCoord.normalizeLongitude(coordinates.map(_.lon).max),
      north = coordinates.map(_.lat).max
    )
  }

}
