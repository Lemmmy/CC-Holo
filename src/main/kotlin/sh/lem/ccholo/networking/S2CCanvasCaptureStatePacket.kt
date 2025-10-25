package sh.lem.ccholo.networking

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerClient
import java.util.function.Supplier

data class S2CCanvasCaptureStatePacket(
  val canvasId: Int = 0,
  val capturing: Boolean = false,
  val capturingMouseMove: Boolean = false,
  val keyCaptures: IntSet = IntOpenHashSet()
) {
  fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeBoolean(capturing)
    buf.writeBoolean(capturingMouseMove)
    buf.writeVarIntArray(keyCaptures.toIntArray())
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = S2CCanvasCaptureStatePacket(
      canvasId = buf.readInt(),
      capturing = buf.readBoolean(),
      capturingMouseMove = buf.readBoolean(),
      keyCaptures = IntOpenHashSet(buf.readVarIntArray())
    )

    fun handle(msg: S2CCanvasCaptureStatePacket, ctx: Supplier<NetworkEvent.Context>) {
      val c = ctx.get()
      c.enqueueWork {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable {
          CanvasHandlerClient.onCanvasCaptureStatePacket(msg)
        } }
      }
      c.packetHandled = true
    }
  }
}
