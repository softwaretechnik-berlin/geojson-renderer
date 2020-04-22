package berlin.softwaretechnik.geojsonrenderer
package tiling

import berlin.softwaretechnik.geojsonrenderer.map._

class TilingScheme(minZoom: Int, maxZoom: Int, tileSize: Int, tileUrl: TileId => String) {

  def tile(id: TileId): Tile = Tile(id, tileSize, tileUrl(id))

  def tiledProjection(zoomLevel: Int, centralLongitude: Double): TiledProjection =
    new TiledProjection(zoomLevel, tileSize, centralLongitude)

  def optimalProjectionAndViewport(boundingBox: GeoBoundingBox, mapSize: MapSize): (TiledProjection, MapBox) = {
    @scala.annotation.tailrec
    def rec(zoomLevel: Int): (TiledProjection, MapBox) = {
      val tiledProjection = this.tiledProjection(zoomLevel, boundingBox.centralLongitude)
      val mapBBox = tiledProjection.mapProjection(boundingBox)
      if (zoomLevel <= minZoom || !(mapBBox.size fitsIn mapSize)) rec(zoomLevel - 1)
      else tiledProjection -> mapBBox.expandTo(mapSize)
    }
    rec(maxZoom)
  }

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
