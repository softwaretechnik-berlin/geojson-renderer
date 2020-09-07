package berlin.softwaretechnik.geojsonrenderer.map

/** The size of a map or of an object when projected to the map. */
case class MapSize(width: Int, height: Int) {
  require(
    width >= 0 && height >= 0,
    s"Cannot have negative width or height: $this"
  )

  def fitsIn(other: MapSize): Boolean =
    width <= other.width && height <= other.height

}

object MapSize {
  def apply(s: String): MapSize = {
    val components = s.split("x")
    if (components.size != 2)
      throw new IllegalArgumentException(
        "Please specify dimensions as <width>x<height>."
      )
    MapSize(components(0).toInt, components(1).toInt)
  }
}
