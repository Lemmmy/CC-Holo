package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerClient
import sh.lem.ccholo.canvas.DEFAULT_RAYCAST_RANGE
import sh.lem.ccholo.networking.CCHoloPacketHandler.wrapPacketHandler
import java.util.function.Supplier

data class S2CCanvasRequestRaycastPacket(
  val requestId: Long = 0,
  val range: Double = DEFAULT_RAYCAST_RANGE,
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeLong(requestId)
    buf.writeDouble(range)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = S2CCanvasRequestRaycastPacket(
      requestId = buf.readLong(),
      range = buf.readDouble()
    )

    fun handle(msg: S2CCanvasRequestRaycastPacket, ctx: Supplier<NetworkEvent.Context>) = wrapPacketHandler {
      val c = ctx.get()
      c.enqueueWork {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable {
          CanvasHandlerClient.onCanvasRequestRaycastPacket(msg)
        } }
      }
      c.packetHandled = true
    }
  }
}
