package berlin.softwaretechnik.geojsonrenderer

/**
 * A 2-dimensional box in the map's cartesian coordinates.
 *
 * The positive x direction is to the right, and the positive y direction is downward.
 */
case class MapBox(left: Int, bottom: Int, right: Int, top: Int) {
  require(left <= right && top <= bottom, s"cannot be to the right of right, and top cannot be below bottom. $this")

  def width: Int = right - left
  def height: Int = bottom - top
  def size: MapSize = MapSize(width, height)

  def expandTo(size: MapSize): MapBox = {
    val left = this.left - (size.width - this.width) / 2
    val top = this.top - (size.height - this.height) / 2
    MapBox(
      left = left,
      bottom = top + size.height,
      right = left + size.width,
      top = top,
    )
  }
}

object MapBox {
  def covering(upperLeft: MapCoordinates, lowerRight: MapCoordinates): MapBox = {
    require(upperLeft.x <= lowerRight.x && upperLeft.y <= lowerRight.y,
      s"In each coordinate, upperLeft must have a value less than or equal to lowerRight. $upperLeft, $lowerRight")
    MapBox(
      left = math.floor(upperLeft.x).toInt,
      bottom = math.ceil(lowerRight.y).toInt,
      right = math.ceil(lowerRight.x).toInt,
      top = math.floor(upperLeft.y).toInt,
    )
  }
}




