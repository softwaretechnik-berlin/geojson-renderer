package berlin.softwaretechnik.geojsonrenderer

import java.io.{File, FileOutputStream, StringReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import berlin.softwaretechnik.geojsonrenderer.geojson.{Feature, FeatureCollection, GeoJson, GeoJsonStyle, Geometry, MultiPoint, Point}
import berlin.softwaretechnik.geojsonrenderer.tiling.{PositionedTile, TilingScheme, ZoomLevel}
import org.rogach.scallop.ScallopConf

import scala.xml.{Attribute, Elem, NodeSeq, Null}

object Main {

  //val tiledMap = TilingScheme.here("VgTVFr1a0ft1qGcLCVJ6", "LJXqQ8ErW71UsRUK3R33Ow")
  val tiledMap = TilingScheme.rrze()

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
      tiledMap.zoomLevels.reverse
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

    <svg
      width={projectedBox.width.toString}
      height={projectedBox.height.toString}
      version="1.1"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink= "http://www.w3.org/1999/xlink"
    >
      {imagesForTiles(zoomLevel, tiles)}
      {renderGeoJson(zoomLevel, projectedBox, geoJson)}
    </svg>.toString()
  }

  private def renderGeoJson(zoomLevel: ZoomLevel,
                            projectedBox: Box2D,
                            geoJson: GeoJson): NodeSeq = {
    val features = geoJson match {
      case geometry: Geometry => Seq(Feature(geometry, Map.empty))
      case feature: Feature => Seq(feature)
      case FeatureCollection(features) => features
    }
    features.map(renderFeature(zoomLevel, projectedBox, _))
  }

  private def renderFeature(zoomLevel: ZoomLevel,
                            projectedBox: Box2D,
                            feature: Feature): Elem = {
    val element = feature.geometry match {
      case Point(gc) =>
        val point = zoomLevel.geoProjection.bitmapPosition(gc) - projectedBox.upperLeft
        <circle cx={point.x.toString} cy={point.y.toString} r="3"
                  style="fill:none"/>

      case MultiPoint(points) =>
        <g>
        {points.map { geoCoord =>
          val point = zoomLevel.geoProjection.bitmapPosition(geoCoord) - projectedBox.upperLeft
            <circle cx={point.x.toString} cy={point.y.toString} r="3"
            />
        }}
        </g>

      case geojson.LineString(coordinates) =>
        <g>
          <polyline
            points={LineString(coordinates).points.map(zoomLevel.geoProjection.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
            fill="None"/>
        </g>
      case geojson.MultiLineString(lines) =>
        <g>{
          lines.map { coordinates =>
              <polyline
              points={LineString(coordinates).points.map(zoomLevel.geoProjection.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
              fill="None"
             />
          }}
        </g>

      case geojson.Polygon(coordinates) =>
        <polygon
          points={LineString(coordinates(0)).points.map(zoomLevel.geoProjection.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
          />

      case geojson.MultiPolygon(lines) =>
        <g>{
          lines.map { coordinates =>
              <polygon
              points={LineString(coordinates(0)).points.map(zoomLevel.geoProjection.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
             />
          }}
        </g>
    }

    val style: GeoJsonStyle = GeoJsonStyle(feature.properties)
    element.copy(child = element.child
      :+ <title>{style.title}</title>
      :+ <desc>{style.title}</desc>
    ) %
      Attribute(null, "stroke", style.stroke, Null) %
      Attribute(null, "stroke-opacity", style.strokeOpacity.toString, Null) %
      Attribute(null, "stroke-width", style.strokeWidth.toString, Null) %
      Attribute(null, "fill", style.fill, Null) %
      Attribute(null, "fill-opacity", style.fillOpacity.toString, Null)
  }

  private def saveAsPng(svgContent: String, filename: String) = {
    import org.apache.batik.transcoder.TranscoderInput
    val input = new TranscoderInput(new StringReader(svgContent))
    import org.apache.batik.transcoder.TranscoderOutput
    val output = new TranscoderOutput(new FileOutputStream(filename))
    import org.apache.batik.transcoder.image.PNGTranscoder

    val converter = new PNGTranscoder

    converter.transcode(input, output)
  }

  private def imagesForTiles(zoomLevel: ZoomLevel,
                             tiles: Seq[PositionedTile]) = {
    tiles.map { tile =>
      <image xlink:href={tiledMap.url(tile.tileId)}
               x={tile.position.x.toString}
               y={tile.position.y.toString}
               width={s"${zoomLevel.tileSize}px"}
               height={s"${zoomLevel.tileSize}px"}/>
    }
  }

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
