package berlin.softwaretechnik.geojsonrenderer.geojson

import ujson.Value.Value

/**
 * Extracts style and other values from commonly used GeoJSON properties.
 *
 * @see https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0
 */
case class GeoJsonStyle(properties: Map[String, Value]) {
  def stroke: String = lookup("stroke", _.strOpt, "#555555")
  def strokeOpacity: Double = lookup("stroke-opacity", _.numOpt, 1)
  def fill: String = lookup("fill", _.strOpt, "#555555")
  def fillOpacity: Double = lookup("fill-opacity", _.numOpt, 0.6)
  def strokeWidth: Double = lookup("stroke-width", _.numOpt, 3)
  def title: String = lookup("title", _.strOpt, "")
  def description: String = lookup("description", _.strOpt, "")

  private def lookup[A](key: String, value: Value => Option[A], default: A): A =
    properties.get(key).flatMap(value).getOrElse(default)
}
