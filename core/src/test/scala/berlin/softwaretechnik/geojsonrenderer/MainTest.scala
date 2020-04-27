package berlin.softwaretechnik.geojsonrenderer

import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {

  test("Exit status 1 when file not found") {
    val exitStatus = Main.mainWithExitStatus(Array("this-file-does-not-exist.json"))
    assert(exitStatus === 1)
  }

}
