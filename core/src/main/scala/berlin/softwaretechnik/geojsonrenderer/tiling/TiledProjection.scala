package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer.map._

import scala.math._

class TiledProjection(val zoomLevel: Int, val tileSize: Int, centralMeridianLongitude: Double) {

  def mapProjection: MapProjection =
    WebMercatorProjection(zoomLevel, tileSize)
      .normalizingLongitudesAround(centralMeridianLongitude)

  def tileCover(viewport: MapBox): Seq[TileId] =
    for {
      x <- tileCoordinates(viewport.left, viewport.right)
      y <- tileCoordinates(viewport.top, viewport.bottom)
    } yield TileId(x, y, zoomLevel)

  private def tileCoordinates(minMapCoordinate: Int, maxMapCoordinate: Int): Range =
    floorDiv(minMapCoordinate, tileSize) to floorDiv(maxMapCoordinate - 1, tileSize)

}
