package berlin.softwaretechnik.geojsonrenderer.geojson

import berlin.softwaretechnik.geojsonrenderer.{
  GeoBoundingBox,
  GeoCoord,
  geojson
}

object GeoJsonSpatialOps {

  def boundingBox(geoJson: GeoJson): GeoBoundingBox = {

    def coordinates(geometry: Geometry): Seq[GeoCoord] =
      geometry match {
        case Point(geo)                      => Seq(geo)
        case MultiPoint(coordinates)         => coordinates
        case geojson.LineString(coordinates) => coordinates
        case geojson.MultiLineString(lines)  => lines.flatten
        case geojson.Polygon(rings)          => rings.flatten
        case geojson.MultiPolygon(polygons)  => polygons.flatten.flatten
      }

    GeoBoundingBox(geoJson match {
      case geometry: Geometry   => coordinates(geometry)
      case Feature(geometry, _) => coordinates(geometry)
      case FeatureCollection(features) =>
        features.flatMap(f => coordinates(f.geometry))
    })
  }
}
