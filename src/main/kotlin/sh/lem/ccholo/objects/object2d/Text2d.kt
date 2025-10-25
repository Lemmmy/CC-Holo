package sh.lem.ccholo.objects.object2d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.phys.Vec2
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.*
import sh.lem.ccholo.objects.ObjectRegistry.TEXT_2D
import sh.lem.ccholo.objects.TextObject.Companion.DEFAULT_LINE_HEIGHT
import sh.lem.ccholo.objects.TextObject.Companion.EMPTY_LINES
import sh.lem.ccholo.objects.renderers.object2d.Text2dRenderer
import sh.lem.ccholo.util.DirtyingProperty
import sh.lem.ccholo.util.readVec2
import sh.lem.ccholo.util.writeVec2

class Text2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : ColourableObject(id, parent, TEXT_2D, canvasRoot), Positionable2d, Scalable, TextObject, HorizontallyAlignable {
  override var position: Vec2 by DirtyingProperty(Vec2.ZERO)
  override var scale by DirtyingProperty(1.0f)

  override var lineHeight by DirtyingProperty(DEFAULT_LINE_HEIGHT)
  override var dropShadow = false
  override var horizontalAlignment by DirtyingProperty(HorizontallyAlignable.Alignment.LEFT)
  override var maxWidth by DirtyingProperty(Int.MAX_VALUE)

  override var plaintext: String? by DirtyingProperty("")
  override var component: Component? by DirtyingProperty(null)

  // Derived by the client at runtime
  internal var plaintextLines: SplitTabulatedTextLines = EMPTY_LINES
  internal var componentLines: List<FormattedCharSequence>? = null
  internal var componentLineWidths = IntArray(0)

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)

    position = buf.readVec2()
    scale = buf.readFloat()
    dropShadow = buf.readBoolean()
    lineHeight = buf.readShort()
    maxWidth = buf.readInt()
    horizontalAlignment = buf.readEnum(HorizontallyAlignable.Alignment::class.java)
    plaintext = buf.readNullable(FriendlyByteBuf::readUtf)
    component = buf.readNullable(FriendlyByteBuf::readComponent)

    DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable { Text2dRenderer.initialiseTextObject(this) }}
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)

    buf.writeVec2(position)
    buf.writeFloat(scale)
    buf.writeBoolean(dropShadow)
    buf.writeShort(lineHeight.toInt())
    buf.writeInt(maxWidth)
    buf.writeEnum(horizontalAlignment)
    buf.writeNullable(plaintext, FriendlyByteBuf::writeUtf)
    buf.writeNullable(component, FriendlyByteBuf::writeComponent)
  }
}
