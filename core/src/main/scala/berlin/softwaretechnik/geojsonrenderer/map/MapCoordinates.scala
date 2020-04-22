package berlin.softwaretechnik.geojsonrenderer
package map

/**
 * A position on the cartesian plane of the map.
 *
 * The positive x direction is to the right, and the positive y direction is downward.
 */
final case class MapCoordinates(x: Double, y: Double) extends Vector2D[MapCoordinates] {
  override protected def create(x: Double, y: Double): MapCoordinates = MapCoordinates(x, y)
}
