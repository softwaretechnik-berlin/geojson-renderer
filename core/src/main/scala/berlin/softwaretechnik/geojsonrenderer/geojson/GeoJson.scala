package berlin.softwaretechnik.geojsonrenderer
package geojson

import java.io.File

import ujson.Value.Value

private object json extends upickle.AttributeTagged {
  override def tagName: String = "type"
}

import berlin.softwaretechnik.geojsonrenderer.geojson.json.{ReadWriter, macroRW}

sealed trait GeoJson

@upickle.implicits.key("FeatureCollection")
case class FeatureCollection(features: Seq[Feature]) extends GeoJson

object FeatureCollection {
  private[geojson] implicit val rw: ReadWriter[FeatureCollection] = macroRW
}

@upickle.implicits.key("Feature")
case class Feature(geometry: Geometry, properties: Map[String, Value]) extends GeoJson

object Feature {
  private[geojson] implicit val rw: ReadWriter[Feature] = macroRW
}

sealed trait Geometry extends GeoJson

object Geometry {
  private[geojson] implicit val rw: ReadWriter[Geometry] = macroRW
}

@upickle.implicits.key("Point")
sealed case class Point(coordinates: GeoCoord) extends Geometry

object Point {
  private[geojson] implicit val rw: ReadWriter[Point] = macroRW
}

@upickle.implicits.key("MultiPoint")
sealed case class MultiPoint(coordinates: Seq[GeoCoord]) extends Geometry

object MultiPoint {
  private[geojson] implicit val rw: ReadWriter[MultiPoint] = macroRW
}

@upickle.implicits.key("LineString")
sealed case class LineString(coordinates: Seq[GeoCoord]) extends Geometry

object LineString {
  private[geojson] implicit val rw: ReadWriter[LineString] = macroRW
}

@upickle.implicits.key("MultiLineString")
sealed case class MultiLineString(coordinates: Seq[Seq[GeoCoord]]) extends Geometry

object MultiLineString {
  private[geojson] implicit val rw: ReadWriter[MultiLineString] = macroRW
}

@upickle.implicits.key("Polygon")
sealed case class Polygon(coordinates: Seq[Seq[GeoCoord]]) extends Geometry

object Polygon {
  private[geojson] implicit val rw: ReadWriter[Polygon] = macroRW
}

@upickle.implicits.key("MultiPolygon")
sealed case class MultiPolygon(coordinates: Seq[Seq[Seq[GeoCoord]]]) extends Geometry

object MultiPolygon {
  private[geojson] implicit val rw: ReadWriter[MultiPolygon] = macroRW
}

object GeoJson {
  private implicit val rw: ReadWriter[GeoJson] = macroRW

  def load(file: File): GeoJson = {

    if (!file.exists()) {
      System.err.println("File not found")
      System.exit(4)
    }

    read(ujson.Readable.fromFile(file))
  }

  def read(readable: ujson.Readable): GeoJson =
    json.read[GeoJson](readable)

  def write(featureCollection: GeoJson): String =
    json.write(featureCollection)
}
