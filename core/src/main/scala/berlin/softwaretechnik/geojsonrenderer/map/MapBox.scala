package berlin.softwaretechnik.geojsonrenderer.map

/**
  * A 2-dimensional box in the map's cartesian coordinates.
  *
  * The positive x direction is to the right, and the positive y direction is downward.
  */
case class MapBox(left: Int, top: Int, size: MapSize) {

  def right: Int = left + size.width
  def bottom: Int = top + size.height

  def expandTo(size: MapSize): MapBox =
    MapBox(
      left = this.left - (size.width - this.size.width) / 2,
      top = this.top - (size.height - this.size.height) / 2,
      size = size
    )

}

object MapBox {
  def covering(
      upperLeft: MapCoordinates,
      lowerRight: MapCoordinates
  ): MapBox = {
    require(
      upperLeft.x <= lowerRight.x && upperLeft.y <= lowerRight.y,
      s"In each coordinate, upperLeft must have a value less than or equal to lowerRight. $upperLeft, $lowerRight"
    )

    val left = math.floor(upperLeft.x).toInt
    val top = math.floor(upperLeft.y).toInt
    MapBox(
      left = left,
      top = top,
      size = MapSize(
        width = math.ceil(lowerRight.x).toInt - left,
        height = math.ceil(lowerRight.y).toInt - top
      )
    )
  }
}
