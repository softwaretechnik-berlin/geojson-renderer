package berlin.softwaretechnik.geojsonrenderer.tiling

import berlin.softwaretechnik.geojsonrenderer.Position2D

case class PositionedTile(id: TileId, position: Position2D, size: Int, url: String)
