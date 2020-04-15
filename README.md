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

* [ ] Create `j` distribution.

* [ ] Fix SVG to PNG rendering to have proper high-res bitmaps.

* [ ] Provide our own PNG rendering that uses batik only to render
  the geojson graphics and do the overlay at the bitmap level
    * [ ] Provide caching for bitmap data

