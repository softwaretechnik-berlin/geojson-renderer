package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.geojson.{Feature, FeatureCollection, GeoJson, GeoJsonStyle, Geometry, MultiPoint, Point}
import berlin.softwaretechnik.geojsonrenderer.tiling.{PositionedTile, TilingScheme, ZoomLevel}

import scala.xml._

object Svg {

  def render(projectedBox: Box2D, zoomLevel: ZoomLevel, tilingScheme: TilingScheme, tiles: Seq[PositionedTile], geoJson: GeoJson): String = {
    <svg
      width={projectedBox.width.toString}
      height={projectedBox.height.toString}
      version="1.1"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink">
  <g id="tiles">{imagesForTiles(tilingScheme, zoomLevel, tiles)}
  </g>
  <g id="features">{renderGeoJson(zoomLevel, projectedBox, geoJson)}
  </g>
</svg>.toString()
  }

  private def imagesForTiles(tilingScheme: TilingScheme,
                             zoomLevel: ZoomLevel,
                             tiles: Seq[PositionedTile]): NodeSeq =
    tiles.flatMap { tile =>
      Seq(
        Text("\n    "),
          <image xlink:href={tilingScheme.url(tile.tileId)}
                 x={tile.position.x.toString}
                 y={tile.position.y.toString}
                 width={s"${zoomLevel.tileSize}px"}
                 height={s"${zoomLevel.tileSize}px"}/>
      )
    }

  private def renderGeoJson(zoomLevel: ZoomLevel,
                            projectedBox: Box2D,
                            geoJson: GeoJson): NodeSeq = {
    val features = geoJson match {
      case geometry: Geometry => Seq(Feature(geometry, Map.empty))
      case feature: Feature => Seq(feature)
      case FeatureCollection(features) => features
    }
    features.flatMap(renderFeature(zoomLevel, projectedBox, _))
  }

  private def renderFeature(zoomLevel: ZoomLevel,
                            projectedBox: Box2D,
                            feature: Feature): Seq[Node] = {
    val element = feature.geometry match {
      case Point(gc) =>
        val point = zoomLevel.geoProjection.bitmapPosition(gc) - projectedBox.upperLeft
          <circle cx={point.x.toString} cy={point.y.toString} r="3" />

      case MultiPoint(points) =>
        <g>
          {points.map { geoCoord =>
          val point = zoomLevel.geoProjection.bitmapPosition(geoCoord) - projectedBox.upperLeft
            <circle cx={point.x.toString} cy={point.y.toString} r="3"
            />
        }}
        </g>

      case geojson.LineString(coordinates) =>
        <g>
          <polyline
          points={coordinates.map(zoomLevel.geoProjection.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
          fill="None"/>
        </g>
      case geojson.MultiLineString(lines) =>
        <g>{
          lines.map { coordinates =>
              <polyline
              points={coordinates.map(zoomLevel.geoProjection.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
              fill="None"
              />
          }}
        </g>

      case geojson.Polygon(coordinates) =>
          <polygon
          points={coordinates(0).map(zoomLevel.geoProjection.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
          />

      case geojson.MultiPolygon(lines) =>
        <g>{
          lines.map { coordinates =>
              <polygon
              points={coordinates(0).map(zoomLevel.geoProjection.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
              />
          }}
        </g>
    }

    val style: GeoJsonStyle = GeoJsonStyle(feature.properties)
    Seq(
      Text("\n    "),
      element.copy(child = element.child
        :+ <title>{style.title}</title>
        :+ <desc>{style.description}</desc>
      ) %
        Attribute(null, "stroke", style.stroke, Null) %
        Attribute(null, "stroke-opacity", style.strokeOpacity.toString, Null) %
        Attribute(null, "stroke-width", style.strokeWidth.toString, Null) %
        Attribute(null, "fill", style.fill, Null) %
        Attribute(null, "fill-opacity", style.fillOpacity.toString, Null)
    )
  }

}
