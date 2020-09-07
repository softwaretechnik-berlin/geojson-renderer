package berlin.softwaretechnik.geojsonrenderer

import scala.util.matching.Regex
import scala.xml.{Elem, Node, NodeSeq, Text}

object XmlHelpers {
  private val Whitespace: Regex = "(\\s|\\n)*".r

  def prettyPrint(elem: Elem): String =
    NodeSeq.fromSeq(Seq(normalizeWhitespace("\n", elem), Text("\n"))).toString()

  private def normalizeWhitespace(beginLine: String, elem: Elem): Elem = {
    val withoutWhitespace = elem.child.filterNot(isWhitespace)
    if (withoutWhitespace.exists(_.isInstanceOf[Elem]))
      elem.copy(
        child = withoutWhitespace.flatMap {
          case elem: Elem =>
            Seq(
              Text(beginLine + "  "),
              normalizeWhitespace(beginLine + "  ", elem)
            )
        } :+ Text(beginLine)
      )
    else elem
  }

  private def isWhitespace: Node => Boolean = {
    case Text(Whitespace(_)) => true
    case _                   => false
  }

}
