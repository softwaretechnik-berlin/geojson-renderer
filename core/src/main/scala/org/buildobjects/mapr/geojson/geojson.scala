package org.buildobjects.mapr.geojson {

  import java.io.File

  import ujson.Value.Value

  object TypeTag extends upickle.AttributeTagged {
    override def tagName: String = "type"
  }

  import org.buildobjects.mapr.geojson.TypeTag.{macroRW, ReadWriter => RW}

  case class FeatureCollection(features: Seq[Feature])

  @upickle.implicits.key("FeatureCollection")
  object FeatureCollection {
    implicit val rw: RW[FeatureCollection] = macroRW
  }

  @upickle.implicits.key("Feature")
  case class Feature(geometry: Geometry, properties: Map[String, Value])

  object Feature {
    implicit val rw: RW[Feature] = macroRW
  }

  sealed trait Geometry

  object Geometry {
    implicit val rw: RW[Geometry] = macroRW

  }

  @upickle.implicits.key("Point")
  sealed case class Point(coordinates: Seq[Double]) extends Geometry

  object Point {
    implicit val rw: RW[Point] = macroRW

  }

  @upickle.implicits.key("MultiPoint")
  sealed case class MultiPoint(coordinates: Seq[Seq[Double]]) extends Geometry

  object MultiPoint {
    implicit val rw: RW[MultiPoint] = macroRW

  }

  @upickle.implicits.key("LineString")
  sealed case class LineString(coordinates: Seq[Seq[Double]]) extends Geometry

  object LineString {
    implicit val rw: RW[LineString] = macroRW
  }

  @upickle.implicits.key("MultiLineString")
  sealed case class MultiLineString(coordinates: Seq[Seq[Seq[Double]]]) extends Geometry

  object MultiLineString {
    implicit val rw: RW[MultiLineString] = macroRW
  }

  @upickle.implicits.key("Polygon")
  sealed case class Polygon(coordinates: Seq[Seq[Seq[Double]]]) extends Geometry

  object Polygon {
    implicit val rw: RW[Polygon] = macroRW
  }

  @upickle.implicits.key("MultiPolygon")
  sealed case class MultiPolygon(coordinates: Seq[Seq[Seq[Seq[Double]]]]) extends Geometry

  object MultiPolygon {
    implicit val rw: RW[MultiPolygon] = macroRW
  }



  object GeoJson {
    def load(file: File): FeatureCollection = {

      if (!file.exists()) {
        System.err.println("File not found")
        System.exit(4)
      }

      TypeTag.read[FeatureCollection](ujson.Readable.fromFile(file))
    }
  }

}
