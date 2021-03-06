package berlin.softwaretechnik.geojsonrenderer.map

case class Tile(id: TileId, size: Int, url: String) {
  def leftXPosition: Int = id.x * size
  def topYPosition: Int = id.y * size
}
