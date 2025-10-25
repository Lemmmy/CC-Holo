package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_SCROLL
import java.util.function.Supplier

data class C2SCanvasCaptureScrollPacket(
  val canvasId: Int = 0,
  val direction: Int = 0,
  val x: Double = 0.0,
  val y: Double = 0.0,
) {
  fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeInt(direction)
    buf.writeDouble(x)
    buf.writeDouble(y)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasCaptureScrollPacket(
      canvasId  = buf.readInt(),
      direction = buf.readInt(),
      x         = buf.readDouble(),
      y         = buf.readDouble(),
    )

    fun handle(msg: C2SCanvasCaptureScrollPacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork

        val root = CanvasHandlerServer.getRootForPlayer(player)
        root.queuePlayerEvent(EVENT_MOUSE_SCROLL, player, msg.direction, msg.x, msg.y)
      }
      c.packetHandled = true
    }
  }
}
