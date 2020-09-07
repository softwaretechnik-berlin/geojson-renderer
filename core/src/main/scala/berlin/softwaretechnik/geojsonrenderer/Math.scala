package berlin.softwaretechnik.geojsonrenderer

case object Math {

  /**
    * Modulus operation for Double.
    *
    * Note that the JVM's {{{%}}} operation is remainder, not modulo.
    *
    * This is the Double equivalent of [[scala.math.floorMod()]], which is currently only defined for integer types.
    */
  def floorMod(x: Double, y: Int): Double = {
    val remainder = x % y
    if (remainder < 0) y + remainder else remainder
  }

}
