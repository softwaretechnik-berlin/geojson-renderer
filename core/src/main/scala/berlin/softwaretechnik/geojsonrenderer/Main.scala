package berlin.softwaretechnik.geojsonrenderer

import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

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

  def main(args: Array[String]): Unit = {

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
    val geoJson: GeoJson = GeoJson.load(inputFile)

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
