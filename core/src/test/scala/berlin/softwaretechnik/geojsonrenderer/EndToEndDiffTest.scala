package berlin.softwaretechnik.geojsonrenderer

import java.nio.file.{Files, Path, Paths}

import berlin.softwaretechnik.geojsonrenderer.MissingJdkMethods.replaceExtension
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{Assertion, BeforeAndAfterAll}

import scala.collection.JavaConverters._

class EndToEndDiffTest extends AnyFunSuite with BeforeAndAfterAll {

  val examplesDirectory: Path = Paths.get("examples")

  val geoJsonFiles: Seq[Path] =
    Files.list(examplesDirectory)
      .filter(_.getFileName.toString.endsWith(".json"))
      .iterator().asScala.toSeq

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
    val svgFile = replaceExtension(geoJsonFile, ".svg")
    val pngFile = replaceExtension(geoJsonFile, ".png")

    lazy val run: Unit = {
      Files.deleteIfExists(svgFile)
      Files.deleteIfExists(pngFile)
      Main.main(Array(
        "--png",
        geoJsonFile.toString,
      ))
    }

    test(s"$svgFile matches accepted XML") {
      run
      assertUnchanged(svgFile)
    }

    test(s"$pngFile matches accepted binary") {
      run
      // TODO semantic image diff instead of just binary diff
      assertUnchanged(pngFile)
    }
  }

  //  test("It renders a trivial geometry") {
  //    val svg =
  //      withInMemoryDirectory { dir =>
  //        val inputPath = dir.resolve("input.json")
  //        Files.write(inputPath, """{"type":"LineString","coordinates":[[0,10],[0,0],[10,0  ]]}""".getBytes(UTF_8))
  //        Main.run(inputPath, Dimensions(1200, 800), false)
  //        val outputPath = dir.resolve("input.svg")
  //        Files.copy(outputPath, Paths.get("output.svg"), REPLACE_EXISTING)
  //        new String(Files.readAllBytes(outputPath), UTF_8)
  //      }
  //
  //    assert(svg === """<svg/>""")
  //  }
  //
  //  private def withInMemoryDirectory[A](use: Path => A): A = {
  //    val filesystem = Jimfs.newFileSystem()
  //    try use(Files.createDirectories(filesystem.getPath("some", "test", "path")))
  //    finally filesystem.close()
  //  }

  private def assertUnchanged(svgFile: Path): Assertion = {
    assert(Files.exists(svgFile), s"Missing ${svgFile.toUri} after running")

    val status = git.status().addPath(svgFile.toString).call()

    assert(status.getUntracked.isEmpty, s"There is not yet a reference version of ${svgFile.toUri} in the repository or index. Review the current version and add it to the index if it is acceptable.")

    assert(status.getModified.isEmpty, s"${svgFile.toUri} differs from the reference version in the ${if (status.getChanged.isEmpty) "repository" else "index"}. Review the current version and add it to the index if it is acceptable.")
  }
}
