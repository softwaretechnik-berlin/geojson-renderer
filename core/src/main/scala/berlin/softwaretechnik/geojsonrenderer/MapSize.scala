package berlin.softwaretechnik.geojsonrenderer

/** The size of a map or of an object when projected to the map. */
case class MapSize(width: Int, height: Int) {

  def fitsIn(other: MapSize): Boolean = width <= other.width && height <= other.height

}

object MapSize {
  def apply(s: String): MapSize = {
    val components = s.split("x")
    if (components.size != 2) throw new IllegalArgumentException("Please specify dimensions as <width>x<height>.")
    MapSize(components(0).toInt, components(1).toInt)
  }
}
