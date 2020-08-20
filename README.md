`geojson-renderer` is a command line tool that
renders geojson to svg adds maptiles in the background,
so that the geojson is displayed on top of a map view.

# Getting started

`geojson-renderer` is distributed with a `jlauncher` manifest.
`jlauncher` needs to be installed:

```bash
gem install jlauncher
```

Then `geojson-renderer` can be started like so:

```bash
j berlin.softwaretechnik:geojson-renderer_2.13:0.0.3 --dimensions 600x200 example.geojson
```

If the input file exists an svg file will be written
next to it.

To learn more about the options, there is a `--help`
command:

```bash
j berlin.softwaretechnik:geojson-renderer_2.13:0.0.3 --help
```

```
Usage: geojson-renderer [OPTION]... [input-file]
geojson-renderer renders a geojson file to svg and optionally to png.

Options:

  -d, --dimensions  <arg>   The dimensions of the target file in pixels.
  -p, --png                 Render the resulting svg into png.
  -h, --help                Show help message

 trailing arguments:
  input-file (required)   Geojson input file.
```

# Development

This project is work in progress. Pull requests
are welcome.

`geojson-renderer` is implemented in Scala and uses
the mill build tool.

## Setup

Mill can create an intellij project:

`mill mill.scalalib.GenIdea/idea`

## Testing

There is a comprehensive test suite that might
fail from time to time as maptiles get updated.

We will address this at some point, by either
not using tiles in the test by providing
stable test tiles.

The tests can be run using

`mill core.test`

## Backlog

- [ ] Make tile source configurable for command line util in a
      way that is convenient and not violating terms of service of
      the tile providers.

- [ ] Fix SVG to PNG rendering to have proper high-res bitmaps.

- [ ] Add an option to embed the bitmap data into the svg rather than
      to link, so that we get a self-contained svg file.

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
