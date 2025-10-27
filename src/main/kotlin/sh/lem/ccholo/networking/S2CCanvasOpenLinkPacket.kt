package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerClient
import sh.lem.ccholo.networking.CCHoloPacketHandler.wrapPacketHandler
import java.util.function.Supplier

data class S2CCanvasOpenLinkPacket(
  val url: String
) {
  fun encode(buf: FriendlyByteBuf) {
    buf.writeUtf(url)
  }

  companion object {
    const val URL_LIMIT = 512

    fun decode(buf: FriendlyByteBuf) = S2CCanvasOpenLinkPacket(
      url = buf.readUtf(URL_LIMIT)
    )

    fun handle(msg: S2CCanvasOpenLinkPacket, ctx: Supplier<NetworkEvent.Context>) = wrapPacketHandler {
      val c = ctx.get()
      c.enqueueWork {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable {
          CanvasHandlerClient.onCanvasOpenLinkPacket(msg)
        } }
      }
      c.packetHandled = true
    }
  }
}
