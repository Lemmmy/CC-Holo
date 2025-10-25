package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.network.chat.Component
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.util.assertUtf8StringLength

/**
 * An object which contains text.
 */
interface TextObject {
  var plaintext: String?
  var component: Component?
  var dropShadow: Boolean
  var lineHeight: Short
  var maxWidth: Int

  /**
   * function():string -- Get the text for this object, either as plaintext or as a JSON string.
   */
  @LuaFunction
  fun getText(): String =
    component?.let { Component.Serializer.toStableJson(it) }
      ?: plaintext
      ?: ""

  /**
   * function(string) -- Set the text for this object as plaintext. May contain newlines and tabs. Maximum 32767 chars.
   */
  @LuaFunction
  fun setText(args: IArguments) {
    plaintext = args.assertUtf8StringLength(0, 0, MAX_LENGTH)
    component = null
  }

  /**
   * function(string) -- Set the text for this object as a JSON string. Maximum 32767 chars.
   */
  @LuaFunction
  fun setTextJson(args: IArguments) {
    val json = args.assertUtf8StringLength(0, 0, MAX_LENGTH)
    try {
      component = Component.Serializer.fromJson(json)
      plaintext = null
    } catch (e: Exception) {
      CCHolo.log.error("Invalid JSON string: $json", e)
      throw LuaException("Invalid JSON string")
    }
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

  /**
   * function():number -- Get the max width for this object.
   */
  @LuaFunction
  fun getMaxWidth(): MethodResult = MethodResult.of(maxWidth)

  /**
   * function(number) -- Set the max width for this object.
   */
  @LuaFunction
  fun setMaxWidth(width: Int): MethodResult {
    maxWidth = if (width > 0) width else Int.MAX_VALUE
    return MethodResult.of()
  }

  companion object {
    /**
     * We use a two-dimensional string array to indicate where tabs are.
     * For example, `"Hello\tworld\nFoo\tBar"` would become
     * `{{"Hello", "world"}, {"Foo", "Bar"}}`.
     *
     * This is used in the rendering to simulate tabs.
     */
    internal val EMPTY_LINES = SplitTabulatedTextLines(emptyList())

    /**
     * Same as Minecraft's default font
     */
    const val DEFAULT_LINE_HEIGHT: Short = 9

    /**
     * A tab is 4 spaces and one space is 4 pixels wide -> 1 tab is 4*4 (16) pixels wide.
     * Used during rendering
     */
    const val TAB_WIDTH = 16

    const val MAX_LENGTH = 32767
  }
}
