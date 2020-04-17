package berlin.softwaretechnik.geojsonrenderer

package object geojson {
  private[geojson] implicit val geoCoordReadWriter: json.ReadWriter[GeoCoord] =
    json.readwriter[Seq[Double]].bimap[GeoCoord](gc => Seq(gc.lon, gc.lat), cs => GeoCoord(cs(1), cs(0)))
}
