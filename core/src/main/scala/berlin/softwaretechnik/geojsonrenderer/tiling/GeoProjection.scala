package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer.geojson.GeoJsonSpatialOps
import berlin.softwaretechnik.geojsonrenderer.{BoundingBox, Box2D, GeoCoord, Position2D}

import scala.math._

trait GeoProjection { top =>
  def bitmapPosition(geoCoord: GeoCoord): Position2D
  def geoCoords(pixelPosition: Position2D): GeoCoord

  def coveringViewport(boundingBox: BoundingBox): Box2D =
    Box2D.covering(
      upperLeft = bitmapPosition(boundingBox.upperLeft()),
      lowerRight = bitmapPosition(boundingBox.lowerRight()),
    )

  def withCentralLongitude(centralLongitude: Double): GeoProjection = {
    val minimumLongitude = centralLongitude - 180
    new GeoProjection {
      override def bitmapPosition(geoCoord: GeoCoord): Position2D =
        top.bitmapPosition(GeoJsonSpatialOps.normalizeLongitude(geoCoord, minimumLongitude))
      override def geoCoords(pixelPosition: Position2D): GeoCoord =
        top.geoCoords(pixelPosition)
    }
  }

  def relativeTo(viewport: Box2D): GeoProjection = new GeoProjection {
    private val origin: Position2D = Position2D(x = viewport.left, y = viewport.top)
    override def bitmapPosition(geoCoord: GeoCoord): Position2D = top.bitmapPosition(geoCoord) - origin
    override def geoCoords(pixelPosition: Position2D): GeoCoord = top.geoCoords(pixelPosition + origin)
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
