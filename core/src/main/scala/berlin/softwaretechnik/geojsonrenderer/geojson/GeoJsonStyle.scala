package berlin.softwaretechnik.geojsonrenderer.geojson

import ujson.Value.Value

case class GeoJsonStyle(
  stroke: String,
  strokeOpacity: String,
  fill: String,
  fillOpacity: String,
  strokeWidth: String,
  title: String,
  description: String
)

object GeoJsonStyle {
  def apply(properties: Map[String, Value]): GeoJsonStyle = {
    def lookup[A](key: String, value: Value => Option[A], default: A): A =
      properties.get(key).flatMap(value).getOrElse(default)

    GeoJsonStyle(
      lookup("stroke", _.strOpt, "#555555"),
      lookup("stroke-opacity", _.numOpt, 1).toString,
      lookup("fill", _.strOpt, "#555555"),
      lookup("fill-opacity", _.numOpt, 0.6).toString,
      lookup("stroke-width", _.numOpt, 3).toString,
      lookup("title", _.strOpt, ""),
      lookup("description", _.strOpt, ""),
    )
  }
}
