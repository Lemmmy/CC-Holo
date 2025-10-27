package sh.lem.ccholo.networking

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerClient
import sh.lem.ccholo.networking.CCHoloPacketHandler.wrapPacketHandler
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectRegistry
import java.util.function.Supplier

data class S2CCanvasInitPacket(
  val canvasId: Int = 0,
  val objects: Collection<BaseObject> = emptyList(),
  val capturing: Boolean = false,
  val capturingMouseMove: Boolean = false,
  val keyCaptures: IntSet = IntOpenHashSet()
) {
  fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeCollection(objects, ObjectRegistry::write)
    buf.writeBoolean(capturing)
    buf.writeBoolean(capturingMouseMove)
    buf.writeVarIntArray(keyCaptures.toIntArray())
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = S2CCanvasInitPacket(
      canvasId = buf.readInt(),
      objects = buf.readCollection({ mutableListOf<BaseObject>() }, ObjectRegistry::read)
        .apply { sortWith(BaseObject.SORTING_ORDER) }, // Sort by ID to guarantee parents load before their children
      capturing = buf.readBoolean(),
      capturingMouseMove = buf.readBoolean(),
      keyCaptures = IntOpenHashSet(buf.readVarIntArray())
    )

    fun handle(msg: S2CCanvasInitPacket, ctx: Supplier<NetworkEvent.Context>) = wrapPacketHandler {
      val c = ctx.get()
      c.enqueueWork {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable {
          CanvasHandlerClient.onCanvasInitPacket(msg)
        } }
      }
      c.packetHandled = true
    }
  }
}
