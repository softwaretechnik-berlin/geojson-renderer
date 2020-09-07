package berlin.softwaretechnik.geojsonrenderer

import java.io.{ByteArrayOutputStream, InputStream, StringReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import berlin.softwaretechnik.geojsonrenderer.MissingJdkMethods.replaceExtension
import berlin.softwaretechnik.geojsonrenderer.geojson._
import berlin.softwaretechnik.geojsonrenderer.map._
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}
import org.rogach.scallop.ScallopConf

import scala.collection.compat.immutable
import scala.io.AnsiColor

object Main {

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

  def main(args: Array[String]): Unit = {
    val exitCode =
      try {
        run(new Conf(immutable.ArraySeq.unsafeWrapArray(args)))
        0
      } catch {
        case err: GeoJsonRendererError =>
          printError(AnsiColor.RED + err.message + AnsiColor.RESET)
          1
        case e: Exception =>
          printError(AnsiColor.RED + e.getMessage + AnsiColor.RESET)
          127
      }

    System.exit(exitCode)
  }

  def run(conf: Conf): Unit = {

    val input = conf.inputFile()

    val geoJson: GeoJson =
      try GeoJson.load(input.in)
      catch {
        case e: ujson.ParseException =>
          throw new GeoJsonRendererError(s"Error: Could not parse '${input.name}': ${e.getMessage}.")
        case e: upickle.core.AbortException =>
          throw new GeoJsonRendererError(s"Error: Could not parse GeoJSON from '${input.name}': ${e.getMessage}.")
      }

    val mapSize: MapSize = conf.dimensions()

    if (!conf.tileUrlTemplate.isSupplied) {
      printWarning(AnsiColor.YELLOW + "Warning: No tile-url-template defined. Falling back to OpenStreetMap tile server. Make sure you adhere" +
        " to the usage policy: https://operations.osmfoundation.org/policies/tiles/." + AnsiColor.RESET)
    }

    val tilingScheme = TilingScheme.template(conf.tileUrlTemplate())

    val svgContent = render(mapSize, geoJson, tilingScheme)
    val outputFormat = if (conf.png()) PNGFormat else SVGFormat
    val output = input.matchingOutput

    // Setting user agent to curl, so that batik can pull map tiles.
    System.setProperty("http.agent", "curl/7.66.0")
    // Avoid starting a window
    System.setProperty("java.awt.headless", "true")
    output.write(svgContent, outputFormat)
  }

  private def printError(message: String): Unit =
    Console.err.println(AnsiColor.RED + message + AnsiColor.RESET)

  private def printWarning(message: String): Unit =
    Console.err.println(AnsiColor.YELLOW + message + AnsiColor.RESET)

  private def render(mapSize: MapSize, geoJson: GeoJson, tilingScheme: TilingScheme): String = {
    val boundingBox = GeoJsonSpatialOps.boundingBox(geoJson)
    val viewport = Viewport.optimal(boundingBox, mapSize, tilingScheme)
    val tiles = tilingScheme.tileCover(viewport)
    new Svg(viewport).render(tiles, geoJson)
  }

  class Conf(args: Seq[String]) extends ScallopConf(args) {

    banner(
      """Usage: geojson-renderer [OPTION]... [input-file]
        |geojson-renderer renders a GeoJSON file to SVG and PNG images.
        |
        |Options:
        |""".stripMargin)

    errorMessageHandler = { message =>
      printError(s"Error: $message\n")
      builder.printHelp
      sys.exit(1)
    }

    val dimensions = opt[String]("dimensions", descr = "The dimensions of the target file in pixels.", default = Some("1200x800")).map(s => MapSize(s))
    val png = opt[Boolean]("png", descr = "Render the resulting svg into png.", default = Some(false))
    val tileUrlTemplate = opt[String]("tile-url-template", descr =
      "Template for tile URLs, placeholders are {tile} for tile coordinate, {a-c} and {1-4} for load balancing."
      , default = Some("http://{a-c}.tile.openstreetmap.org/{tile}.png"))
    val inputFile = trailArg[String](descr = "GeoJSON input file or - for standard input")
      .map(str => if (str == "-") StdInput else FileInput(Paths.get(str)))

    addValidation(inputFile() match {
      case input: FileInput =>
        if (Files.exists(input.file)) Right(()) else Left(s"File '${input.file}' does not exist.")
      case _ => Right(())
    })

    verify()
  }

}

class GeoJsonRendererError(val message: String) extends Exception(message)

sealed trait GeoJsonInput {
  val name: String

  def in: InputStream

  def matchingOutput: ImageOutput
}

case class FileInput(file: Path) extends GeoJsonInput {
  override val name: String = file.toString

  override def in: InputStream = Files.newInputStream(file)

  override def matchingOutput: ImageOutput =
    MirroredFileOutput(file)
}

case object StdInput extends GeoJsonInput {
  override val name: String = "STDIN"

  override def in: InputStream = System.in

  override def matchingOutput: ImageOutput = StdOutput
}

sealed abstract class OutputFormat(val extension: String) {
  def convert(svgContent: String): Array[Byte]
}

case object SVGFormat extends OutputFormat("svg") {
  override def convert(svgContent: String): Array[Byte] = svgContent.getBytes(StandardCharsets.UTF_8)
}

case object PNGFormat extends OutputFormat("png") {
  override def convert(svgContent: String): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    new PNGTranscoder().transcode(
      new TranscoderInput(new StringReader(svgContent)),
      new TranscoderOutput(out)
    )
    out.toByteArray
  }
}

sealed trait ImageOutput {
  val name: String

  def write(svgContent: String, format: OutputFormat): Unit
}

case class MirroredFileOutput(inputFile: Path) extends ImageOutput {
  override val name: String = inputFile.toString

  override def write(svgContent: String, format: OutputFormat): Unit = {
    val outputFile = replaceExtension(inputFile, s".${format.extension}")
    Files.write(outputFile, format.convert(svgContent))
  }
}

case object StdOutput extends ImageOutput {
  override val name: String = "STDOUT"

  override def write(svgContent: String, format: OutputFormat): Unit =
    System.out.write(format.convert(svgContent))
}