package berlin.softwaretechnik.geojsonrenderer.geojson

import berlin.softwaretechnik.geojsonrenderer.{BoundingBox, GeoCoord, geojson}

object GeoJsonSpatialOps {

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

    boundingBox(geoJson match {
      case geometry: Geometry => coordinates(geometry)
      case Feature(geometry, _) => coordinates(geometry)
      case FeatureCollection(features) => features.flatMap(f => coordinates(f.geometry))
    })
  }

  def boundingBox(coordinates: Seq[GeoCoord]): BoundingBox =
    BoundingBox(
      west = normalizeLongitude(coordinates.map(_.lon).min),
      south = coordinates.map(_.lat).min,
      east = normalizeLongitude(coordinates.map(_.lon).max),
      north = coordinates.map(_.lat).max
    )

  def normalizeLongitude(longitude: Double, minimumLongitude: Double = -180): Double =
    floorMod(longitude - minimumLongitude, 360) + minimumLongitude

  def normalizeLongitude(geoCoord: GeoCoord, minimumLongitude: Double): GeoCoord =
    geoCoord.copy(lon = normalizeLongitude(geoCoord.lon, minimumLongitude))

  def floorMod(x: Double, y: Double): Double = {
    val remainder = x % y
    if (remainder < 0) y + remainder else remainder
  }
}
