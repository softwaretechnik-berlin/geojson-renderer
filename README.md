How to intellij
---------------

`mill  mill.scalalib.GenIdea/idea`

How to run
----------

`mill core.run --dimensions 200x100 --png polygon.geojson`

TODO
----

* [ ] Make tile source configurable for command line util in a
  way that is convenient and not violating terms of service of
  the tile providers.

* [X] Create `j` distribution.

* [ ] Fix SVG to PNG rendering to have proper high-res bitmaps.

* [ ] Add an option to embed the bitmap data into the svg rather than 
      to link, so that we get a self-contained svg file.

* [ ] Provide our own PNG rendering that uses batik only to render
  the geojson graphics and do the overlay at the bitmap level
    * [ ] Provide caching for bitmap data

* [ ] Add an option(s) to write svg embedded into html

* [ ] Add option to add scripting to svg/ html which adds
      inspection of properties etc.

* [ ] Add meaningful classes and IDs to SVG

* [ ] Support "geojson" files with multiple feature collections.

* [ ] Add different dimension strategies.

      Currently we determine a bounding box that contains the whole geojson
      and maintains the aspect ration of the given dimensions.

      Other strategies could be:

      * Use the given dimensions as the maximum and cut the box to the boundary
        of the geojson content.

      * Specify a width or a height in pixels.

