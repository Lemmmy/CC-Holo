package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.util.*
import java.util.function.Supplier

data class C2SCanvasRaycastPacket(
  val requestId: Long = 0,
  val blockHit: BlockHitResult? = null,
  val entityHit: EntityHitInfo? = null,
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeLong(requestId)
    buf.writeOptBlockHitResult(blockHit)
    buf.writeOptEntityHitInfo(entityHit)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasRaycastPacket(
      requestId = buf.readLong(),
      blockHit  = buf.readOptBlockHitResult(),
      entityHit = buf.readOptEntityHitInfo(),
    )

    fun handle(msg: C2SCanvasRaycastPacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()

      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork
        val hitTable = HitResultUtil.createHitResultTable(msg.blockHit, msg.entityHit, player.level())
        CanvasHandlerServer.respondToRaycast(player, msg.requestId, hitTable)
      }

      c.packetHandled = true
    }
  }
}
