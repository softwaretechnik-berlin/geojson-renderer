package berlin.softwaretechnik.geojsonrenderer

import java.nio.file.Path

object MissingJdkMethods {

  def replaceExtension(path: Path, extension: String): Path =
    path.resolveSibling(
      path.getFileName.toString.replaceAll("[.][^.]*$", extension)
    )

}
