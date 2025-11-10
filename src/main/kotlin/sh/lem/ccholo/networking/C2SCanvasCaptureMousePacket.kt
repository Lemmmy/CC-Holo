package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.networking.C2SCanvasCaptureMousePacket.Event.CLICK
import sh.lem.ccholo.networking.C2SCanvasCaptureMousePacket.Event.MOVE
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_CLICK
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_DRAG
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_MOVE
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_UP
import sh.lem.ccholo.util.*
import java.util.function.Supplier

data class C2SCanvasCaptureMousePacket(
  val canvasId: Int = 0,
  val event: Event = CLICK,
  val button: Int = 0,
  val x: Double = 0.0,
  val y: Double = 0.0,
  val blockHit: BlockHitResult? = null,
  val entityHit: EntityHitInfo? = null,
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeEnum(event)
    buf.writeInt(button)
    buf.writeDouble(x)
    buf.writeDouble(y)
    buf.writeOptBlockHitResult(blockHit)
    buf.writeOptEntityHitInfo(entityHit)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasCaptureMousePacket(
      canvasId  = buf.readInt(),
      event     = buf.readEnum(Event::class.java),
      button    = buf.readInt(),
      x         = buf.readDouble(),
      y         = buf.readDouble(),
      blockHit  = buf.readOptBlockHitResult(),
      entityHit = buf.readOptEntityHitInfo(),
    )

    fun handle(msg: C2SCanvasCaptureMousePacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()

      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork
        val root = CanvasHandlerServer.getRootForPlayer(player)

        val hitTable = HitResultUtil.createHitResultTable(msg.blockHit, msg.entityHit, player.level())

        // For mouse_move, always send the first argument (e.g. button, direction) as 1, to maintain backwards
        // compatibility with programs that perform blanket-handling for all mouse events.
        val button = if (msg.event == MOVE) 1 else msg.button
        root.queuePlayerEvent(msg.event.ccEvent, player, button, msg.x, msg.y, hitTable)
      }

      c.packetHandled = true
    }
  }

  enum class Event(val ccEvent: String) {
    CLICK(EVENT_MOUSE_CLICK),
    UP(EVENT_MOUSE_UP),
    DRAG(EVENT_MOUSE_DRAG),
    MOVE(EVENT_MOUSE_MOVE)
  }
}
