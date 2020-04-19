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

    def boundingBox(coordinates: Seq[GeoCoord]): BoundingBox = {
      BoundingBox(
        coordinates.map(_.lon).min,
        coordinates.map(_.lat).min,
        coordinates.map(_.lon).max,
        coordinates.map(_.lat).max
      )
    }

    boundingBox(geoJson match {
      case geometry: Geometry => coordinates(geometry)
      case Feature(geometry, _) => coordinates(geometry)
      case FeatureCollection(features) => features.flatMap(f => coordinates(f.geometry))
    })
  }

}
