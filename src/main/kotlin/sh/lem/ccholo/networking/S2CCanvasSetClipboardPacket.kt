package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerClient
import sh.lem.ccholo.networking.CCHoloPacketHandler.wrapPacketHandler
import java.util.function.Supplier

data class S2CCanvasSetClipboardPacket(
  val text: String
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeUtf(text)
  }

  companion object {
    const val PASTE_LIMIT = 512

    fun decode(buf: FriendlyByteBuf) = S2CCanvasSetClipboardPacket(
      text = buf.readUtf(PASTE_LIMIT)
    )

    fun handle(msg: S2CCanvasSetClipboardPacket, ctx: Supplier<NetworkEvent.Context>) = wrapPacketHandler {
      val c = ctx.get()
      c.enqueueWork {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable {
          CanvasHandlerClient.onCanvasSetClipboardPacket(msg)
        } }
      }
      c.packetHandled = true
    }
  }
}
