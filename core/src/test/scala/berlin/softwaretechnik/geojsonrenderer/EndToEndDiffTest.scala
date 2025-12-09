package berlin.softwaretechnik.geojsonrenderer

import java.nio.file.{Files, Path, Paths}

import berlin.softwaretechnik.geojsonrenderer.MissingJdkMethods.replaceExtension
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{Assertion, BeforeAndAfterAll}

import scala.jdk.StreamConverters._

class EndToEndDiffTest extends AnyFunSuite with BeforeAndAfterAll {

  val geoJsonExampleFiles: Seq[Path] =
    Files.list(Paths.get("examples"))
      .filter(_.getFileName.toString.endsWith(".json"))
      .toScala(Seq)

  var git: Git = new Git(
      new FileRepositoryBuilder()
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
    )

  override protected def afterAll(): Unit =
    try git.close()
    finally git.getRepository.close()

  test("Finds GeoJSON examples") {
    assert(geoJsonExampleFiles.nonEmpty)
  }

  geoJsonExampleFiles.foreach { geoJsonFile =>
    def testFormat(format: String, fileExtension: String, embed: Boolean = false): Unit = {
      val outputFile = replaceExtension(geoJsonFile, fileExtension)
      test(s"$outputFile matches expected $format format") {
        Files.deleteIfExists(outputFile)

        val args = (if (embed) Seq("-e") else Seq()) ++ Seq("-c", "core/src/test/resources/cached-tiles", "-f", format, "-o", outputFile.toString, geoJsonFile.toString)
        Main.run(new Main.Conf(args))
        assertUnchanged(outputFile)
      }
    }

    testFormat("svg", ".svg")
    testFormat("svg", "-embedded.svg", embed = true)
    testFormat("png", ".png")
    testFormat("html", ".html")
    testFormat("html", "-embedded.html", embed = true)
  }

  private def assertUnchanged(file: Path): Assertion = {
    assert(Files.exists(file), s"Missing ${file.toUri} after running")

    val status = git.status().addPath(file.toString).call()

    assert(status.getUntracked.isEmpty, s"There is not yet a reference version of ${file.toUri} in the repository or index. Review the current version and add it to the index if it is acceptable.")

    assert(status.getModified.isEmpty, s"${file.toUri} differs from the reference version in the ${if (status.getChanged.isEmpty) "repository" else "index"}. Review the current version and add it to the index if it is acceptable.")
  }
}
