package berlin.softwaretechnik.geojsonrenderer.geojson

import ujson.Value.Value

/**
 * GeoJSON style, derived from commonly used properties.
 *
 * @see https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0
 */
case class GeoJsonStyle(
  stroke: String,
  strokeOpacity: Double,
  fill: String,
  fillOpacity: Double,
  strokeWidth: Double,
  title: String,
  description: String
)

object GeoJsonStyle {
  def apply(properties: Map[String, Value]): GeoJsonStyle = {
    def lookup[A](key: String, value: Value => Option[A], default: A): A =
      properties.get(key).flatMap(value).getOrElse(default)

    GeoJsonStyle(
      lookup("stroke", _.strOpt, "#555555"),
      lookup("stroke-opacity", _.numOpt, 1),
      lookup("fill", _.strOpt, "#555555"),
      lookup("fill-opacity", _.numOpt, 0.6),
      lookup("stroke-width", _.numOpt, 3),
      lookup("title", _.strOpt, ""),
      lookup("description", _.strOpt, ""),
    )
  }
}
