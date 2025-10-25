package sh.lem.ccholo.canvas

import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.server.ServerLifecycleHooks
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.networking.CCHoloPacketHandler
import sh.lem.ccholo.peripheral.HologramPeripheral
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Mod.EventBusSubscriber
object CanvasHandlerServer {
  private val lastId = AtomicInteger(0)

  // All active canvas roots known to the server. Canvas roots are persisted per player game profile, so if they log out
  // and back in, they will get the same canvas root instance again. This allows the hologram peripheral to persist its
  // Lua references without worrying about losing information, but may use more memory if there are too many players.
  // TODO: Consider memory trade-offs here, maybe add a timeout to remove old roots?
  private val roots = ConcurrentHashMap<UUID, CanvasRootServer>()

  fun nextId() = lastId.getAndIncrement()

  /// Get the canvas root for a player, creating a new one if necessary. Assumes the player is currently online.
  @SideOnly(Side.SERVER)
  fun getRootForPlayer(p: ServerPlayer): CanvasRootServer
    = roots.getOrPut(p.gameProfile.id) {
      CCHolo.log.debug("Creating new canvas root for player {} ({})", p.gameProfile.name, p.gameProfile.id)

      // Create a new canvas root
      CanvasRootServer().also {
        // Send the init packet to the player
        it.makeInitPacket()?.let {
          pkt -> CCHoloPacketHandler.channel.send(PacketDistributor.PLAYER.with { p }, pkt)
        }
      }
    }

  /// When a player logs in, send them their existing canvas root if they have one
  @SubscribeEvent
  @SideOnly(Side.SERVER)
  fun onPlayerLogin(event: PlayerEvent.PlayerLoggedInEvent) {
    val p = event.entity as? ServerPlayer ?: return
    val root = roots[p.gameProfile.id] ?: return

    CCHolo.log.debug("Player {} ({}) logged in, sending canvas init packet", p.gameProfile.name, p.gameProfile.id)
    root.makeInitPacket()?.let {
      pkt -> CCHoloPacketHandler.channel.send(PacketDistributor.PLAYER.with { p }, pkt)
    }
  }

  /**
   * Update all holograms for all players. This builds the update packets for all holograms and sends them to players
   * if they are online. Must be run on the server thread.
   */
  @SubscribeEvent
  @SideOnly(Side.SERVER)
  fun update(event: TickEvent.ServerTickEvent) {
    ServerLifecycleHooks.getCurrentServer().playerList.players.forEach { p ->
      val root = roots[p.gameProfile.id] ?: return@forEach

      // Send the update packet. Returns null if there are no changes to send
      root.makeUpdatePacket()?.let {
        pkt -> CCHoloPacketHandler.channel.send(PacketDistributor.PLAYER.with { p }, pkt)
      }
    }
  }

  /// Clear all canvas roots and send removal packets to players if they are online. Must be run on the server thread.
  @SideOnly(Side.SERVER)
  fun removeAllRoots() {
    CCHolo.log.debug("Removing all canvas roots")

    // Send removal packets to all players with active roots
    roots.forEach { (uuid, root) ->
      val p = ServerLifecycleHooks.getCurrentServer().playerList.getPlayer(uuid) ?: return@forEach
      CCHolo.log.debug("Sending canvas removal packet to player {} ({})", p.gameProfile.name, p.gameProfile.id)

      val pkt = root.makeRemovePacket()
      CCHoloPacketHandler.channel.send(PacketDistributor.PLAYER.with { p }, pkt)
    }

    // Clear the roots map
    roots.clear()
  }

  /// Clear the given listener from all canvas roots
  @SideOnly(Side.SERVER)
  fun removeListener(listener: HologramPeripheral) {
    roots.values.forEach { it.removeListener(listener) }
  }
}
