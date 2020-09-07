package berlin.softwaretechnik.geojsonrenderer

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.Base64

import berlin.softwaretechnik.geojsonrenderer.MissingJdkMethods.replaceExtension
import berlin.softwaretechnik.geojsonrenderer.geojson._
import berlin.softwaretechnik.geojsonrenderer.map._
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}
import org.rogach.scallop.{ScallopConf, ScallopOption}

import scala.collection.compat.immutable
import scala.io.AnsiColor
import scala.xml.{Elem, XML}

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
          err.message.foreach { msg =>
            printError(AnsiColor.RED + msg + AnsiColor.RESET)
          }
          1
        case e: Exception =>
          printError(AnsiColor.RED + e.getMessage + AnsiColor.RESET)
          127
      }

    System.exit(exitCode)
  }

  def run(conf: Conf): Unit = {

    val input = conf.input()

    val geoJson: GeoJson =
      try GeoJson.load(input.in)
      catch {
        case e: ujson.ParseException =>
          throw new GeoJsonRendererError(
            s"Error: Could not parse '${input.name}': ${e.getMessage}."
          )
        case e: upickle.core.AbortException =>
          throw new GeoJsonRendererError(
            s"Error: Could not parse GeoJSON from '${input.name}': ${e.getMessage}."
          )
      }

    val mapSize: MapSize = conf.dimensions()

    if (!conf.tileUrlTemplate.isSupplied) {
      printWarning(
        AnsiColor.YELLOW + "Warning: No tile-url-template defined. Falling back to OpenStreetMap tile server. Make sure you adhere" +
          " to the usage policy: https://operations.osmfoundation.org/policies/tiles/." + AnsiColor.RESET
      )
    }

    // Setting user agent to curl, so that batik can pull map tiles.
    System.setProperty("http.agent", "curl/7.66.0")
    // Avoid starting a window
    System.setProperty("java.awt.headless", "true")

    val output = conf.output.getOrElse(input.matchingOutput)
    val format = conf.outputFormat()
    val tilingScheme = TilingScheme.template(conf.tileUrlTemplate())

    val boundingBox = GeoJsonSpatialOps.boundingBox(geoJson)
    val viewport = Viewport.optimal(boundingBox, mapSize, tilingScheme)
    val svg = new Svg(viewport, tilingScheme, geoJson, format.imagePolicy)

    output.write(svg, format)
  }

  private def printError(message: String): Unit =
    Console.err.println(AnsiColor.RED + message + AnsiColor.RESET)

  private def printWarning(message: String): Unit =
    Console.err.println(AnsiColor.YELLOW + message + AnsiColor.RESET)

  class Conf(
      args: Seq[String],
      defaultTileLoader: TileLoader = new JavaTileLoader
  ) extends ScallopConf(args) {

    banner("""Usage: geojson-renderer [OPTION]... [input-file]
        |geojson-renderer renders a GeoJSON file to SVG and PNG images.
        |
        |Options:
        |""".stripMargin)

    errorMessageHandler = { message =>
      printError(s"Error: $message\n")
      builder.printHelp
      throw new GeoJsonRendererError()
    }

    val dimensions: ScallopOption[MapSize] = opt[String](
      "dimensions",
      short = 'd',
      descr =
        "The dimensions of the target file in pixels as <WIDTH>x<HEIGHT> (e.g. 800x600).",
      default = Some("1200x800")
    ).map(s => MapSize(s))
    val output: ScallopOption[ImageOutput] = opt[String](
      "output",
      short = 'o',
      descr =
        "Image output file or - for standard output. Defaults to a file when the input is a file, or stdout when reading from stdin."
    ).map(str => if (str == "-") StdOutput else FileOutput(Paths.get(str)))
    val outputFormat: ScallopOption[OutputFormatter] = opt[String](
      "output-format",
      short = 'f',
      descr =
        s"Defines the output image format (${OutputFormatter.names.mkString(", ")})",
      default = Some(SvgFormatter.toString),
      validate = str => OutputFormatter.find(str).isDefined
    ).map(format => OutputFormatter.find(format).get(tileLoader))
    val tileUrlTemplate: ScallopOption[String] = opt[String](
      "tile-url-template",
      descr =
        "Template for tile URLs, placeholders are {tile} for tile coordinate, {a-c} and {1-4} for load balancing.",
      default = Some("http://{a-c}.tile.openstreetmap.org/{tile}.png")
    )
    val cacheDir: ScallopOption[Path] = opt[String](
      "cache-dir",
      'c',
      "Enables caching and specifies the directory used to cache tiles."
    ).map(dir => Paths.get(dir))
    val input: ScallopOption[GeoJsonInput] =
      trailArg[String](descr = "GeoJSON input file or - for standard input")
        .map(str => if (str == "-") StdInput else FileInput(Paths.get(str)))

    addValidation(input() match {
      case input: FileInput =>
        if (Files.exists(input.file)) Right(())
        else Left(s"File '${input.file}' does not exist.")
      case _ => Right(())
    })

    addValidation(cacheDir.toOption match {
      case Some(dir) if !Files.exists(dir) =>
        Left(s"Cache directory '$dir' does not exist.")
      case Some(dir) if !Files.isDirectory(dir) =>
        Left(s"Cache directory '$dir' is invalid.")
      case _ => Right(())
    })

    verify()

    val tileLoader: TileLoader =
      cacheDir.toOption
        .map(dir => new DirectoryCachingTileLoader(dir, defaultTileLoader))
        .getOrElse(defaultTileLoader)
  }

}

class GeoJsonRendererError(val message: Option[String])
    extends Exception(message.mkString) {
  def this(message: String) = this(Some(message))

  def this() = this(None)
}

sealed trait GeoJsonInput {
  val name: String

  def in: InputStream

  def matchingOutput: ImageOutput
}

case class FileInput(file: Path) extends GeoJsonInput {
  override val name: String = file.toString

  override def in: InputStream = Files.newInputStream(file)

  override def matchingOutput: ImageOutput =
    CompanionFileOutput(file)
}

case object StdInput extends GeoJsonInput {
  override val name: String = "STDIN"

  override def in: InputStream = System.in

  override def matchingOutput: ImageOutput = StdOutput
}

abstract class OutputFormatter(
    val name: String,
    val extension: String,
    val imagePolicy: TileImagePolicy
) {
  def format(svg: Svg): Array[Byte]

  override def toString: String = name
}

object OutputFormatter {
  val formatters: Seq[(String, TileLoader => OutputFormatter)] =
    Seq(
      "svg" -> (_ => SvgFormatter),
      "svg-embedded" -> (tileLoader => new EmbeddedSvgFormatter(tileLoader)),
      "png" -> (tileLoader => new PngFormatter(tileLoader)),
      "html" -> (_ => HtmlFormatter),
      "html-embedded" -> (tileLoader => new EmbeddedHtmlFormatter(tileLoader))
    )

  def names: Seq[String] = formatters.map(_._1)

  def find(name: String): Option[TileLoader => OutputFormatter] =
    formatters.collectFirst { case (`name`, formatter) => formatter }
}

object SvgFormatter extends OutputFormatter("svg", "svg", DirectUrl) {
  override def format(svg: Svg): Array[Byte] =
    svg.renderToUtf8()
}

class EmbeddedSvgFormatter(tileLoader: TileLoader)
    extends OutputFormatter("svg-embedded", "svg", new EmbeddedData(tileLoader)) {
  override def format(svg: Svg): Array[Byte] =
    svg.renderToUtf8()
}

class PngFormatter(tileLoader: TileLoader)
    extends OutputFormatter("png", "png", new EmbeddedData(tileLoader)) {
  override def format(svg: Svg): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    new PNGTranscoder().transcode(
      new TranscoderInput(new ByteArrayInputStream(svg.renderToUtf8())),
      new TranscoderOutput(out)
    )
    out.toByteArray
  }
}

object HtmlFormatter extends OutputFormatter("html", "html", DirectUrl) {
  // An <svg> tag is necessary to allow browser interactivity,
  // necessary to download the referenced images.
  override def format(svg: Svg): Array[Byte] =
    embedInHtml(XML.loadString(svg.render()))

  def embedInHtml(elem: Elem): Array[Byte] =
    XmlHelpers
      .prettyPrint(
        <html>
          <head>
            <title>geojson-renderer</title>
          </head>
          <body>
            {elem}
          </body>
        </html>
      )
      .getBytes(StandardCharsets.UTF_8)
}

class EmbeddedHtmlFormatter(tileLoader: TileLoader)
    extends OutputFormatter(
      "html-embedded",
      "html",
      new EmbeddedData(tileLoader)
    ) {
  override def format(svg: Svg): Array[Byte] =
    HtmlFormatter.embedInHtml(<img
      width={svg.width.toString}
      height={svg.height.toString}
      src={asDataUrl(svg)}/>)

  private def asDataUrl(svg: Svg): String =
    s"data:image/svg+xml;base64,${Base64.getEncoder.encodeToString(svg.renderToUtf8())}"
}

sealed trait ImageOutput {
  val name: String

  def write(svg: Svg, formatter: OutputFormatter): Unit
}

case class CompanionFileOutput(inputFile: Path) extends ImageOutput {
  override val name: String = inputFile.toString

  override def write(
      svg: Svg,
      formatter: OutputFormatter
  ): Unit = {
    val outputFile = replaceExtension(inputFile, s".${formatter.extension}")
    Files.write(outputFile, formatter.format(svg))
  }
}

case class FileOutput(outputFile: Path) extends ImageOutput {
  override val name: String = outputFile.toString

  override def write(
      svg: Svg,
      formatter: OutputFormatter
  ): Unit = {
    Files.write(outputFile, formatter.format(svg))
  }
}

case object StdOutput extends ImageOutput {
  override val name: String = "STDOUT"

  override def write(
      svg: Svg,
      formatter: OutputFormatter
  ): Unit =
    Console.out.write(formatter.format(svg))
}
