package berlin.softwaretechnik.geojsonrenderer
package tiling

class TilingScheme(minZoom: Int, maxZoom: Int, tileSize: Int, tileUrl: TileId => String) {

  def zoomLevels: IndexedSeq[ZoomLevel] =
    (minZoom to maxZoom)
      .map(zoomLevel => {
        new ZoomLevel(
          zoomLevel,
          tileSize
        )
      })

  def url(tileId: TileId): String = tileUrl(tileId.normalized)
}

object TilingScheme {

  def osm() = new TilingScheme(0, 19, 256, tileId =>
    s"http://tile.openstreetmap.org/${tileId.z}/${tileId.x}/${tileId.y}.png"
  )
  def rrze() = new TilingScheme(0, 19, 256, tileId =>
    s"https://c.osm.rrze.fau.de/osmhd/${tileId.z}/${tileId.x}/${tileId.y}.png"
  )

  def here(appId: String, appCode: String) = new TilingScheme(0, 19, 512, tileId =>
    s"https://1.base.maps.api.here.com/maptile/2.1/maptile/844fb05a40/normal.day/${tileId.z}/${tileId.x}/${tileId.y}/512/png8?app_id=${appId}&app_code=${appCode}&lg=eng&ppi=320&pview=DEF"
  )
}
