package berlin.softwaretechnik.geojsonrenderer.tiling

case class TileId(x: Int, y: Int, z: Int) {
  assert(0 <= y && y < upperCoordinateLimit, s"At zoom level $z, y must be on [0, $upperCoordinateLimit). $this")

  def normalized: TileId = {
    val normalizedX = Math.floorMod(this.x, upperCoordinateLimit)
    if (x == normalizedX) this
    else copy(normalizedX)
  }

  private def upperCoordinateLimit: Int = 1 << z
}
