package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_CLICK
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_DRAG
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_UP
import java.util.function.Supplier

data class C2SCanvasCaptureMousePacket(
  val canvasId: Int = 0,
  val event: Event = Event.CLICK,
  val button: Int = 0,
  val x: Double = 0.0,
  val y: Double = 0.0,
) {
  fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeEnum(event)
    buf.writeInt(button)
    buf.writeDouble(x)
    buf.writeDouble(y)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasCaptureMousePacket(
      canvasId = buf.readInt(),
      event    = buf.readEnum(Event::class.java),
      button   = buf.readInt(),
      x        = buf.readDouble(),
      y        = buf.readDouble(),
    )

    fun handle(msg: C2SCanvasCaptureMousePacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork

        val root = CanvasHandlerServer.getRootForPlayer(player)
        root.queuePlayerEvent(msg.event.ccEvent, player, msg.button, msg.x, msg.y)
      }
      c.packetHandled = true
    }
  }

  enum class Event(val ccEvent: String) {
    CLICK(EVENT_MOUSE_CLICK),
    UP(EVENT_MOUSE_UP),
    DRAG(EVENT_MOUSE_DRAG)
  }
}
