package berlin.softwaretechnik.geojsonrenderer

import org.scalactic.Tolerance
import org.scalatest.funsuite.AnyFunSuite

class MathTest extends AnyFunSuite with Tolerance {

  for {
    ((x, y), modulus) <- Seq[((Double, Int), Double)](
      (0.0, 1) -> 0,
      (1.0, 1) -> 0,
      (7.0, 4) -> 3,
      (7.6, 4) -> 3.6,
      (-7.0, 4) -> 1,
      (-7.6, 4) -> 0.4,
    )
  } test(s"$x floorMod $y === $modulus") {
    assert(Math.floorMod(x, y) === modulus +- 1e-10)
  }

}
