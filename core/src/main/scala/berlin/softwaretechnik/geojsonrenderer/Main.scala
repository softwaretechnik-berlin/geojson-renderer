package berlin.softwaretechnik.geojsonrenderer

import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, NoSuchFileException, Path, Paths}

import berlin.softwaretechnik.geojsonrenderer.MissingJdkMethods.replaceExtension
import berlin.softwaretechnik.geojsonrenderer.geojson._
import berlin.softwaretechnik.geojsonrenderer.map._
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}
import org.rogach.scallop.{ScallopConf}

object Main {

  def main(args: Array[String]): Unit =
    System.exit(mainWithExitStatus(args))

  val template= s"https://1.base.maps.api.here.com/maptile/2.1/maptile/newest/normal.day/{tile}/256/png8?app_id=VgTVFr1a0ft1qGcLCVJ6&app_code=LJXqQ8ErW71UsRUK3R33Ow&lg=eng&ppi=320&pview=DEF"
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

      banner("""Usage: geojson-renderer [OPTION]... [input-file]
               |geojson-renderer renders a geojson file to svg and optionally to png.
               |
               |Options:
               |""".stripMargin)

      errorMessageHandler = { message =>

        Console.err.println("Error: %s\n" format message)

        builder.printHelp
        sys.exit(1)
      }

      val dimensions = opt[String]("dimensions", descr= "The dimensions of the target file in pixels.",  default = Some("1200x800")).map(s => MapSize(s))
      val png = opt[Boolean]("png", descr = "Render the resulting svg into png.", default = Some(false))
      val tileUrlTemplate = opt[String]("tile-url-template", descr =
        "Template for tile URLs, placeholders are {tile} for tile coordinate, {a-c} and {1-4} for load balancing."
          , default = Some("http://{a-c}.tile.openstreetmap.org/{tile}.png"))
      val inputFile = trailArg[String](descr = "Geojson input file.")

      verify
    }

    val inputFileOpt: String = Conf.inputFile()

    val inputFile = Paths.get(inputFileOpt)

    val geoJson: GeoJson =
      try GeoJson.load(inputFile)
      catch { case e: NoSuchFileException =>
        System.err.println(s"Error: File '${inputFile.toString}' does not exist.")
        return 1
      case e:ujson.ParseException =>
        System.err.println(s"Error: Could not parse '${inputFile.toString}': ${e.getMessage}.")
        return 1
      case e: upickle.core.AbortException =>
        System.err.println(s"Error: Could not parse geojson from '${inputFile.toString}': ${e.getMessage}.")
        return 1
      }

    val mapSize: MapSize = Conf.dimensions()

    if (!Conf.tileUrlTemplate.isSupplied) {
      System.err.println("Warning: No tile-url-template defined. Falling back to OpenStreetMap tile server. Make sure you adhere" +
        " to the usage policy: https://operations.osmfoundation.org/policies/tiles/.")
    }


    val tilingScheme = TilingScheme.template(Conf.tileUrlTemplate())

    val svgContent = render(mapSize, geoJson, tilingScheme)

    Files.write(
      replaceExtension(inputFile, ".svg"),
      svgContent.getBytes(StandardCharsets.UTF_8)
    )

    if (Conf.png()) {
      // Setting user agent to curl, so that batik can pull map tiles.
      System.setProperty("http.agent", "curl/7.66.0")
      saveAsPng(svgContent, replaceExtension(inputFile, ".png"))
    }

    0
  }

  def render(mapSize: MapSize, geoJson: GeoJson, tilingScheme: TilingScheme): String = {
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
