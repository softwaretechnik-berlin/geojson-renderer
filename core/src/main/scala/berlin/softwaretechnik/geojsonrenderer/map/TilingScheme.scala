package berlin.softwaretechnik.geojsonrenderer.map

import scala.math.floorDiv

class TilingScheme(
    val minZoom: Int,
    val maxZoom: Int,
    tileSize: Int,
    tileUrl: TileId => String
) {

  def projection(zoomLevel: Int): MapProjection =
    WebMercatorProjection(zoomLevel, tileSize)

  def tileCover(viewport: Viewport): Seq[Tile] =
    for {
      x <- tileCoordinates(viewport.box.left, viewport.box.right)
      y <- tileCoordinates(viewport.box.top, viewport.box.bottom)
      id = TileId(x, y, viewport.zoomLevel)
    } yield Tile(id, tileSize, tileUrl(id))

  private def tileCoordinates(
      minMapCoordinate: Int,
      maxMapCoordinate: Int
  ): Range =
    floorDiv(minMapCoordinate, tileSize) to floorDiv(
      maxMapCoordinate - 1,
      tileSize
    )

}

object TilingScheme {

  def template(templateString: String) = {
    new TilingScheme(
      0,
      19,
      256,
      tileId =>
        templateString
          .replaceAll("\\{tile}", tileId.normalizedPath)
          .replaceAll(
            "\\{1-4}",
            (1 + (Math.abs(tileId.x + tileId.y) % 4)).toString
          )
          .replaceAll(
            "\\{a-c}",
            "".appended((Math.abs(tileId.x + tileId.y) % 3 + 'a'.toInt).toChar)
          )
    )
  }

  def osm() =
    new TilingScheme(
      0,
      19,
      256,
      tileId => s"http://a.tile.openstreetmap.org/${tileId.normalizedPath}.png"
    )

}
