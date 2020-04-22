package berlin.softwaretechnik.geojsonrenderer
package map

import scala.math._

trait MapProjection { outer =>
  def apply(geoCoord: GeoCoord): MapCoordinates
  def invert(pixelPosition: MapCoordinates): GeoCoord

  def apply(boundingBox: GeoBoundingBox): MapBox =
    MapBox.covering(
      upperLeft = apply(boundingBox.upperLeft()),
      lowerRight = apply(boundingBox.lowerRight()),
    )

  def withCentralMeridian(longitude: Double): MapProjection = {
    val leftmostLongitude = longitude - 180
    new MapProjection {
      override def apply(geoCoord: GeoCoord): MapCoordinates =
        outer.apply(geoCoord.normalizeLongitude(leftmostLongitude))
      override def invert(pixelPosition: MapCoordinates): GeoCoord =
        outer.invert(pixelPosition)
    }
  }

  def relativeTo(origin: MapCoordinates): MapProjection = new MapProjection {
    override def apply(geoCoord: GeoCoord): MapCoordinates = outer.apply(geoCoord) - origin
    override def invert(pixelPosition: MapCoordinates): GeoCoord = outer.invert(pixelPosition + origin)
  }

}

/**
 * A Web Mercator projection.
 *
 * The formulas used are slightly refactored versions of those provided at
 * [[https://en.wikipedia.org/wiki/Web_Mercator_projection#Formulas]].
 *
 * @see https://en.wikipedia.org/wiki/Web_Mercator_projection
 */
// TODO I suspect the tile size doesn't belong here, and should always be 256…
case class WebMercatorProjection(zoomLevel: Int, tileSize: Int) extends MapProjection {

  private val mapWidth: Double = tileSize << zoomLevel
  private val pixelsPerDegreeOfLongitude: Double = mapWidth / 360
  private val pixelsPerUnitOfProjectedLatitude: Double = mapWidth / (2 * Pi)

  override def apply(geoCoord: GeoCoord): MapCoordinates =
    MapCoordinates(
      x = pixelsPerDegreeOfLongitude * (180 + geoCoord.lon),
      y = pixelsPerUnitOfProjectedLatitude * (Pi - log(tan(toRadians(90 + geoCoord.lat) / 2)))
    )

  override def invert(pixelPosition: MapCoordinates): GeoCoord =
    GeoCoord(
      lat = toDegrees(2 * atan(exp(Pi - pixelPosition.y / pixelsPerUnitOfProjectedLatitude))) - 90,
      lon = pixelPosition.x / pixelsPerDegreeOfLongitude - 180
    )

}
