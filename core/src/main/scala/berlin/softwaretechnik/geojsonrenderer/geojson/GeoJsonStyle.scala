package berlin.softwaretechnik.geojsonrenderer.geojson

import ujson.{Num, Str}
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
    GeoJsonStyle(
      properties
        .collectFirst {
          case ("stroke", Str(stroke)) => stroke
        }
        .getOrElse("#555555"),
      properties
        .collectFirst {
          case ("stroke-opacity", Num(num)) => num.toString
        }
        .getOrElse("1"),
      properties
        .collectFirst {
          case ("fill", Str(fill)) => fill
        }
        .getOrElse("#555555"),
      properties
        .collectFirst {
          case ("fill-opacity", Num(num)) => num.toString
        }
        .getOrElse("0.6"),
      properties
        .collectFirst {
          case ("stroke-width", Num(strokeWidth)) => strokeWidth.toString
        }
        .getOrElse("3"),
      properties
        .collectFirst {
          case ("title", Num(strokeWidth)) => strokeWidth.toString
        }
        .getOrElse(""),
      properties
        .collectFirst {
          case ("description", Num(strokeWidth)) => strokeWidth.toString
        }
        .getOrElse("")
    )
  }
}
