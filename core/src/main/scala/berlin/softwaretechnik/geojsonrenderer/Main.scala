package berlin.softwaretechnik.geojsonrenderer

import java.io.{File, FileOutputStream, StringReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import berlin.softwaretechnik.geojsonrenderer.geojson.{Feature, FeatureCollection, GeoJson, Geometry, MultiPoint, Point}
import berlin.softwaretechnik.geojsonrenderer.tiling.{PositionedTile, TilingScheme}
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}
import org.rogach.scallop.ScallopConf

object Main {

  //val tiledMap = TilingScheme.here("VgTVFr1a0ft1qGcLCVJ6", "LJXqQ8ErW71UsRUK3R33Ow")
  val tilingScheme = TilingScheme.rrze()

  def main(args: Array[String]): Unit = {

    object Conf extends ScallopConf(args) {
      banner("""Usage: geojson2svg [OPTION]... [input-file]
               |geojson2svg renders a geojson file to svg
               |Options:
               |""".stripMargin)
      val dimensions = opt[String]("dimensions", descr= "the dimensions of the target file in pixels",  default = Some("1200x800")).map(s => Dimensions(s))
      val png = opt[Boolean]("png", descr = "render the resulting svg into png", default = Some(false))
      val inputFile = trailArg[String](descr = "geojson input file")
      verify
    }

    val basename = Conf.inputFile.getOrElse(???).reverse.dropWhile(x => x != '.').reverse

    System.setProperty("http.agent", "curl/7.66.0");

    val geoJson = GeoJson.load(new File(Conf.inputFile.getOrElse(???)))

    val svgContent = render(
      determineBoundingBox(geoJson),
      //  BoundingBox(13.335, 52.5033, 13.4265, 52.5276),
      screenDimensions = Conf.dimensions.getOrElse(???),
      geoJson
    )
    Files.write(
      Paths.get(s"${basename}svg"),
      svgContent.getBytes(StandardCharsets.UTF_8)
    )
    if (Conf.png.getOrElse(???))
      saveAsPng(svgContent, s"${basename}png")
  }

  def render(boundingBox: BoundingBox,
             screenDimensions: Dimensions,
             geoJson: GeoJson): String = {



    val (bitMapBox: Box2D, zoomLevel) =
      tilingScheme.zoomLevels.reverse
        .map(zoomLevel => {
          val bitmapBox: Box2D = zoomLevel.bitmapBox(boundingBox)
          (bitmapBox, zoomLevel)
        })
        .find {
          case (bitmapBox, _) =>
            bitmapBox.width <= screenDimensions.width && bitmapBox.height <= screenDimensions.height
        }
        .get

    println(s"Best zoom level ${zoomLevel.zoomLevel}")

    val offset: Position2D = (screenDimensions.toVector - bitMapBox.dimensions.toVector) * 0.5

    val projectedBox = Box2D(
      bitMapBox.upperLeft - offset,
      bitMapBox.upperLeft - offset + screenDimensions.toVector
    )

    val tiles: Seq[PositionedTile] = zoomLevel.tileCover(projectedBox)

    val projectedBoundingBox = zoomLevel.bitmapBox(boundingBox) - projectedBox.upperLeft

    println(s"${projectedBoundingBox}")

    Svg.render(projectedBox, zoomLevel, tilingScheme, tiles, geoJson)
  }

  private def saveAsPng(svgContent: String, filename: String): Unit =
    new PNGTranscoder().transcode(
      new TranscoderInput(new StringReader(svgContent)),
      new TranscoderOutput(new FileOutputStream(filename))
    )

  def determineBoundingBox(geoJson: GeoJson): BoundingBox = {

    def coordinates(geometry: Geometry): Seq[GeoCoord] =
      geometry match {
        case Point(geo) => Seq(geo)
        case MultiPoint(coordinates) => coordinates
        case geojson.LineString(coordinates) => coordinates
        case geojson.MultiLineString(lines) => lines.flatten
        case geojson.Polygon(rings) => rings.flatten
        case geojson.MultiPolygon(polygons) => polygons.flatten.flatten
      }

    def boundingBox(coordinates: Seq[GeoCoord]): BoundingBox = {
      val geos = LineString(coordinates).points
      BoundingBox(
        geos.map(_.lon).min,
        geos.map(_.lat).min,
        geos.map(_.lon).max,
        geos.map(_.lat).max
      )
    }

    boundingBox(geoJson match {
      case geometry: Geometry => coordinates(geometry)
      case Feature(geometry, _) => coordinates(geometry)
      case FeatureCollection(features) => features.flatMap(f => coordinates(f.geometry))
    })

  }
}
