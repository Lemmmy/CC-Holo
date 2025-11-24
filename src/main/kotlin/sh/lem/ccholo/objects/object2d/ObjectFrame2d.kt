package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_WIDTH
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.canvas.CanvasRootServer
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectRegistry.FRAME_2D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.objects.object3d.Rotatable3d
import sh.lem.ccholo.util.*

class ObjectFrame2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : BaseObject(id, parent, FRAME_2D, canvasRoot), ResizableFrame2d, Positionable2d, Rotatable3d, Scalable {
  override var position: Vec2 by DirtyingProperty(Vec2.ZERO)
  override var rotation: Vec3? by DirtyingProperty(Vec3.ZERO)
  override var scale by DirtyingProperty(1.0f)
  
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
    position = buf.readVec2()
    rotation = buf.readOptVec3()
    scale = buf.readFloat()
    _width = buf.readVarInt()
    _height = buf.readVarInt()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeVec2(position)
    buf.writeOptVec3(rotation)
    buf.writeFloat(scale)
    buf.writeVarInt(_width)
    buf.writeVarInt(_height)
  }
}
