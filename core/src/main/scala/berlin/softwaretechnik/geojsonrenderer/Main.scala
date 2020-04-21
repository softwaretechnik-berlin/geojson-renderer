package berlin.softwaretechnik.geojsonrenderer

import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import berlin.softwaretechnik.geojsonrenderer.MissingJdkMethods.replaceExtension
import berlin.softwaretechnik.geojsonrenderer.geojson.{GeoJson, GeoJsonSpatialOps}
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

    val inputFile = Paths.get(Conf.inputFile.getOrElse(???))
    val geoJson: GeoJson = GeoJson.load(inputFile)

    // TODO Why curl?
    System.setProperty("http.agent", "curl/7.66.0")

    val svgContent = render(
      screenDimensions = Conf.dimensions.getOrElse(???),
      geoJson
    )
    Files.write(
      replaceExtension(inputFile, ".svg"),
      svgContent.getBytes(StandardCharsets.UTF_8)
    )
    if (Conf.png.getOrElse(???))
      saveAsPng(svgContent, replaceExtension(inputFile, ".png"))
  }

  def render(screenDimensions: Dimensions, geoJson: GeoJson): String = {
    val boundingBox = GeoJsonSpatialOps.determineBoundingBox(geoJson)

    val (bitMapBox, tiledProjection) = tilingScheme.optimalZoomLevel(boundingBox, screenDimensions)
    println(s"Best zoom level ${tiledProjection.zoomLevel}")

    val offset: Position2D = (screenDimensions.toVector - bitMapBox.dimensions.toVector) * 0.5

    val viewport = Box2D(
      bitMapBox.upperLeft - offset,
      bitMapBox.upperLeft - offset + screenDimensions.toVector
    )

    val tiles: Seq[PositionedTile] = tiledProjection.tileCover(viewport)

    Svg.render(tiledProjection.geoProjection, viewport, tiles, geoJson)
  }

  private def saveAsPng(svgContent: String, path: Path): Unit =
    new PNGTranscoder().transcode(
      new TranscoderInput(new StringReader(svgContent)),
      new TranscoderOutput(Files.newOutputStream(path))
    )

}
