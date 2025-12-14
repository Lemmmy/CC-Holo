package sh.lem.ccholo.peripheral

import dan200.computercraft.api.lua.ILuaCallback
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.MethodResult
import sh.lem.ccholo.canvas.PendingRaycast
import sh.lem.ccholo.networking.HitResultTable

class RaycastTaskCallback(
  val ctx: ILuaContext,
  val raycast: PendingRaycast,
) : ILuaCallback {
  private val pull = MethodResult.pullEvent(HologramEvents.EVENT_RAYCAST, this)

  override fun resume(response: Array<out Any?>): MethodResult {
    if (response.size < 5) return pull
    val playerName = response[1] as? String ?: return pull
    val playerUuid = response[2] as? String ?: return pull
    val requestId = response[3] as? Double ?: return pull
    @Suppress("UNCHECKED_CAST")
    val hitResult = response[4] as? HitResultTable

    if (
      playerName != raycast.player?.gameProfile?.name ||
      playerUuid != raycast.player?.gameProfile?.id.toString() ||
      requestId.toLong() != raycast.requestId
    ) {
      return pull
    }

    return MethodResult.of(hitResult)
  }

  companion object {
    fun make(ctx: ILuaContext, raycast: PendingRaycast) = RaycastTaskCallback(ctx, raycast).pull
  }
}
