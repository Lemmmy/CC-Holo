package sh.lem.ccholo.objects

data class SplitTabulatedTextLines(
  val lines: List<Line>,
) {
  data class Line(
    val parts: List<TextPart>,
    val width: Int,
  )

  data class TextPart(
    val text: String,
    val width: Int,
    val x: Int,
  )
}
