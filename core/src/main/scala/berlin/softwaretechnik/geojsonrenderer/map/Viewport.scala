package berlin.softwaretechnik.geojsonrenderer.map

import berlin.softwaretechnik.geojsonrenderer.GeoBoundingBox

case class Viewport(zoomLevel: Int, projection: MapProjection, box: MapBox)

object Viewport {

  def optimal(boundingBox: GeoBoundingBox, mapSize: MapSize, tilingScheme: TilingScheme): Viewport = {
    @scala.annotation.tailrec
    def rec(zoomLevel: Int): Viewport = {
      val normalizingProjection =
        tilingScheme
          .projection(zoomLevel)
          .normalizingLongitudesAround(boundingBox.centralLongitude)
      val mapBBox = normalizingProjection(boundingBox)

      if (!(mapBBox.size fitsIn mapSize) && zoomLevel > tilingScheme.minZoom) rec(zoomLevel - 1)
      else {
        val viewportBox = mapBBox.expandTo(mapSize)
        Viewport(zoomLevel, normalizingProjection.relativeTo(MapCoordinates(viewportBox.left, viewportBox.top)), viewportBox)
      }
    }
    rec(tilingScheme.maxZoom)
  }

}
