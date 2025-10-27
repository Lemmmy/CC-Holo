package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf

interface CCHoloPacket {
  fun encode(buf: FriendlyByteBuf)
}
