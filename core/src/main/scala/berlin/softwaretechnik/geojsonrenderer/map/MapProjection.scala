package berlin.softwaretechnik.geojsonrenderer
package map

trait MapProjection { outer =>
  def apply(geoCoord: GeoCoord): MapCoordinates
  def invert(pixelPosition: MapCoordinates): GeoCoord

  def apply(boundingBox: GeoBoundingBox): MapBox =
    MapBox.covering(
      upperLeft = apply(boundingBox.upperLeft()),
      lowerRight = apply(boundingBox.lowerRight()),
    )

  def normalizingLongitudesAround(longitude: Double): MapProjection = new MapProjection {
    private val leftmostLongitude: Double = longitude - 180
    override def apply(geoCoord: GeoCoord): MapCoordinates =
      outer.apply(geoCoord.normalizeLongitude(leftmostLongitude))
    override def invert(pixelPosition: MapCoordinates): GeoCoord =
      outer.invert(pixelPosition)
  }

  def relativeTo(origin: MapCoordinates): MapProjection = new MapProjection {
    override def apply(geoCoord: GeoCoord): MapCoordinates = outer.apply(geoCoord) - origin
    override def invert(pixelPosition: MapCoordinates): GeoCoord = outer.invert(pixelPosition + origin)
  }

}


