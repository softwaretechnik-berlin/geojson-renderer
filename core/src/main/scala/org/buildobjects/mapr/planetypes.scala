package org.buildobjects.mapr

import scala.xml.Elem

case class Dimensions(width: Double, height: Double) {
  def toVector = Vector2D(width, height)
  def toBox2D = Box2D(new Position2D(0, 0), new Position2D(width, height))
}

object Dimensions {
  def apply(s: String): Dimensions = {
    val components = s.split("x")
    if (components.size != 2) throw new IllegalArgumentException("Please specify dimensions as <width>x<height>.")
    Dimensions(components(0).toDouble, components(1).toDouble)
  }

}

class Position2D(x: Double, y: Double) extends Vector2D(x, y)

case class Box2D(upperLeft: Vector2D, lowerRight: Vector2D) {
  def dimensions: Dimensions =
    Dimensions(lowerRight.x - upperLeft.x, lowerRight.y - upperLeft.y)

  def width: Double = lowerRight.x - upperLeft.x
  def height: Double = -upperLeft.y + lowerRight.y

  def -(v: Vector2D): Box2D =
    Box2D(upperLeft - v, lowerRight - v)

  def +(v: Vector2D): Box2D =
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

case class Vector2D(x: Double, y: Double) {
  def +(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)

  def -(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)

  def *(scalar: Double): Vector2D = Vector2D(x * scalar, y * scalar)
}
