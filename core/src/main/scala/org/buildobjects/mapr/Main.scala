package org.buildobjects.mapr

import java.io.{File, FileOutputStream, StringReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.buildobjects.mapr.geojson.{Feature, FeatureCollection, GeoJson, GeoJsonStyle, MultiPoint, Point}
import org.buildobjects.mapr.tiling.{TilesWithOffset, TilingScheme, ZoomLevel}
import org.rogach.scallop.{ScallopConf, ScallopOption}

import scala.xml.{Attribute, Elem, Node, Null}

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

    val featureCollection = GeoJson.load(new File(Conf.inputFile.getOrElse(???)))

    val svgContent = render(
      determineBoundingBox(featureCollection),
      //  BoundingBox(13.335, 52.5033, 13.4265, 52.5276),
      screenDimensions = Conf.dimensions.getOrElse(???),
      featureCollection
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
             features: FeatureCollection): String = {



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

    val offset
      : DoubleVector2D = (screenDimensions.toVector - bitMapBox.dimensions.toVector) * 0.5

    val projectedBox = Box2D(
      bitMapBox.upperLeft - offset,
      bitMapBox.upperLeft - offset + screenDimensions.toVector
    )

    val tiles: Seq[TilesWithOffset] = zoomLevel.tileCover(projectedBox)

    val projectedBoundingBox = zoomLevel.bitmapBox(boundingBox) - projectedBox.upperLeft

    println(s"${projectedBoundingBox}")

    <svg
      width={projectedBox.width.toString}
      height={projectedBox.height.toString}
      version="1.1"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink= "http://www.w3.org/1999/xlink"
    >
      <defs>
        <marker id="arrow"
                markerWidth="10" markerHeight="10"
                refX="9" refY="3"
                orient="auto"
                markerUnits="strokeWidth">
        <path d="M0,0 L0,6 L9,3 z" />
      </marker>
      </defs>
      {imagesForTiles(zoomLevel, tiles)}
      <!--{screenDimensions.toBox2D.rect}-->
      <!--{projectedBoundingBox.rect}-->
      {features.features.map(feature => renderFeature(zoomLevel, projectedBox, feature))}
    </svg>.toString()
  }

  //marker-end="url(#arrow)"

  private def renderFeature(zoomLevel: ZoomLevel,
                            projectedBox: Box2D,
                            feature: Feature) = {
    val element = feature.geometry match {
      case Point(coordinates) => {
        val point = zoomLevel.bitmapPosition(GeoCoord(coordinates)) - projectedBox.upperLeft
        <circle cx={point.x.toString} cy={point.y.toString} r="3"
                  style="fill:none"/>
      }

      case MultiPoint(points) => {
        <g>
        {points.map { coordinates =>
          val point = zoomLevel.bitmapPosition(GeoCoord(coordinates)) - projectedBox.upperLeft
            <circle cx={point.x.toString} cy={point.y.toString} r="3"
            />
        }}
        </g>
      }

      case geojson.LineString(coordinates) =>
        <g>
          <polyline
            points={LineString(coordinates).points.map(zoomLevel.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
            fill="None"/>
        </g>
      case geojson.MultiLineString(lines) =>
        <g>{
          lines.map { coordinates =>
              <polyline
              points={LineString(coordinates).points.map(zoomLevel.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
              fill="None"
             />
          }}
        </g>

      case geojson.Polygon(coordinates) =>
        <polygon
          points={LineString(coordinates(0)).points.map(zoomLevel.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
          />

      case geojson.MultiPolygon(lines) =>
        <g>{
          lines.map { coordinates =>
              <polygon
              points={LineString(coordinates(0)).points.map(zoomLevel.bitmapPosition(_) - projectedBox.upperLeft).map(pos => s"${pos.x},${pos.y}").mkString(" ")}
             />
          }}
        </g>
    }

    val style: GeoJsonStyle = GeoJsonStyle(feature.properties)
    addChild(
      addChild(element, <title>{style.title}</title>),
      <desc>{style.title}</desc>
    ) %
      Attribute(null, "stroke", style.stroke, Null) %
      Attribute(null, "stroke-opacity", style.strokeOpacity, Null) %
      Attribute(null, "stroke-width", style.strokeWidth, Null) %
      Attribute(null, "fill", style.fill, Null) %
      Attribute(null, "fill-opacity", style.fillOpacity, Null)
  }

  def addChild(n: Node, newChild: Node) = n match {
    case Elem(prefix, label, attribs, scope, child @ _*) =>
      Elem(prefix, label, attribs, scope, child ++ newChild : _*)
    case _ => throw new RuntimeException("Can only add children to elements!")
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
                             tiles: Seq[TilesWithOffset]) = {
    tiles.map { tile =>
      <image xlink:href={tiledMap.url(tile.tileId)}
               x={tile.offset.x.toString}
               y={tile.offset.y.toString}
               width={s"${zoomLevel.tileSize}px"}
               height={s"${zoomLevel.tileSize}px"}/>
    }
  }

  def determineBoundingBox(collection: FeatureCollection): BoundingBox = {
    def boundingBox(coordinates: Seq[Seq[Double]]): BoundingBox = {
      val geos = LineString(coordinates).points
      BoundingBox(
        geos.map(_.lon).min,
        geos.map(_.lat).min,
        geos.map(_.lon).max,
        geos.map(_.lat).max
      )
    }

    collection.features
      .map(_.geometry match {
        case Point(coordinates) =>
          val geo = GeoCoord(coordinates);
          BoundingBox(geo.lon, geo.lat, geo.lon, geo.lat)
        case MultiPoint(coordinates) =>
          boundingBox(coordinates)
        case geojson.LineString(coordinates) =>
          boundingBox(coordinates)
        case geojson.MultiLineString(lines) =>
          boundingBox(lines.flatten)
        case geojson.Polygon(coordinates) =>
          boundingBox(coordinates.flatten)
        case geojson.MultiPolygon(lines) =>
          boundingBox(lines.flatten.flatten)
      })
      .reduce(
        (b1, b2) =>
          BoundingBox(
            math.min(b1.west, b2.west),
            math.min(b1.south, b2.south),
            math.max(b1.east, b2.east),
            math.max(b1.north, b2.north)
        )
      )

  }
}
