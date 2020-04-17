package berlin.softwaretechnik.geojsonrenderer.geojson

import berlin.softwaretechnik.geojsonrenderer.GeoCoord
import org.scalatest.funsuite.AnyFunSuite

class GeoJsonSerializationTest extends AnyFunSuite {

  // https://tools.ietf.org/html/rfc7946#section-1.5
  val featureCollectionJsonFromRfc: String =
    """{
    "type": "FeatureCollection",
    "features": [{
        "type": "Feature",
        "geometry": {
            "type": "Point",
            "coordinates": [102.0, 0.5]
        },
        "properties": {
            "prop0": "value0"
        }
    }, {
        "type": "Feature",
        "geometry": {
            "type": "LineString",
            "coordinates": [
                [102.0, 0.0],
                [103.0, 1.0],
                [104.0, 0.0],
                [105.0, 1.0]
            ]
        },
        "properties": {
            "prop0": "value0",
            "prop1": 0.0
        }
    }, {
        "type": "Feature",
        "geometry": {
            "type": "Polygon",
            "coordinates": [
                [
                    [100.0, 0.0],
                    [101.0, 0.0],
                    [101.0, 1.0],
                    [100.0, 1.0],
                    [100.0, 0.0]
                ]
            ]
        },
        "properties": {
            "prop0": "value0",
            "prop1": {
                "this": "that"
            }
        }
    }]
}"""

  val featureCollectionFromRfc: FeatureCollection =
    FeatureCollection(Seq(
      Feature(Point(GeoCoord(lon = 102.0, lat = 0.5)), Map("prop0" -> "value0")),
      Feature(LineString(Seq(
        GeoCoord(lon = 102.0, lat = 0.0),
        GeoCoord(lon = 103.0, lat = 1.0),
        GeoCoord(lon = 104.0, lat = 0.0),
        GeoCoord(lon = 105.0, lat = 1.0),
      )), Map("prop0" -> "value0", "prop1" -> 0.0)),
      Feature(Polygon(Seq(Seq(
        GeoCoord(lon = 100.0, lat = 0.0),
        GeoCoord(lon = 101.0, lat = 0.0),
        GeoCoord(lon = 101.0, lat = 1.0),
        GeoCoord(lon = 100.0, lat = 1.0),
        GeoCoord(lon = 100.0, lat = 0.0),
      ))), Map("prop0" -> "value0", "prop1" -> Map("this" -> "that"))),
    ))

  test("Should parse a FeatureCollection") {
    val value = GeoJson.read(ujson.Readable.fromString(featureCollectionJsonFromRfc))

    assert(value == featureCollectionFromRfc)
  }

  test("Should write a FeatureCollection") {
    val json = GeoJson.write(featureCollectionFromRfc)

    assert(json ==
      featureCollectionJsonFromRfc
        .replaceAll("[ \n]+", "")
        .replaceAll("\\.0\\b", "")
    )
  }

}
