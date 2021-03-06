<img src="geojson-renderer-logo.svg" alt="geojsono-renderer logo" width="232" height="212"/>

# `geojson-renderer`

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/berlin.softwaretechnik/geojson-renderer_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/berlin.softwaretechnik/geojson-renderer_2.13)

`geojson-renderer` is a command-line tool that renders
[GeoJSON](https://geojson.org/) documents to SVG images, with map tiles in the
background and the described geometries on top of the map.

For example, the following
[input](<https://flexivis.infrastruktur.link/?layout=(explanation30-map)/source&explanation=md:https://raw.githubusercontent.com/programmiersportgruppe/flexivis/master/docs/samples/berlin-walk.md&map=map:https://raw.githubusercontent.com/programmiersportgruppe/flexivis/master/docs/samples/berlin-walk.json&source=json:https://raw.githubusercontent.com/programmiersportgruppe/flexivis/master/docs/samples/berlin-walk.json>):

```json
{
  "type": "Feature",
  "geometry": {
    "type": "LineString",
    "coordinates": [
      [13.3907, 52.5074],
      [13.3902, 52.5076],
      [13.3891, 52.5076],
      [13.3871, 52.5077],
      [13.3855, 52.5073],
      [13.3841, 52.5095],
      [13.3838, 52.5109],
      [13.3827, 52.5136],
      [13.3813, 52.5156],
      [13.3796, 52.5165],
      [13.3785, 52.5163]              
    ]
  },
  "properties": {
    "stroke": "green"
  }
}
```

...would produce an [SVG image](examples/berlin-walk.svg) that looks like:

<img src="examples/berlin-walk.png" width="600" height="400">

## Getting started

`geojson-renderer` is distributed with a
[`jlauncher`](https://github.com/softwaretechnik-berlin/jlauncher) manifest,
i.e. it can be run without manual download once `jlauncher` is installed. To
install it, run:

```bash
gem install jlauncher
```

Then `geojson-renderer` can be installed like this:

```bash
jlauncher install berlin.softwaretechnik:geojson-renderer_2.13:0.2.1
```

Now `geojson-renderer` can be invoked like this to render an example
to a 600x200 pixel viewport:
  
```bash
 geojson-renderer --dimensions 600x200 example.geojson
```

If the input file is valid, an SVG file will be written next to it with the
relevant file extension.

To learn more about the options, a `--help` flag is available:

```bash
geojson-renderer --help
```

```
Usage: geojson-renderer [OPTION]... [input-file]
geojson-renderer renders a GeoJSON file to SVG and PNG images.

Options:

  -c, --cache-dir  <arg>           Enables caching and specifies the directory
                                   used to cache tiles.
  -d, --dimensions  <arg>          The dimensions of the target file in pixels
                                   as <WIDTH>x<HEIGHT> (e.g. 800x600).
  -e, --embed-images               Embed tile images instead of just referencing
                                   their URLs.
      --no-embed-images
  -o, --output  <arg>              Image output file or - for standard output.
                                   Defaults to a file when the input is a file,
                                   or stdout when reading from stdin.
  -f, --output-format  <arg>       Defines the output image format. Choices:
                                   svg, png, html
  -t, --tile-url-template  <arg>   Template for tile URLs, placeholders are
                                   {tile} for tile coordinate, {a-c} and {1-4}
                                   for load balancing.
  -h, --help                       Show help message

 trailing arguments:
  input (required)   GeoJSON input file or - for standard input
```

## How it Works

`geojson-renderer` creates an SVG image with the GeoJSON features and adds map
tiles in the background, so that the features are displayed on top of the map
view.

The visualisation of the features takes the `properties` object into account and
supports the following properties inspired by the 
[simplestyle spec](https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0):

* `description` - rendered into an SVG `<desc>`-element for the feature.
* `fill` - fill colour (#RRGGBB).
* `fillOpacity` - fill opacity (0..1).
* `stroke` - stroke colour (#RRGGBB).
* `strokeOpacity` - stroke opacity (0..1).
* `strokeWidth` - width in pixels. 
* `title` - rendered into an SVG `<title>` element for the feature. 
* `id` - id for the generated SVG element.
* `class` - CSS class for the generated SVG element.

Depending on the output-format setting tiles get rendered as an SVG <image>-tag
that either contains HTTP URLs pointing to tile server, that is they are not
downloaded until the SVG is shown, or they are embedded as data URLs

The tile source can be configured by providing a template URL where
the following parameters are supported:

- `{tile}`: the coordinate of the tile as `{z}/{x}/{Y}`.
- `{1-4}`: will be replaced with a number between 1 and 4,
- `{a-c}` will be replaced with one of the letters 'a', 'b' or 'c'.

There are several tile services based on OpenStreetMap, see
[list](https://wiki.openstreetmap.org/wiki/Tile_servers). Also, there are
services based on proprietary maps, such as the
[HERE Map Tile API](https://developer.here.com/documentation/map-tile/dev_guide/topics/introduction.html),
which can be used like so (Note, you'll need to replace `{app_id}` and `{app_code}` with 
your HERE credentials):

~~~
--tile-url-template "https://{1-4}.base.maps.api.here.com/maptile/2.1/maptile/newest/normal.day/{tile}/256/png8?app_id={app_id}&app_code={app_code}&ppi=320"
~~~

The use of tile servers is not generally free, so please adhere to the relevant
policies.

## Development

This project is a work in progress. Pull requests are welcome. A backlog with
more tasks is maintained at the end of this document.

`geojson-renderer` is implemented in Scala and uses the mill build tool.

### Setup

[Mill](https://github.com/lihaoyi/mill) can create an IntelliJ project:

```bash
mill mill.scalalib.GenIdea/idea
```

### Testing

There is a comprehensive integration test suite that uses live data to validate
the tool. The data is cached in the `core/src/test/resources/cached-tiles/`
directory. Therefore, deleting the directory's content would effectively clean
the cache.

The tests can be run using:

```bash
mill core.test
```

### Code Formatting

Run

```bash
mill core.reformat
```

### Backlog

 
- [ ] Add properties view for HTML and potentially SVG target format, so that user
      can click features and see properties from the geojson file.
        - [ ] Add the feature. **Work in Progress**
        - [ ] Clean-up the solution.
        - [ ] Add an option whether to include viewer.
- [ ] Add feature list to HTML format, so that user can click in a textual list
      to highlight feature in map and see its properties.             
- [ ] Make caching work with different tile sources (potentially just use an http cache)
- [ ] Fix SVG to PNG rendering to have proper high-res bitmaps.
- [ ] Provide our own PNG rendering that uses batik only to render the GeoJSON
      graphics and do the overlay at the bitmap level
- [ ] Add option to add scripting to SVG/HTML which adds inspection of
      properties etc.
- [ ] Support "GeoJSON" files with multiple feature collections.
- [ ] Add different dimension strategies. Currently, we determine a bounding box
      that contains the whole GeoJSON and maintains the aspect ratio of the
      given dimensions. Other strategies could be:
  - Use the given dimensions as the maximum and cut the box to the boundary of
    the GeoJSON content.
  - Specify width or height in pixels.
- [x] Add meaningful classes and IDs to SVG
- [x] Provide caching for bitmap data
- [x] Add an option(s) to write SVG embedded into html
- [x] Add an option to embed the bitmap data into the SVG rather than to link,
      so that we get a self-contained SVG file.
- [x] Add a flag that allow specifying the output file path.
- [x] Allow reading input GeoJSON from stdin.
- [x] Make tile source configurable for command line util in a way that is
      convenient and not violating terms of service of the tile providers.
