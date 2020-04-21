package berlin.softwaretechnik.geojsonrenderer.geojson

import ujson.Value.Value

/**
 * Extracts style and other values from commonly used GeoJSON properties.
 *
 * @see https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0
 */
case class GeoJsonStyle(properties: Map[String, Value]) {
  def description: Option[String] = string("description")
  def fill: Option[String] = string("fill")
  def fillOpacity: Option[Double] = number("fill-opacity")
  def stroke: Option[String] = string("stroke")
  def strokeOpacity: Option[Double] = number("stroke-opacity")
  def strokeWidth: Option[Double] = number("stroke-width")
  def title: Option[String] = string("title")

  private def number(key: String): Option[Double] = properties.get(key).flatMap(_.numOpt)
  private def string(key: String): Option[String] = properties.get(key).flatMap(_.strOpt)
}
