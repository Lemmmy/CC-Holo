package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerClient
import java.util.function.Supplier

data class S2CCanvasRemovePacket(val canvasId: Int = 0) {
  fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = S2CCanvasRemovePacket(
      canvasId = buf.readInt()
    )

    fun handle(msg: S2CCanvasRemovePacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable {
          CanvasHandlerClient.onCanvasRemovePacket(msg)
        } }
      }
      c.packetHandled = true
    }
  }
}
