package berlin.softwaretechnik.geojsonrenderer

import berlin.softwaretechnik.geojsonrenderer.geojson.{Feature, FeatureCollection, GeoJson, GeoJsonStyle, Geometry, MultiPoint, Point}
import berlin.softwaretechnik.geojsonrenderer.tiling.{GeoProjection, PositionedTile}

import scala.xml._

object Svg {

  def render(geoProjection: GeoProjection, viewport: Box2D, tiles: Seq[PositionedTile], geoJson: GeoJson): String = XmlHelpers.prettyPrint(
    <svg
      width={viewport.width.toString}
      height={viewport.height.toString}
      version="1.1"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink">
  <g id="tiles">{renderTiles(viewport, tiles)}
  </g>
  <g id="features">{renderGeoJson(geoProjection.relativeTo(viewport), geoJson)}
  </g>
</svg>
  )

  private def renderTiles(viewport: Box2D, tiles: Seq[PositionedTile]): NodeSeq =
    tiles.flatMap { tile =>
      Seq(
        Text("\n    "),
          <image xlink:href={tile.url}
                 x={(tile.leftXPosition - viewport.left).toString}
                 y={(tile.topYPosition - viewport.top).toString}
                 width={s"${tile.size}px"}
                 height={s"${tile.size}px"}/>
      )
    }

  private def renderGeoJson(geoProjection: GeoProjection, geoJson: GeoJson): NodeSeq = {
    val features = geoJson match {
      case geometry: Geometry => Seq(Feature(geometry, Map.empty))
      case feature: Feature => Seq(feature)
      case FeatureCollection(features) => features
    }
    features.flatMap(renderFeature(geoProjection, _))
  }

  private def renderFeature(geoProjection: GeoProjection, feature: Feature): Seq[Node] = {
    val element = feature.geometry match {
      case Point(gc) =>
        val point = geoProjection.bitmapPosition(gc)
          <circle cx={point.x.toString} cy={point.y.toString} r="3" />

      case MultiPoint(points) =>
        <g>
          {points.map { geoCoord =>
          val point = geoProjection.bitmapPosition(geoCoord)
            <circle cx={point.x.toString} cy={point.y.toString} r="3"
            />
        }}
        </g>

      case geojson.LineString(coordinates) =>
        <g>
          <polyline
          points={coordinates.map(geoProjection.bitmapPosition).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
          fill="None"/>
        </g>
      case geojson.MultiLineString(lines) =>
        <g>{
          lines.map { coordinates =>
              <polyline
              points={coordinates.map(geoProjection.bitmapPosition).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
              fill="None"
              />
          }}
        </g>

      case geojson.Polygon(coordinates) =>
          <polygon
          points={coordinates(0).map(geoProjection.bitmapPosition).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
          />

      case geojson.MultiPolygon(lines) =>
        <g>{
          lines.map { coordinates =>
              <polygon
              points={coordinates(0).map(geoProjection.bitmapPosition).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
              />
          }}
        </g>
    }

    val style: GeoJsonStyle = GeoJsonStyle(feature.properties)
    Seq(
      Text("\n    "),
      element.copy(
        child = (
        style.title.map(t => <title>{t}</title>) ++
        style.description.map(t => <desc>{t}</desc>) ++
        element.child
      ).toSeq) %
        attribute("stroke", style.stroke.getOrElse("#555555")) %
        attribute("stroke-opacity", style.strokeOpacity.map(_.toString)) %
        Attribute(null, "stroke-width", style.strokeWidth.getOrElse(3).toString, Null) %
        attribute("fill", style.fill.getOrElse("#555555")) %
        Attribute(null, "fill-opacity", style.fillOpacity.getOrElse(0.6).toString, Null)
    )
  }

  def attribute(name: String, value: String): MetaData = Attribute(null, name, value, Null)
  def attribute(name: String, value: Option[String]): MetaData = value.fold[MetaData](Null)(attribute(name, _))
}
