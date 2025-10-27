package sh.lem.ccholo.networking

import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
inline fun <reified T : CCHoloPacket> T.send() {
  CCHoloPacketHandler.channel.sendToServer(this)
}
