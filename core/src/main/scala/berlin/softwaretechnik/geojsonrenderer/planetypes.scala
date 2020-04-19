package berlin.softwaretechnik.geojsonrenderer

import scala.xml.Elem

case class Dimensions(width: Double, height: Double) {
  def toVector = Position2D(width, height)
  def toBox2D = Box2D(new Position2D(0, 0), new Position2D(width, height))
}

object Dimensions {
  def apply(s: String): Dimensions = {
    val components = s.split("x")
    if (components.size != 2) throw new IllegalArgumentException("Please specify dimensions as <width>x<height>.")
    Dimensions(components(0).toDouble, components(1).toDouble)
  }

}

/**
 * A position in screen coordinates.
 *
 * The positive x direction is to the right, and the positive y direction is downward.
 */
final case class Position2D(x: Double, y: Double) extends Vector2DOps[Position2D] {
  override protected def v(x: Double, y: Double): Position2D = Position2D(x, y)
}

/**
 * A 2-dimensional box in screen coordinates.
 *
 * The positive x direction is to the right, and the positive y direction is downward.
 */
case class Box2D(upperLeft: Position2D, lowerRight: Position2D) {
  require(upperLeft.x <= lowerRight.x && upperLeft.y <= lowerRight.y,
    s"In each coordinate, upperLeft must have a value less than or equal to lowerRight. $this")

  def dimensions: Dimensions =
    Dimensions(lowerRight.x - upperLeft.x, lowerRight.y - upperLeft.y)

  def width: Double = lowerRight.x - upperLeft.x
  def height: Double = -upperLeft.y + lowerRight.y

  def -(v: Position2D): Box2D =
    Box2D(upperLeft - v, lowerRight - v)

  def +(v: Position2D): Box2D =
    Box2D(upperLeft + v, lowerRight + v)

  def rect: Elem = <rect
    x={upperLeft.x.toString}
    y={upperLeft.y.toString}
    width={width.toString}
    height={height.toString}
    fill="none"
    stroke="red"
    />
}

final case class Vector2D(x: Double, y: Double) extends Vector2DOps[Vector2D] {
  override protected def v(x: Double, y: Double): Vector2D = Vector2D(x, y)
}

trait Vector2DOps[V <: Vector2DOps[V]] {
  def x: Double
  def y: Double
  protected def v(x: Double, y: Double): V

  final def +(other: V): V = v(x + other.x, y + other.y)
  final def -(other: V): V = v(x - other.x, y - other.y)
  final def *(scalar: Double): V = v(x * scalar, y * scalar)
}
