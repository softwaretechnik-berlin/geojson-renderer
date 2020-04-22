package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer.GeoBoundingBox
import berlin.softwaretechnik.geojsonrenderer.map.{MapBox, MapCoordinates, MapProjection, MapSize, WebMercatorProjection}

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

      if (zoomLevel > tilingScheme.minZoom && !(mapBBox.size fitsIn mapSize)) rec(zoomLevel - 1)
      else {
        val viewportBox = mapBBox.expandTo(mapSize)
        Viewport(zoomLevel, normalizingProjection.relativeTo(MapCoordinates(viewportBox.left, viewportBox.top)), viewportBox)
      }
    }
    rec(tilingScheme.maxZoom)
  }

}
