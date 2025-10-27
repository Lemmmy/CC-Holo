package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerServer
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_SCREEN_SIZE
import java.util.function.Supplier

data class C2SCanvasScreenSizePacket(
  val canvasId: Int = 0,
  val screenWidth: Int,
  val screenHeight: Int,
  val guiScaledWidth: Int,
  val guiScaledHeight: Int,
  val guiScale: Double
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeInt(screenWidth)
    buf.writeInt(screenHeight)
    buf.writeInt(guiScaledWidth)
    buf.writeInt(guiScaledHeight)
    buf.writeDouble(guiScale)
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = C2SCanvasScreenSizePacket(
      canvasId = buf.readInt(),
      screenWidth = buf.readInt(),
      screenHeight = buf.readInt(),
      guiScaledWidth = buf.readInt(),
      guiScaledHeight = buf.readInt(),
      guiScale = buf.readDouble()
    )

    fun handle(msg: C2SCanvasScreenSizePacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        val player = c.sender ?: return@enqueueWork

        val root = CanvasHandlerServer.getRootForPlayer(player)
        root.screenWidth = msg.screenWidth.coerceAtLeast(1)
        root.screenHeight = msg.screenHeight.coerceAtLeast(1)
        root.guiScaledWidth = msg.guiScaledWidth.coerceAtLeast(1)
        root.guiScaledHeight = msg.guiScaledHeight.coerceAtLeast(1)
        root.guiScale = msg.guiScale.coerceAtLeast(0.1)

        if (root.pollScreenSizeDirty()) {
          root.queuePlayerEvent(
            EVENT_SCREEN_SIZE,
            player,
            root.screenWidth,
            root.screenHeight,
            root.guiScaledWidth,
            root.guiScaledHeight,
            root.guiScale
          )
        }
      }
      c.packetHandled = true
    }
  }
}
