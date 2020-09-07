package berlin.softwaretechnik.geojsonrenderer.map

import berlin.softwaretechnik.geojsonrenderer.GeoCoord

import scala.math._

/**
  * A Web Mercator projection.
  *
  * The formulas used are slightly refactored versions of those provided at
  * [[https://en.wikipedia.org/wiki/Web_Mercator_projection#Formulas]].
  *
  * @see https://en.wikipedia.org/wiki/Web_Mercator_projection
  */
case class WebMercatorProjection(zoomLevel: Int, tileSize: Int)
    extends MapProjection {

  private val mapWidth: Double = tileSize << zoomLevel
  private val pixelsPerDegreeOfLongitude: Double = mapWidth / 360
  private val pixelsPerUnitOfProjectedLatitude: Double = mapWidth / (2 * Pi)

  override def apply(geoCoord: GeoCoord): MapCoordinates =
    MapCoordinates(
      x = pixelsPerDegreeOfLongitude * (180 + geoCoord.lon),
      y = pixelsPerUnitOfProjectedLatitude * (Pi - log(
        tan(toRadians(90 + geoCoord.lat) / 2)
      ))
    )

  override def invert(pixelPosition: MapCoordinates): GeoCoord =
    GeoCoord(
      lat = toDegrees(
        2 * atan(exp(Pi - pixelPosition.y / pixelsPerUnitOfProjectedLatitude))
      ) - 90,
      lon = pixelPosition.x / pixelsPerDegreeOfLongitude - 180
    )

}
