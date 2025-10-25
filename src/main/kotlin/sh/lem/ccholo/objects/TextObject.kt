package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import sh.lem.ccholo.util.assertUtf8StringLength

/**
 * An object which contains text.
 */
interface TextObject {
  var text: String
  var dropShadow: Boolean
  var lineHeight: Short

  /**
   * function():string -- Get the text for this object.
   */
  @LuaFunction
  fun getText(): MethodResult = MethodResult.of(text) // wrap in MethodResult to avoid signature conflict

  /**
   * function(string) -- Set the text for this object.
   */
  @LuaFunction
  fun setText(args: IArguments) {
    text = args.assertUtf8StringLength(0, 0, MAX_LENGTH)
  }

  /**
   * function():boolean -- Get whether this object has a drop shadow.
   */
  @LuaFunction
  fun hasShadow() = dropShadow

  /**
   * function(boolean) -- Set the shadow for this object.
   */
  @LuaFunction
  fun setShadow(shadow: Boolean) {
    dropShadow = shadow
  }

  /**
   * function():number -- Get the line height for this object.
   */
  @LuaFunction
  fun getLineHeight(): MethodResult = MethodResult.of(lineHeight)

  /**
   * function(number) -- Set the line height for this object.
   */
  @LuaFunction
  fun setLineHeight(height: Int): MethodResult {
    lineHeight = height.toShort()
    return MethodResult.of() // wrap in MethodResult to avoid signature conflict
  }

  companion object {
    /**
     * We use a two-dimensional string array to indicate where tabs are.
     * For example, `"Hello\tworld\nFoo\tBar"` would become
     * `{{"Hello", "world"}, {"Foo", "Bar"}}`.
     *
     * This is used in the rendering to simulate tabs.
     */
    internal val EMPTY_LINES: List<List<String>> = emptyList()

    /**
     * Same as Minecraft's default font
     */
    const val DEFAULT_LINE_HEIGHT: Short = 9

    /**
     * A tab is 4 spaces and one space is 4 pixels wide -> 1 tab is 4*4 (16) pixels wide.
     * Used during rendering
     */
    const val TAB_WIDTH = 16

    private val SPLIT_PATTERN = Regex("\r\n|\n|\r")
    private val TAB_PATTERN = Regex("\t")

    const val MAX_LENGTH = 32768

    internal fun splitText(text: String): List<List<String>> {
      val lines = SPLIT_PATTERN.split(text)

      val splitLines: MutableList<MutableList<String>> = MutableList(lines.size) { mutableListOf() }
      val format = StringBuilder()
      for ((i, line) in lines.withIndex()) {
        val tabs = line.split(TAB_PATTERN).toMutableList()
        splitLines[i] = tabs

        for ((j, tab) in tabs.withIndex()) {
          format.append(tab)
          appendFormat(format, format.toString().also { tabs[j] = it })
        }
      }

      return splitLines
    }

    private fun appendFormat(builder: StringBuilder, text: String) {
      builder.setLength(0)

      val l = text.length
      var i = -1

      while (text.indexOf('\u00a7', i + 1).also { i = it } != -1) {
        if (i < l - 1) {
          val c0 = text[i + 1]

          if (isFormatColor(c0)) {
            builder.setLength(0)
            builder.append('\u00a7').append(c0)
          } else if (isFormatSpecial(c0)) {
            builder.append('\u00a7').append(c0)
          }
        }
      }
    }

    private fun isFormatColor(colorChar: Char) =
      (colorChar in '0'..'9' || colorChar >= 'a') && colorChar <= 'f' || colorChar in 'A'..'F'

    /**
     * Checks if the char code is O-K...lLrRk-o... used to set special formatting.
     */
    private fun isFormatSpecial(formatChar: Char) =
      (formatChar in 'k'..'o' || formatChar >= 'K') && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R'
  }
}
