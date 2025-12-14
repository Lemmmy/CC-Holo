package sh.lem.ccholo.canvas

import dan200.computercraft.api.peripheral.IComputerAccess
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.server.ServerAboutToStartEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.server.ServerLifecycleHooks
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.networking.HitResultTable
import sh.lem.ccholo.networking.send
import sh.lem.ccholo.peripheral.HologramPeripheral
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Mod.EventBusSubscriber
object CanvasHandlerServer {
  private val lastId = AtomicInteger(0)

  // All active canvas roots known to the server. Canvas roots are persisted per player game profile, so if they log out
  // and back in, they will get the same canvas root instance again. This allows the hologram peripheral to persist its
  // Lua references without worrying about losing information, but may use more memory if there are too many players.
  // TODO: Consider memory trade-offs here, maybe add a timeout to remove old roots?
  private val roots = ConcurrentHashMap<UUID, CanvasRootServer>()

  private val pendingRaycasts = ConcurrentHashMap<Long, PendingRaycast>()
  private val raycastId = AtomicLong(0)

  private val server
    get() = ServerLifecycleHooks.getCurrentServer()

  fun nextId() = lastId.getAndIncrement()

  fun requestRaycast(
    player: ServerPlayer?,
    range: Double,
    computer: IComputerAccess
  ): PendingRaycast {
    val id = raycastId.getAndIncrement()
    return PendingRaycast(id, range, player, server.overworld().gameTime, computer).also {
      pendingRaycasts[id] = it
      it.send() // Send the raycast request to the player if the player is currently available
    }
  }

  fun respondToRaycast(player: ServerPlayer, id: Long, hitTable: HitResultTable?) {
    pendingRaycasts.computeIfPresent(id) { _, raycast ->
      if (raycast.player != player) {
        CCHolo.log.warn("Received raycast request for a different player? Expected ${raycast.player}, got $player")
        return@computeIfPresent raycast
      }

      raycast.respond(hitTable)
      null
    }
  }

  /// Get the canvas root for a player, creating a new one if necessary. Assumes the player is currently online.
  @SideOnly(Side.SERVER)
  fun getRootForPlayer(p: ServerPlayer): CanvasRootServer
    = roots.getOrPut(p.gameProfile.id) {
      CCHolo.log.debug("Creating new canvas root for player {} ({})", p.gameProfile.name, p.gameProfile.id)

      // Create & return a new canvas root
      CanvasRootServer().also {
        it.reset()
        // Send the init packet to the player
        it.makeInitPacket()?.send(p)
      }
    }

  /// When the server starts, clear all canvas roots. This is mainly to clear roots on singleplayer when the player
  // starts/switches worlds
  @SubscribeEvent
  @SideOnly(Side.SERVER)
  fun onServerStarting(event: ServerAboutToStartEvent) {
    CCHolo.log.debug("Clearing canvas roots map")
    roots.clear()
    lastId.set(0)
  }

  /// When a player logs in, send them their existing canvas root if they have one
  @SubscribeEvent
  @SideOnly(Side.SERVER)
  fun onPlayerLogin(event: PlayerEvent.PlayerLoggedInEvent) {
    val p = event.entity as? ServerPlayer ?: return
    val root = roots[p.gameProfile.id] ?: return

    CCHolo.log.debug("Player {} ({}) logged in, sending canvas init packet", p.gameProfile.name, p.gameProfile.id)
    root.makeInitPacket()?.send(p)
  }

  /**
   * Update all holograms for all players. This builds the update packets for all holograms and sends them to players
   * if they are online. Must be run on the server thread.
   */
  @SubscribeEvent
  @SideOnly(Side.SERVER)
  fun update(event: TickEvent.ServerTickEvent) {
    val server = event.server

    server.playerList.players.forEach { p ->
      val root = roots[p.gameProfile.id] ?: return@forEach
      root.makeUpdatePacket()?.send(p) // Send the update packet. Returns null if there are no changes to send
    }

    pendingRaycasts.entries.removeIf { (_, raycast) ->
      raycast.tick(server)
    }
  }

  /// Clear all canvas roots and send re-initialisation packets to players if they are online to clear all objects. Must
  // be run on the server thread.
  @SideOnly(Side.SERVER)
  fun clearAllRoots() {
    CCHolo.log.debug("Resetting all canvas roots")

    // Send removal packets to all players with active roots
    roots.forEach { (uuid, root) ->
      root.reset()

      val p = server.playerList.getPlayer(uuid) ?: return@forEach
      CCHolo.log.debug("Sending canvas removal packet to player {} ({})", p.gameProfile.name, p.gameProfile.id)
      root.makeInitPacket()?.send(p)
    }
  }

  /// Clear the given listener from all canvas roots
  @SideOnly(Side.SERVER)
  fun removeListener(listener: HologramPeripheral) {
    roots.values.forEach { it.removeListener(listener) }
  }
}
