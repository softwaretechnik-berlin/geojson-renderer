package berlin.softwaretechnik.geojsonrenderer
package geojson

import java.io.File

import ujson.Value.Value

object json extends upickle.AttributeTagged {
  override def tagName: String = "type"
}

import berlin.softwaretechnik.geojsonrenderer.geojson.json.{ReadWriter, macroRW}

case class FeatureCollection(features: Seq[Feature])

@upickle.implicits.key("FeatureCollection")
object FeatureCollection {
  implicit val rw: ReadWriter[FeatureCollection] = macroRW
}

@upickle.implicits.key("Feature")
case class Feature(geometry: Geometry, properties: Map[String, Value])

object Feature {
  implicit val rw: ReadWriter[Feature] = macroRW
}

sealed trait Geometry

object Geometry {
  implicit val rw: ReadWriter[Geometry] = macroRW

}

@upickle.implicits.key("Point")
sealed case class Point(coordinates: GeoCoord) extends Geometry

object Point {
  implicit val rw: ReadWriter[Point] = macroRW

}

@upickle.implicits.key("MultiPoint")
sealed case class MultiPoint(coordinates: Seq[GeoCoord]) extends Geometry

object MultiPoint {
  implicit val rw: ReadWriter[MultiPoint] = macroRW

}

@upickle.implicits.key("LineString")
sealed case class LineString(coordinates: Seq[GeoCoord]) extends Geometry

object LineString {
  implicit val rw: ReadWriter[LineString] = macroRW
}

@upickle.implicits.key("MultiLineString")
sealed case class MultiLineString(coordinates: Seq[Seq[GeoCoord]]) extends Geometry

object MultiLineString {
  implicit val rw: ReadWriter[MultiLineString] = macroRW
}

@upickle.implicits.key("Polygon")
sealed case class Polygon(coordinates: Seq[Seq[GeoCoord]]) extends Geometry

object Polygon {
  implicit val rw: ReadWriter[Polygon] = macroRW
}

@upickle.implicits.key("MultiPolygon")
sealed case class MultiPolygon(coordinates: Seq[Seq[Seq[GeoCoord]]]) extends Geometry

object MultiPolygon {
  implicit val rw: ReadWriter[MultiPolygon] = macroRW
}


object GeoJson {
  def load(file: File): FeatureCollection = {

    if (!file.exists()) {
      System.err.println("File not found")
      System.exit(4)
    }

    read(ujson.Readable.fromFile(file))
  }

  def read(readable: ujson.Readable): FeatureCollection =
    json.read[FeatureCollection](readable)
}
