[![Maven Central](https://maven-badges.herokuapp.com/maven-central/berlin.softwaretechnik/geojson-renderer_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/berlin.softwaretechnik/geojson-renderer_2.13)

`geojson-renderer` is a command line tool that
renders geojson to SVG on top of a tiled map display. 

# Getting started

`geojson-renderer` is distributed with a [`jlauncher`](https://github.com/softwaretechnik-berlin/jlauncher) manifest, i.e. it can
be run without manual download once `jlauncher` is installed:

```bash
gem install jlauncher
```

Then `geojson-renderer` can be started like so:

```bash
j berlin.softwaretechnik:geojson-renderer_2.13:0.0.4 --dimensions 600x200 example.geojson
```

If the input file exists an svg file will be written
next to it.

To learn more about the options, there is a `--help`
command:

```bash
j berlin.softwaretechnik:geojson-renderer_2.13:0.0.4 --help
```

```
Usage: geojson-renderer [OPTION]... [input-file]
geojson-renderer renders a geojson file to svg and optionally to png.

Options:

  -d, --dimensions  <arg>          The dimensions of the target file in pixels.
  -p, --png                        Render the resulting svg into png.
  -t, --tile-url-template  <arg>   Template for tile URLs, placeholders are
                                   {tile} for tile coordinate, {a-c} and {1-4}
                                   for load balancing.
  -h, --help                       Show help message

 trailing arguments:
  input-file (required)   Geojson input file.
```

# How it Works
geojson renderer creates an SVG image with 
the geojson features and adds map tiles in the background,
so that the geojson is displayed on top of the map view.

Currently the tiles are rendered as SVG image tag that have a 
url pointing to the actual tile. That means that for SVG rendering
no tiles need to be downloaded. 

The image can also be rendered to PNG using apache batik. This will
lead to the downloading of tiles.

The tile source can be configured by providing a template, where the following
parameters are supported:

* `{tile}`:  the coordinate of the tile as `{z}/{x}/{Y}`.
* `{1-4}`: will be replaced with a number between 1 and 4,
* `{a-c}` will be replaced with one of the letters 'a', 'b' or 'c'. 

The use of tile servers is not generally free, so please make sure you adhere 
to the relevant policies.

There are a number of tile services based on open street map, see [list](https://wiki.openstreetmap.org/wiki/Tile_servers). 
Also, there are services based on proprietary maps, such as the [HERE Map Tile API](https://developer.here.com/documentation/map-tile/dev_guide/topics/introduction.html).

# Development

This project is work in progress. Pull requests
are welcome. There is a backlog with more tasks at
the end of this document.

`geojson-renderer` is implemented in Scala and uses
the mill build tool.

## Setup

Mill can create an intellij project:

`mill mill.scalalib.GenIdea/idea`

## Testing

There is a comprehensive test suite that might
fail from time to time as map tiles get updated.

We will address this at some point, by either
not using tiles in the test by providing
stable test tiles.

The tests can be run using

`mill core.test`

## Backlog

- [ ] Add an option to embed the bitmap data into the svg rather than
      to link, so that we get a self-contained svg file.

- [ ] Fix SVG to PNG rendering to have proper high-res bitmaps.

- [ ] Provide our own PNG rendering that uses batik only to render
      the geojson graphics and do the overlay at the bitmap level

  - [ ] Provide caching for bitmap data

- [ ] Add an option(s) to write svg embedded into html

- [ ] Add option to add scripting to svg/ html which adds
      inspection of properties etc.

- [ ] Add meaningful classes and IDs to SVG

- [ ] Support "geojson" files with multiple feature collections.

- [ ] Add different dimension strategies.

    Currently we determine a bounding box that contains the whole geojson
    and maintains the aspect ration of the given dimensions.
    
    Other strategies could be:
    
    * Use the given dimensions as the maximum and cut the box to the boundary
    of the geojson content.
    
    * Specify a width or a height in pixels.


### Done

- [X] Make tile source configurable for command line util in a
      way that is convenient and not violating terms of service of
      the tile providers.


