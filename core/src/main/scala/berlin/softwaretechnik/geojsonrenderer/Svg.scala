package berlin.softwaretechnik.geojsonrenderer

import java.net.{HttpURLConnection, URL}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.Base64

import berlin.softwaretechnik.geojsonrenderer.geojson._
import berlin.softwaretechnik.geojsonrenderer.map._

import scala.xml._

class Svg(
    viewport: Viewport,
    tilingScheme: TilingScheme,
    geoJson: GeoJson,
    imagePolicy: TileImagePolicy
) {

  val tiles: Seq[Tile] = tilingScheme.tileCover(viewport)
  val width: Int = viewport.box.size.width
  val height: Int = viewport.box.size.height

  def render(): String =
    XmlHelpers.prettyPrint(
      <svg width={width.toString}
           height={height.toString}
           version="1.1"
           xmlns="http://www.w3.org/2000/svg"
           xmlns:xlink="http://www.w3.org/1999/xlink">
        <g class="geojson-tiles">
          {tiles.map(renderTile)}
        </g>
        <g class="geojson-features">
          {renderGeoJson(geoJson)}
        </g>
      </svg>
    )

  def renderToUtf8(): Array[Byte] =
    render().getBytes(StandardCharsets.UTF_8)

  private def renderTile(tile: Tile): Elem =
    <image xlink:href={imagePolicy.href(tile)}
             x={(tile.leftXPosition - viewport.box.left).toString}
             y={(tile.topYPosition - viewport.box.top).toString}
             width={s"${tile.size}px"}
             height={s"${tile.size}px"}/>

  private def renderGeoJson(geoJson: GeoJson): NodeSeq =
    asFeatures(geoJson).flatMap(renderFeature)

  private def asFeatures(geoJson: GeoJson): Seq[Feature] =
    geoJson match {
      case geometry: Geometry          => Seq(Feature(geometry, Map.empty))
      case feature: Feature            => Seq(feature)
      case FeatureCollection(features) => features
    }

  private def renderFeature(feature: Feature): Seq[Node] = {
    val style: GeoJsonStyle = GeoJsonStyle(feature.properties)
    <g
    id={style.id.orNull}
    class={"geojson-feature" + style.clazz.map(" " + _).mkString}
    stroke={style.stroke.getOrElse("#555555")}
    stroke-opacity={style.strokeOpacity.map(_.toString).orNull}
    stroke-width={style.strokeWidth.getOrElse(3).toString}
    fill={style.fill.getOrElse("#555555")}
    fill-opacity={style.fillOpacity.getOrElse(0.6).toString}
    data-properties={upickle.default.writeJs(feature.properties).render()}
    >
      {style.title.map(t => <title>
      {t}
    </title>).orNull}{style.description.map(t => <desc>
      {t}
    </desc>).orNull}{renderGeometry(feature.geometry)}
    </g>
  }

  private def renderGeometry(geometry: Geometry): Seq[Elem] =
    geometry match {
      case Point(geoCoord)              => Seq(renderPoint(geoCoord))
      case MultiPoint(geoCoords)        => geoCoords.map(renderPoint)
      case LineString(geoCoords)        => Seq(renderLineString(geoCoords))
      case MultiLineString(lineStrings) => lineStrings.map(renderLineString)
      case Polygon(coordinates)         => Seq(renderPolygon(coordinates))
      case MultiPolygon(polygons)       => polygons.map(renderPolygon)
    }

  private def renderPoint(gc: GeoCoord): Elem = {
    val mc = viewport.projection(gc)
    <circle cx={mc.x.toString} cy={mc.y.toString} r="3"/>
  }

  private def renderLineString(geoCoords: Seq[GeoCoord]): Elem =
    <polyline points={asSvgPoints(geoCoords)} fill="None"/>

  private def renderPolygon(coordinates: Seq[Seq[GeoCoord]]): Elem =
    <polygon points={asSvgPoints(coordinates.head)}/>

  private def asSvgPoints(geoCoords: Seq[GeoCoord]): String =
    geoCoords
      .map(viewport.projection.apply)
      .map(pos => s"${pos.x},${pos.y}")
      .mkString(" ")

}

trait TileImagePolicy {
  def href(tile: Tile): String
}

object DirectUrl extends TileImagePolicy {
  override def href(tile: Tile): String = tile.url
}

class EmbeddedData(client: TileLoader) extends TileImagePolicy {
  override def href(tile: Tile): String =
    asDataUrl(client.get(tile))

  private def asDataUrl(bytes: Array[Byte]): String =
    s"data:image/png;base64,${Base64.getEncoder.encodeToString(bytes)}"
}

trait TileLoader {
  def get(tile: Tile): Array[Byte]
}

class DirectoryCachingTileLoader(cacheDir: Path, inner: TileLoader)
    extends TileLoader {
  override def get(tile: Tile): Array[Byte] = {
    val cacheFile =
      cacheDir.resolve(s"${tile.id.x}_${tile.id.y}_${tile.id.z}.png")
    if (Files.exists(cacheFile)) {
      Files.readAllBytes(cacheFile)
    } else {
      val bytes = inner.get(tile)
      Files.write(cacheFile, bytes)
      bytes
    }
  }
}

class JavaTileLoader extends TileLoader {
  //TODO use a proper http client
  override def get(tile: Tile): Array[Byte] = {
    val url = new URL(tile.url)
    val con = url.openConnection.asInstanceOf[HttpURLConnection]
    con.setRequestMethod("GET")
    con.setRequestProperty("User-Agent", "curl/7.66.0")
    val in = con.getInputStream
    val bytes =
      LazyList.continually(in.read).takeWhile(_ != -1).map(_.toByte).toArray
    in.close()
    con.disconnect()
    bytes
  }
}
