package berlin.softwaretechnik.geojsonrenderer.map

import scala.math.floorDiv

class TilingScheme(val minZoom: Int, val maxZoom: Int, tileSize: Int, tileUrl: TileId => String) {

  def projection(zoomLevel: Int): MapProjection = WebMercatorProjection(zoomLevel, tileSize)

  def tileCover(viewport: Viewport): Seq[Tile] =
    for {
      x <- tileCoordinates(viewport.box.left, viewport.box.right)
      y <- tileCoordinates(viewport.box.top, viewport.box.bottom)
      id = TileId(x, y, viewport.zoomLevel)
    } yield Tile(id, tileSize, tileUrl(id))

  private def tileCoordinates(minMapCoordinate: Int, maxMapCoordinate: Int): Range =
    floorDiv(minMapCoordinate, tileSize) to floorDiv(maxMapCoordinate - 1, tileSize)

}

object TilingScheme {

  def osm() = new TilingScheme(0, 19, 256, tileId =>
    s"http://tile.openstreetmap.org/${tileId.normalizedPath}.png"
  )

  def rrze() = new TilingScheme(0, 19, 256, tileId =>
    s"https://c.osm.rrze.fau.de/osmhd/${tileId.normalizedPath}.png"
  )

  def here(appId: String, appCode: String) = new TilingScheme(0, 19, 512, tileId =>
    s"https://1.base.maps.api.here.com/maptile/2.1/maptile/844fb05a40/normal.day/${tileId.normalizedPath}/512/png8?app_id=${appId}&app_code=${appCode}&lg=eng&ppi=320&pview=DEF"
  )

}
