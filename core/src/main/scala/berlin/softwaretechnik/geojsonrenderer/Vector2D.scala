package berlin.softwaretechnik.geojsonrenderer

trait Vector2D[V <: Vector2D[V]] {
  protected def x: Double
  protected def y: Double
  protected def create(x: Double, y: Double): V

  final def +(other: V): V = create(x + other.x, y + other.y)
  final def -(other: V): V = create(x - other.x, y - other.y)
}

object Vector2D {
  def x(v: Vector2D[_]): Double = v.x
  def y(v: Vector2D[_]): Double = v.y
}
