package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_CHAR
import java.util.function.Supplier

data class C2SCanvasCaptureCharPacket(
  val canvasId: Int = 0,
  val char: Int = 0
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeInt(char)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasCaptureCharPacket(
      canvasId = buf.readInt(),
      char     = buf.readInt(),
    )

    fun handle(msg: C2SCanvasCaptureCharPacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork

        val root = CanvasHandlerServer.getRootForPlayer(player)
        root.queuePlayerEvent(EVENT_CHAR, player, byteArrayOf(msg.char.toByte()))
      }
      c.packetHandled = true
    }
  }
}
