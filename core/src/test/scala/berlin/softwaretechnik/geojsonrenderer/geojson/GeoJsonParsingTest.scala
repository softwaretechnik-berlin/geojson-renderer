package berlin.softwaretechnik.geojsonrenderer.geojson

import org.scalatest.FunSuite

class GeoJsonParsingTest extends FunSuite {

  // https://tools.ietf.org/html/rfc7946#section-1.5
  val exampleFeatureCollectionFromRfc =
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

  test("Should parse a FeatureCollection") {
    val value = GeoJson.read(ujson.Readable.fromString(exampleFeatureCollectionFromRfc))

    val expectedFeatureCollection =
      FeatureCollection(Seq(
        Feature(Point(Seq(102.0, 0.5)), Map("prop0" -> "value0")),
        Feature(LineString(Seq(
          Seq(102.0, 0.0),
          Seq(103.0, 1.0),
          Seq(104.0, 0.0),
          Seq(105.0, 1.0),
        )), Map("prop0" -> "value0", "prop1" -> 0.0)),
        Feature(Polygon(Seq(Seq(
          Seq(100.0, 0.0),
          Seq(101.0, 0.0),
          Seq(101.0, 1.0),
          Seq(100.0, 1.0),
          Seq(100.0, 0.0),
        ))), Map("prop0" -> "value0", "prop1" -> Map("this" -> "that"))),
      ))

    assert(value == expectedFeatureCollection)
  }

}
