package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_MOUSE_SCROLL
import sh.lem.ccholo.util.EntityHitInfo
import sh.lem.ccholo.util.readOptBlockHitResult
import sh.lem.ccholo.util.readOptEntityHitInfo
import sh.lem.ccholo.util.writeOptBlockHitResult
import sh.lem.ccholo.util.writeOptEntityHitInfo
import java.util.function.Supplier

data class C2SCanvasCaptureScrollPacket(
  val canvasId: Int = 0,
  val direction: Int = 0,
  val x: Double = 0.0,
  val y: Double = 0.0,
  val blockHit: BlockHitResult? = null,
  val entityHit: EntityHitInfo? = null,
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeInt(direction)
    buf.writeDouble(x)
    buf.writeDouble(y)
    buf.writeOptBlockHitResult(blockHit)
    buf.writeOptEntityHitInfo(entityHit)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasCaptureScrollPacket(
      canvasId  = buf.readInt(),
      direction = buf.readInt(),
      x         = buf.readDouble(),
      y         = buf.readDouble(),
      blockHit  = buf.readOptBlockHitResult(),
      entityHit = buf.readOptEntityHitInfo(),
    )

    fun handle(msg: C2SCanvasCaptureScrollPacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork

        val root = CanvasHandlerServer.getRootForPlayer(player)
        val hitTable = HitResultUtil.createHitResultTable(msg.blockHit, msg.entityHit, player.level())
        root.queuePlayerEvent(EVENT_MOUSE_SCROLL, player, msg.direction, msg.x, msg.y, hitTable)
      }
      c.packetHandled = true
    }
  }
}
