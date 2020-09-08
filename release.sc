#!/usr/bin/env amm
import ammonite.ops._

case class Version(major: Int, minor: Int, patch: Int) {
  def components: Seq[Int] = Seq(major, minor, patch)

  def newerThan(other: Version): Boolean = {
    components.zip(other.components).foreach{case(self, other) =>
      if (self > other) return true
      if (other > self) return false
    }
    false
  }

  override def toString: String = {
    s"$major.$minor.$patch"
  }
}

object Version {
  def apply(string: String): Version = {
    val components = string.split("\\.").map(_.toInt)
    Version(components(0), components(1), components(2))
  }
}


def validateCleanWorkingCopy = {
  val gitStatus = %%('git, Seq("status", "--untracked-files=no", "--porcelain"))(pwd)
  if (gitStatus.out.lines.size > 0) {
    System.err.println("Working copy is not clean:" + gitStatus.out.lines.mkString("\n"))

    System.exit(-1)
  }
}

def transform(file: Path, f: String => String): Unit = {
  val newContent = f(read! file)
  write.over(file, newContent)
}


// Launches an interactive editor on the content and returns
// the edited content if the editor is saved, None otherwise.
def interactiveEditor(content: String): Option[String] = {
  val tempFile: Path = os.temp(content)
  write.over(tempFile, content)
  val mtime = stat(tempFile).mtime
  %('vim, Seq(tempFile.toString()))(pwd)
  try {
    if (stat(tempFile).mtime.toMillis <= mtime.toMillis) {
      None
    } else {
      Some(read(tempFile))
    }
  } finally {
    rm! tempFile
  }
}

@main
def main(version: String): Unit = {
  val nextVersion = Version(version)

  validateCleanWorkingCopy

  try {
    %%('mill, Seq("core.test"))(pwd)
  } catch {
    case e: Exception => println(e); System.exit(1)
  }

  validateCleanWorkingCopy

  val previousVersion = Version(%%('mill, Seq("show", "core.publishVersion"))(pwd).out.lines.head.replaceAll("\"", ""))
  if (!nextVersion.newerThan(previousVersion)) {
    System.err.println("New version needs to be newer!")
    System.exit(-1)
  }

  val previousTag = %%('git, Seq("describe", "--abbrev=0"))(pwd).out.lines.headOption.getOrElse(%%('git, Seq("rev-list", "HEAD"))(pwd).out.lines.last)

  val log = %%('git, Seq("log", s"${previousTag}..HEAD"))(pwd).out.string

  val releaseNotes: String = interactiveEditor(s"# Release ${nextVersion}\n\n" + log).getOrElse({
    System.err.println("Release notes were not saved. Aborting.")
    System.exit(-1)
    ???
  })

  // Update Versions
  transform(pwd / "README.md", _.replaceAll(
    previousVersion.toString, // the dots in the version are not escaped, but it's fine for now.
    nextVersion.toString
  ))
  transform(pwd / "build.sc", _.replaceAll(
    s"""publishVersion = "$previousVersion""",  // the dots in the version are not escaped, but it's fine for now.
    s"""publishVersion = "$nextVersion"""
  ))

  %%('git, Seq("commit", "-a", "-m" , s"Releasing ${nextVersion}."))(pwd)

  %%('git, Seq("tag", "-f" ,s"v${nextVersion}", "--annotate", "--message", releaseNotes))(pwd)

  println("Performing the actual release:")

  // This is a long operation and it makes sense to watch it.
  %('bash, Seq("-c", "./deploy.sh"))(pwd)

  // This might ask for passphrase, thus run interactively
  %('git, Seq("push", "--follow-tags"))(pwd)
}
