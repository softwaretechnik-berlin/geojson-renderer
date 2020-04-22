package berlin.softwaretechnik.geojsonrenderer.tiling

case class TileId(x: Int, y: Int, z: Int) {
  assert(0 <= y && y < upperCoordinateLimit, s"At zoom level $z, y must be on [0, $upperCoordinateLimit). $this")

  def normalized: TileId = copy(Math.floorMod(this.x, upperCoordinateLimit))
  def normalizedPath: String = normalized.path

  private def upperCoordinateLimit: Int = 1 << z
  private def path: String = s"$z/$x/$y"
}
