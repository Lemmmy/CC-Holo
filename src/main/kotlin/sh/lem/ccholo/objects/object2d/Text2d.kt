package sh.lem.ccholo.objects.object2d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.ColourableObject
import sh.lem.ccholo.objects.ObjectRegistry.TEXT_2D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.objects.TextObject
import sh.lem.ccholo.objects.TextObject.Companion.DEFAULT_LINE_HEIGHT
import sh.lem.ccholo.objects.TextObject.Companion.EMPTY_LINES
import sh.lem.ccholo.objects.TextObject.Companion.splitText
import sh.lem.ccholo.util.DirtyingProperty
import sh.lem.ccholo.util.readVec2
import sh.lem.ccholo.util.writeVec2

class Text2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : ColourableObject(id, parent, TEXT_2D, canvasRoot), Positionable2d, Scalable, TextObject {
  override var position: Vec2 by DirtyingProperty(Vec2.ZERO)
  override var scale by DirtyingProperty(1.0f)

  override var lineHeight: Short by DirtyingProperty(DEFAULT_LINE_HEIGHT)
  override var dropShadow = false
  override var plaintext: String? by DirtyingProperty("") { _, new, _ -> lines = splitText(new) }
  override var component: Component? by DirtyingProperty(null)

  internal var lines = EMPTY_LINES

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)

    position = buf.readVec2()
    scale = buf.readFloat()
    dropShadow = buf.readBoolean()
    lineHeight = buf.readShort()
    plaintext = buf.readNullable(FriendlyByteBuf::readUtf)
    component = buf.readNullable(FriendlyByteBuf::readComponent)

    lines = splitText(plaintext)
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)

    buf.writeVec2(position)
    buf.writeFloat(scale)
    buf.writeBoolean(dropShadow)
    buf.writeShort(lineHeight.toInt())
    buf.writeNullable(plaintext, FriendlyByteBuf::writeUtf)
    buf.writeNullable(component, FriendlyByteBuf::writeComponent)
  }
}
