package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_KEY
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_KEY_UP
import java.util.function.Supplier

data class C2SCanvasCaptureKeyPacket(
  val canvasId: Int = 0,
  val key: Int = 0,
  val repeat: Boolean = false,
  val down: Boolean = true,
  val captureMode: Boolean = false,
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeInt(key)
    buf.writeBoolean(repeat)
    buf.writeBoolean(down)
    buf.writeBoolean(captureMode)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasCaptureKeyPacket(
      canvasId    = buf.readInt(),
      key         = buf.readInt(),
      repeat      = buf.readBoolean(),
      down        = buf.readBoolean(),
      captureMode = buf.readBoolean(),
    )

    fun handle(msg: C2SCanvasCaptureKeyPacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork

        val root = CanvasHandlerServer.getRootForPlayer(player)
        if (msg.down) {
          root.queuePlayerEvent(EVENT_KEY, player, msg.key, msg.repeat, msg.captureMode)
        } else {
          root.queuePlayerEvent(EVENT_KEY_UP, player, msg.key, msg.captureMode)
        }
      }
      c.packetHandled = true
    }
  }
}
