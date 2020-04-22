package berlin.softwaretechnik.geojsonrenderer
package tiling

class TilingScheme(minZoom: Int, maxZoom: Int, tileSize: Int, tileUrl: TileId => String) {

  def tiledProjection(zoomLevel: Int, centralLongitude: Double): TiledProjection =
    new TiledProjection(zoomLevel, tileSize, tileUrl, centralLongitude)

  def optimalViewportAndProjection(boundingBox: BoundingBox, mapSize: MapSize): (Box2D, TiledProjection) = {
    (minZoom to maxZoom).reverse
      .map { zoomLevel =>
        val tiledProjection = this.tiledProjection(zoomLevel, boundingBox.centralLongitude)
        tiledProjection.mapProjection.viewport(boundingBox) -> tiledProjection
      }.collectFirst {
        case (mapBBox, tp) if mapBBox.width <= mapSize.width && mapBBox.height <= mapSize.height =>
          mapBBox.expandTo(mapSize) -> tp
      }.get
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
