package sh.lem.ccholo.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import sh.lem.ccholo.canvas.CanvasHandlerClient
import sh.lem.ccholo.networking.CCHoloPacketHandler.wrapPacketHandler
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectRegistry
import java.util.function.Supplier

data class S2CCanvasUpdatePacket(
  val canvasId: Int = 0,
  var changed: MutableList<BaseObject> = mutableListOf(),
  var removed: IntArray = IntArray(0),
): CCHoloPacket {
  override fun encode(buf: FriendlyByteBuf) {
    buf.writeInt(canvasId)
    buf.writeCollection(changed, ObjectRegistry::write)
    buf.writeVarIntArray(removed)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as S2CCanvasUpdatePacket

    if (canvasId != other.canvasId) return false
    if (changed != other.changed) return false
    if (!removed.contentEquals(other.removed)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = canvasId
    result = 31 * result + changed.hashCode()
    result = 31 * result + removed.contentHashCode()
    return result
  }

  companion object {
    fun decode(buf: FriendlyByteBuf) = S2CCanvasUpdatePacket(
      canvasId = buf.readInt(),
      changed = buf.readCollection({ mutableListOf<BaseObject>() }, ObjectRegistry::read)
        .apply { sortWith(BaseObject.SORTING_ORDER) }, // Sort by ID to guarantee parents load before their children
      removed = buf.readVarIntArray(),
    )

    fun handle(msg: S2CCanvasUpdatePacket, ctx: Supplier<NetworkEvent.Context>) = wrapPacketHandler {
      val c = ctx.get()
      c.enqueueWork {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable {
          CanvasHandlerClient.onCanvasUpdatePacket(msg)
        } }
      }
      c.packetHandled = true
    }
  }
}
