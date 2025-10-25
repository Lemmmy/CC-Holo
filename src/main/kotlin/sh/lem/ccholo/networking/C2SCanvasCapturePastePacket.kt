package sh.lem.ccholo.networking

import io.netty.handler.codec.DecoderException
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_PASTE
import sh.lem.ccholo.util.CCStringUtil
import java.nio.ByteBuffer
import java.util.function.Supplier

data class C2SCanvasCapturePastePacket(
  val canvasId: Int = 0,
  val text: ByteBuffer = ByteBuffer.allocate(0),
) {
  fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeVarInt(text.remaining())
    buf.writeBytes(text)
  }

  companion object {
    const val PASTE_LIMIT = 512

    fun decode(buf: FriendlyByteBuf): C2SCanvasCapturePastePacket {
      val canvasId = buf.readInt()

      val length = buf.readVarInt()
      if (length > PASTE_LIMIT) {
        throw DecoderException("ByteArray with size $length is bigger than allowed 512")
      }

      val text = ByteArray(length)
      buf.readBytes(text)

      return C2SCanvasCapturePastePacket(
        canvasId = canvasId,
        text     = ByteBuffer.wrap(text)
      )
    }

    fun handle(msg: C2SCanvasCapturePastePacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork

        val root = CanvasHandlerServer.getRootForPlayer(player)
        if (msg.text.remaining() > 0 && isValidClipboard(msg.text)) {
          root.queuePlayerEvent(EVENT_PASTE, player, msg.text)
        }
      }
      c.packetHandled = true
    }

    private fun isValidClipboard(buffer: ByteBuffer): Boolean {
      for (i in buffer.position() until buffer.limit()) {
        if (!CCStringUtil.isTypableChar(buffer.get(i).toInt() and 0xFF)) return false
      }
      return true
    }
  }
}
