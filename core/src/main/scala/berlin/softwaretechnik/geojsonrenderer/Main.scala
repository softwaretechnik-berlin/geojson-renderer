package berlin.softwaretechnik.geojsonrenderer

import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, NoSuchFileException, Path, Paths}

import berlin.softwaretechnik.geojsonrenderer.MissingJdkMethods.replaceExtension
import berlin.softwaretechnik.geojsonrenderer.geojson._
import berlin.softwaretechnik.geojsonrenderer.map._
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}
import org.rogach.scallop.ScallopConf

object Main {

  // TODO args for tiling scheme
  //val tiledMap = TilingScheme.here("VgTVFr1a0ft1qGcLCVJ6", "LJXqQ8ErW71UsRUK3R33Ow")
  val tilingScheme = TilingScheme.rrze()

  def main(args: Array[String]): Unit =
    System.exit(mainWithExitStatus(args))

  def mainWithExitStatus(args: Array[String]): Int = {

    /*
      TODO: replace --dimension flag with the following:
        - when `--width` or `--height` is specified, use that exact number of pixels
        - when `--max-width` or `--max-height` is specified, use a zoom level that gets us close to that many pixels, but don't add extra margin
        - when only one of height or width is specified, use the specified dimension to determine the zoom level, and compute the other dimension from bounding box + margin
        - error when specifying both --max-foo and --foo
        - when no dimensional arguments are specified, default to --max-width 1200 --max-height 800

      TODO: Specify margin
        - `--margin` to specify a number of pixels to use as a (minimum) margin.
     */

    object Conf extends ScallopConf(args) {
      banner("""Usage: geojson2svg [OPTION]... [input-file]
               |geojson2svg renders a geojson file to svg
               |Options:
               |""".stripMargin)
      val dimensions = opt[String]("dimensions", descr= "the dimensions of the target file in pixels",  default = Some("1200x800")).map(s => MapSize(s))
      val png = opt[Boolean]("png", descr = "render the resulting svg into png", default = Some(false))
      val inputFile = trailArg[String](descr = "geojson input file")
      verify
    }

    val inputFile = Paths.get(Conf.inputFile.getOrElse(???))
    val geoJson: GeoJson =
      try GeoJson.load(inputFile)
      catch { case e: NoSuchFileException =>
        System.err.println(e.getMessage)
        return 1
      }

    val mapSize: MapSize = Conf.dimensions.getOrElse(???)
    val svgContent = render(mapSize, geoJson)
    Files.write(
      replaceExtension(inputFile, ".svg"),
      svgContent.getBytes(StandardCharsets.UTF_8)
    )

    if (Conf.png.getOrElse(???)) {
      System.setProperty("http.agent", "curl/7.66.0") // TODO Why curl?
      saveAsPng(svgContent, replaceExtension(inputFile, ".png"))
    }

    0
  }

  def render(mapSize: MapSize, geoJson: GeoJson): String = {
    val boundingBox = GeoJsonSpatialOps.boundingBox(geoJson)
    val viewport = Viewport.optimal(boundingBox, mapSize, tilingScheme)
    val tiles = tilingScheme.tileCover(viewport)
    new Svg(viewport).render(tiles, geoJson)
  }

  private def saveAsPng(svgContent: String, path: Path): Unit =
    new PNGTranscoder().transcode(
      new TranscoderInput(new StringReader(svgContent)),
      new TranscoderOutput(Files.newOutputStream(path))
    )

}
