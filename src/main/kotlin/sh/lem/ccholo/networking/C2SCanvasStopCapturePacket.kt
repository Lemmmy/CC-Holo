package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import java.util.function.Supplier

data class C2SCanvasStopCapturePacket(val canvasId: Int = 0): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasStopCapturePacket(
      canvasId = buf.readInt(),
    )

    fun handle(msg: C2SCanvasStopCapturePacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork

        val root = CanvasHandlerServer.getRootForPlayer(player)
        root.stopCapture(player, false)
      }
      c.packetHandled = true
    }
  }
}
