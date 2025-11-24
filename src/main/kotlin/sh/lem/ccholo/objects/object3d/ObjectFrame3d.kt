package sh.lem.ccholo.objects.object3d

import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_WIDTH
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.canvas.CanvasRootServer
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectRegistry.FRAME_3D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.objects.object2d.ResizableFrame2d
import sh.lem.ccholo.util.*

class ObjectFrame3d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : BaseObject(id, parent, FRAME_3D, canvasRoot), ResizableFrame2d, Positionable3d, Rotatable3d, Scalable, DepthTestable {
  override var position: Vec3 by DirtyingProperty(Vec3.ZERO)
  override var rotation: Vec3? by DirtyingProperty(Vec3.ZERO)
  override var hasDepthTest by DirtyingProperty(true)
  override var scale by DirtyingProperty(DEFAULT_SCALE)
  
  private var _width by DirtyingProperty(BASE_FRAME_WIDTH)
  private var _height by DirtyingProperty(BASE_FRAME_HEIGHT)
  
  override val width: Int get() = _width
  override val height: Int get() = _height

  @LuaFunction
  override fun setSize(width: Int, height: Int) {
    _width = width.coerceIn(1, CanvasRoot.MAX_FRAME_WIDTH)
    _height = height.coerceIn(1, CanvasRoot.MAX_FRAME_HEIGHT)
  }

  override val canvasRootClient: CanvasRootClient
    get() = canvasRoot as? CanvasRootClient ?: error("Tried to access client canvas from server context")
  override val canvasRootServer: CanvasRootServer
    get() = canvasRoot as? CanvasRootServer ?: error("Tried to access server canvas from client context")

  override fun readInitial(buf: FriendlyByteBuf) {
    position = buf.readVec3()
    rotation = buf.readOptVec3()
    hasDepthTest = buf.readBoolean()
    scale = buf.readFloat()
    _width = buf.readVarInt()
    _height = buf.readVarInt()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeVec3(position)
    buf.writeOptVec3(rotation)
    buf.writeBoolean(hasDepthTest)
    buf.writeFloat(scale)
    buf.writeVarInt(_width)
    buf.writeVarInt(_height)
  }

  companion object {
    const val DEFAULT_SCALE = 1 / 64.0f
  }
}
