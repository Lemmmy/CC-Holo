package sh.lem.ccholo.networking

import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.network.PacketDistributor

@SideOnly(Side.SERVER)
inline fun <reified T : CCHoloPacket> T.send(player: ServerPlayer) {
  CCHoloPacketHandler.channel.send(PacketDistributor.PLAYER.with { player }, this)
}
