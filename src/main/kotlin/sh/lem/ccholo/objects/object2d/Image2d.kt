package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.ColourableObject
import sh.lem.ccholo.objects.ObjectRegistry.IMAGE_2D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.util.DirtyingProperty
import sh.lem.ccholo.util.assertColour
import sh.lem.ccholo.util.readVec2
import sh.lem.ccholo.util.writeVec2

open class Image2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : ColourableObject(id, parent, IMAGE_2D, canvasRoot), Scalable, Positionable2d {
  override var position: Vec2 by DirtyingProperty(Vec2.ZERO)
  override var scale by DirtyingProperty(1.0f)

  var imageUrl by DirtyingProperty("")
  var imageWidth by DirtyingProperty(0)
  var imageHeight by DirtyingProperty(0)

  var fallbackColour by DirtyingProperty(0x00000000)

  override fun readInitial(buf: FriendlyByteBuf) {
    position = buf.readVec2()
    scale = buf.readFloat()

    imageUrl = buf.readUtf()
    imageWidth = buf.readInt()
    imageHeight = buf.readInt()

    fallbackColour = buf.readInt()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeVec2(position)
    buf.writeFloat(scale)

    buf.writeUtf(imageUrl)
    buf.writeInt(imageWidth)
    buf.writeInt(imageHeight)

    buf.writeInt(fallbackColour)
  }

  /**
   * function():string -- Get the URL for this image.
   */
  @LuaFunction("getUrl")
  fun getUrl() = imageUrl

  /**
   * function(url:string) -- Set the URL for this image.
   */
  @LuaFunction("setUrl")
  fun setUrl(url: String) {
    imageUrl = url
  }

  /**
   * function():number, number -- Get the size of this image.
   */
  @LuaFunction
  fun getSize(): MethodResult = MethodResult.of(imageWidth, imageHeight)

  /**
   * function(width:number, height:number) -- Set the size of this image.
   */
  @LuaFunction
  fun setSize(width: Int, height: Int) {
    imageWidth = width.coerceIn(1, MAX_IMAGE_WIDTH)
    imageHeight = height.coerceIn(1, MAX_IMAGE_HEIGHT)
  }

  /**
   * function():number -- Get the fallback colour for this image.
   */
  @LuaFunction("getFallbackColor", "getFallbackColour")
  fun getFallbackColour() = fallbackColour.toLong() and 0xFFFFFFFFL

  /**
   * function(colour|r:int, [g:int, b:int], [alpha:int]):number -- Set the fallback colour for this image.
   */
  @LuaFunction("setFallbackColor", "setFallbackColour")
  fun setFallbackColour(args: IArguments) {
    fallbackColour = args.assertColour(0, fallbackColour)
  }

  /**
   * function():number -- Get the fallback alpha for this image.
   */
  @LuaFunction
  fun getFallbackAlpha() = fallbackColour and 0xFF

  /**
   * function(alpha:number):number -- Set the fallback alpha for this image.
   */
  @LuaFunction
  fun setFallbackAlpha(alpha: Int) {
    fallbackColour = fallbackColour and 0xFF.inv() or (alpha and 0xFF)
  }

  companion object {
    const val MAX_IMAGE_WIDTH = 8192
    const val MAX_IMAGE_HEIGHT = 8192
  }
}
