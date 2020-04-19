package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer.{GeoCoord, Position2D}

import scala.math._

trait GeoProjection {
  def bitmapPosition(geoCoord: GeoCoord): Position2D
  def geoCoords(pixelPosition: Position2D): GeoCoord
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
case class WebMercatorProjection(zoomLevel: Int, tileSize: Int) extends GeoProjection {

  private val mapWidth: Double = tileSize << zoomLevel
  private val pixelsPerDegreeOfLongitude: Double = mapWidth / 360
  private val pixelsPerUnitOfProjectedLatitude: Double = mapWidth / (2 * Pi)

  override def bitmapPosition(geoCoord: GeoCoord): Position2D =
    Position2D(
      x = pixelsPerDegreeOfLongitude * (180 + geoCoord.lon),
      y = pixelsPerUnitOfProjectedLatitude * (Pi - log(tan(toRadians(90 + geoCoord.lat) / 2)))
    )

  override def geoCoords(pixelPosition: Position2D): GeoCoord =
    GeoCoord(
      lat = toDegrees(2 * atan(exp(Pi - pixelPosition.y / pixelsPerUnitOfProjectedLatitude))) - 90,
      lon = pixelPosition.x / pixelsPerDegreeOfLongitude - 180
    )

}
