package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.network.FriendlyByteBuf
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.util.DirtyingProperty
import sh.lem.ccholo.util.assertColour

const val DEFAULT_COLOUR = 0xFFFFFFFFL

abstract class ColourableObject(
  id: Int,
  parent: Int,
  type: Byte,
  canvasRoot: CanvasRoot
) : BaseObject(id, parent, type, canvasRoot) {
  var colour by DirtyingProperty(DEFAULT_COLOUR.toInt())

  val r: Int
    get() = colour shr 24 and 0xFF
  val g: Int
    get() = colour shr 16 and 0xFF
  val b: Int
    get() = colour shr 8 and 0xFF
  val a: Int
    get() = colour and 0xFF

  /**
   * function():number -- Get the colour for this object.
   */
  @LuaFunction("getColor", "getColour")
  fun getColour() = colour.toLong() and 0xFFFFFFFFL

  /**
   * function(colour|r:int, [g:int, b:int], [alpha:int]):number -- Set the colour for this object.
   */
  @LuaFunction("setColor", "setColour")
  fun setColour(args: IArguments) {
    colour = args.assertColour(0, colour)
  }

  /**
   * function():number -- Get the alpha for this object.
   */
  @LuaFunction
  fun getAlpha() = colour and 0xFF

  /**
   * function(alpha:number):number -- Set the alpha for this object.
   */
  @LuaFunction
  fun setAlpha(alpha: Int) {
    colour = colour and 0xFF.inv() or (alpha and 0xFF)
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeInt(colour)
  }

  override fun readInitial(buf: FriendlyByteBuf) {
    colour = buf.readInt()
  }
}
