package berlin.softwaretechnik.geojsonrenderer

import java.nio.file.{Files, Path, Paths}

import berlin.softwaretechnik.geojsonrenderer.MissingJdkMethods.replaceExtension
import berlin.softwaretechnik.geojsonrenderer.map.Tile
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{Assertion, BeforeAndAfterAll}

import scala.jdk.StreamConverters._

class EndToEndDiffTest extends AnyFunSuite with BeforeAndAfterAll {

  val examplesDirectory: Path = Paths.get("examples")

  val geoJsonFiles: Seq[Path] =
    Files.list(examplesDirectory)
      .filter(_.getFileName.toString.endsWith(".json"))
      .toScala(Seq)

  var git: Git = _

  override protected def beforeAll(): Unit =
    git = new Git(
      new FileRepositoryBuilder()
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
    )

  override protected def afterAll(): Unit =
    try git.close()
    finally git.getRepository.close()

  test("Finds GeoJSON examples") {
    assert(geoJsonFiles.nonEmpty)
  }

  geoJsonFiles.foreach { geoJsonFile =>
    def testFormat(format: String, fileExtension: String): Unit = {
      val outputFile = replaceExtension(geoJsonFile, fileExtension)
      test(s"$outputFile matches expected $format format") {
        Files.deleteIfExists(outputFile)

        Main.run(new Main.Conf(Seq("-f", format, "-o", outputFile.toString, geoJsonFile.toString), TestCachingTileLoader))
        assertUnchanged(outputFile)
      }
    }

    testFormat("svg", ".svg")
    testFormat("svg-embedded", "-embedded.svg")
    testFormat("png", ".png")
    testFormat("html", ".html")
    testFormat("html-embedded", "-embedded.html")
  }

  private def assertUnchanged(file: Path): Assertion = {
    assert(Files.exists(file), s"Missing ${file.toUri} after running")

    val status = git.status().addPath(file.toString).call()

    assert(status.getUntracked.isEmpty, s"There is not yet a reference version of ${file.toUri} in the repository or index. Review the current version and add it to the index if it is acceptable.")

    assert(status.getModified.isEmpty, s"${file.toUri} differs from the reference version in the ${if (status.getChanged.isEmpty) "repository" else "index"}. Review the current version and add it to the index if it is acceptable.")
  }

  private object TestCachingTileLoader extends TileLoader {
    private val inner = new JavaTileLoader
    private val cacheDir = Paths.get("core/src/test/resources/cached-tiles")

    override def get(tile: Tile): Array[Byte] = {
      val cacheFile = cacheDir.resolve(s"${tile.id.x}_${tile.id.y}_${tile.id.z}.png")
      if (Files.exists(cacheFile)) {
        Files.readAllBytes(cacheFile)
      } else {
        val bytes = inner.get(tile)
        Files.write(cacheFile, bytes)
        bytes
      }
    }
  }
}
