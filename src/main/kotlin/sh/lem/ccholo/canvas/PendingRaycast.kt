package sh.lem.ccholo.canvas

import dan200.computercraft.api.peripheral.IComputerAccess
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import sh.lem.ccholo.networking.HitResultTable
import sh.lem.ccholo.networking.S2CCanvasRequestRaycastPacket
import sh.lem.ccholo.networking.send
import sh.lem.ccholo.peripheral.HologramEvents

const val DEFAULT_RAYCAST_RANGE = 20.0
const val RAYCAST_TIMEOUT_TICKS = 40

data class PendingRaycast(
  val requestId: Long,
  val range: Double,
  var player: ServerPlayer? = null,
  val requestedTick: Long,
  val computer: IComputerAccess,
) {
  var sent = false
  var responded = false

  fun tick(server: MinecraftServer) =
    player?.hasDisconnected() == true || server.overworld().gameTime - requestedTick > RAYCAST_TIMEOUT_TICKS

  fun send() {
    if (sent) return

    player?.let {
      S2CCanvasRequestRaycastPacket(requestId, range).send(it)
      sent = true
    }
  }

  fun respond(hitTable: HitResultTable?) {
    if (responded) return

    computer.queueEvent(
      HologramEvents.EVENT_RAYCAST,
      player?.gameProfile?.name,
      player?.gameProfile?.id.toString(),
      requestId,
      hitTable
    )

    responded = true
  }
}
