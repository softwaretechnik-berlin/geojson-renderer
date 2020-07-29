package org.buildobjects.mapr

case class Dimensions(width: Double, height: Double) {
  def toVector = DoubleVector2D(width, height)
  def toBox2D = Box2D(new Position2D(0, 0), new Position2D(width, height))
}

object Dimensions {
  def apply(s: String): Dimensions = {
    val components = s.split("x")
    if (components.size != 2) throw new IllegalArgumentException("Please specify dimensions as <width>x<height>.")
    Dimensions(components(0).toDouble, components(1).toDouble)
  }

}

class Position2D(x: Double, y: Double) extends DoubleVector2D(x, y)

case class Box2D(upperLeft: DoubleVector2D, lowerRight: DoubleVector2D) {
  def dimensions =
    Dimensions(lowerRight.x - upperLeft.x, lowerRight.y - upperLeft.y)
  def width = lowerRight.x - upperLeft.x
  def height = -upperLeft.y + lowerRight.y

  def -(v: DoubleVector2D): Box2D =
    Box2D(upperLeft - v, lowerRight - v)

  def +(v: DoubleVector2D): Box2D =
    Box2D(upperLeft + v, lowerRight + v)

  def rect = <rect
    x={upperLeft.x.toString}
    y={upperLeft.y.toString}
    width={width.toString}
    height={height.toString}
    fill="none"
    stroke="red"
    />
}

case class DoubleVector2D(x: Double, y: Double) {
  def toInt: IntVector2D = IntVector2D(x.toInt, y.toInt)
  def toLong: LongVector2D = LongVector2D(x.toLong, y.toLong)

  def +(other: DoubleVector2D) = DoubleVector2D(x + other.x, y + other.y)

  def -(other: DoubleVector2D) = DoubleVector2D(x - other.x, y - other.y)

  def *(factor: Double) = DoubleVector2D(x * factor, y * factor)
}

case class IntVector2D(x: Int, y: Int) {
  def +(other: IntVector2D) = IntVector2D(x + other.x, y + other.y)

  def -(other: IntVector2D) = IntVector2D(x - other.x, y - other.y)

  def *(factor: Int) = IntVector2D(x * factor, y * factor)
}

case class LongVector2D(x: Long, y: Long) {
  def +(other: LongVector2D) = LongVector2D(x + other.x, y + other.y)

  def -(other: LongVector2D) = LongVector2D(x - other.x, y - other.y)

  def *(factor: Long) = LongVector2D(x * factor, y * factor)

  def /(divisor: Long) = LongVector2D(x / divisor, y / divisor)

  def %(divisor: Long) = LongVector2D(x % divisor, y % divisor)
}
