package berlin.softwaretechnik.geojsonrenderer

case class Dimensions(width: Int, height: Int)

object Dimensions {
  def apply(s: String): Dimensions = {
    val components = s.split("x")
    if (components.size != 2) throw new IllegalArgumentException("Please specify dimensions as <width>x<height>.")
    Dimensions(components(0).toInt, components(1).toInt)
  }

}

/**
 * A 2-dimensional box in screen coordinates.
 *
 * The positive x direction is to the right, and the positive y direction is downward.
 */
case class Box2D(left: Int, bottom: Int, right: Int, top: Int) {
  require(left <= right && top <= bottom, s"cannot be to the right of right, and top cannot be below bottom. $this")

  def width: Int = right - left
  def height: Int = bottom - top
  def dimensions: Dimensions = Dimensions(width, height)

  def expandTo(dimensions: Dimensions): Box2D = {
    val left = this.left - (dimensions.width - this.width) / 2
    val top = this.top - (dimensions.height - this.height) / 2
    Box2D(
      left = left,
      bottom = top + dimensions.height,
      right = left + dimensions.width,
      top = top,
    )
  }
}

object Box2D {
  def covering(upperLeft: MapCoordinates, lowerRight: MapCoordinates): Box2D = {
    require(upperLeft.x <= lowerRight.x && upperLeft.y <= lowerRight.y,
      s"In each coordinate, upperLeft must have a value less than or equal to lowerRight. $upperLeft, $lowerRight")
    Box2D(
      left = math.floor(upperLeft.x).toInt,
      bottom = math.ceil(lowerRight.y).toInt,
      right = math.ceil(lowerRight.x).toInt,
      top = math.floor(upperLeft.y).toInt,
    )
  }
}




