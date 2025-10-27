package sh.lem.ccholo.canvas

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.PacketDistributor
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.networking.*
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectGroup
import sh.lem.ccholo.objects.object2d.Frame2d
import sh.lem.ccholo.objects.object3d.Origin3d
import sh.lem.ccholo.peripheral.HologramEvents.EVENT_CAPTURE_STOP
import sh.lem.ccholo.peripheral.HologramPeripheral
import java.util.concurrent.atomic.AtomicInteger

class CanvasRootServer: CanvasRoot() {
  private val rootId: Int = CanvasHandlerServer.nextId()

  private val objects: Int2ObjectMap<BaseObject> = Int2ObjectOpenHashMap()
  private val childrenOf: Int2ObjectMap<IntSet> = Int2ObjectOpenHashMap()

  private val removed: IntSet = IntOpenHashSet()

  private val listeners = ObjectOpenHashSet<HologramPeripheral>()

  private val lastId = AtomicInteger(ID_3D)

  val canvas2d = object: Frame2d {
    override val id = ID_2D
    override val canvasRoot = this@CanvasRootServer
  }

  val canvas3d = object: Origin3d() {
    override val id = ID_3D
    override val canvasRoot = this@CanvasRootServer
  }

  init {
    childrenOf.put(ID_2D, IntOpenHashSet())
    childrenOf.put(ID_3D, IntOpenHashSet())
  }

  fun newObjectId() = lastId.incrementAndGet()

  @Synchronized
  fun makeInitPacket(): S2CCanvasInitPacket? = try {
    S2CCanvasInitPacket(
      canvasId = rootId,
      objects = objects.values,
      capturing = capturing,
      capturingMouseMove = capturingMouseMove,
      hidingMouse = hidingMouse,
      keyCaptures = keyCaptures
    )
  } catch (e: Exception) {
    CCHolo.log.error("Error while making add packet. Object list was abandoned", e)
    null
  }

  @Synchronized
  fun makeRemovePacket(): S2CCanvasRemovePacket = S2CCanvasRemovePacket(rootId)

  @Synchronized
  fun makeUpdatePacket(): S2CCanvasUpdatePacket? {
    try {
      var changed: MutableList<BaseObject>? = null
      for (obj in objects.values) {
        try {
          if (obj!!.pollDirty()) {
            if (changed == null) changed = mutableListOf()
            changed.add(obj)
          }
        } catch (e: Exception) {
          CCHolo.log.error("Error while polling object for changes. Object was skipped", e)
        }
      }

      if (changed == null && removed.isEmpty()) return null
      if (changed == null) changed = mutableListOf()

      val packet = S2CCanvasUpdatePacket(rootId, changed, removed.toIntArray())
      removed.clear()
      return packet
    } catch (e: Exception) {
      CCHolo.log.error("Error while making update packet. Changelist was abandoned", e)
      return null
    }
  }

  @Synchronized
  private fun makeCaptureStatePacket(): S2CCanvasCaptureStatePacket =
    S2CCanvasCaptureStatePacket(
      canvasId = rootId,
      capturing = capturing,
      capturingMouseMove = capturingMouseMove,
      hidingMouse = hidingMouse,
      keyCaptures = keyCaptures
    )

  private fun sendCaptureStatePacket(player: ServerPlayer) {
    CCHoloPacketHandler.channel.send(PacketDistributor.PLAYER.with { player }, makeCaptureStatePacket())
  }

  @Synchronized
  fun add(obj: BaseObject) {
    val parent = childrenOf[obj.parent] ?: throw IllegalArgumentException("No such parent")
    check(objects.put(obj.id, obj) == null) { "An object already exists with that key" }
    parent.add(obj.id)
    if (obj is ObjectGroup) childrenOf.put(obj.id, IntOpenHashSet())
  }

  @Synchronized
  fun remove(obj: BaseObject) {
    check(removeImpl(obj.id)) { "No such object with this key" }
  }

  @Synchronized
  fun clear(obj: ObjectGroup) {
    val children = childrenOf[obj.id] ?: throw IllegalStateException("Object has no children")
    clearImpl(children)
  }

  @Synchronized
  fun startCapture(
    peripheral: HologramPeripheral?,
    player: ServerPlayer,
    includeMouseMove: Boolean,
    hideMouse: Boolean
  ) {
    peripheral?.let { listeners.add(it) }

    if (capturing) queuePlayerEvent(EVENT_CAPTURE_STOP, player)
    capturing = true
    capturingMouseMove = includeMouseMove
    hidingMouse = hideMouse
    sendCaptureStatePacket(player)
  }

  @Synchronized
  fun stopCapture(peripheral: HologramPeripheral?, player: ServerPlayer, sendPacket: Boolean) {
    peripheral?.let { listeners.add(it) }

    if (!capturing) return
    capturing = false
    capturingMouseMove = false
    hidingMouse = false
    queuePlayerEvent(EVENT_CAPTURE_STOP, player)
    if (sendPacket) sendCaptureStatePacket(player)
  }

  @Synchronized
  fun startKeyCapture(peripheral: HologramPeripheral?, player: ServerPlayer, keyCode: Int) {
    peripheral?.let { listeners.add(it) }

    keyCaptures.add(keyCode)
    sendCaptureStatePacket(player)
  }

  @Synchronized
  fun stopKeyCapture(peripheral: HologramPeripheral?, player: ServerPlayer, keyCode: Int) {
    peripheral?.let { listeners.add(it) }

    keyCaptures.remove(keyCode)
    sendCaptureStatePacket(player)
  }

  @Synchronized
  fun clearKeyCaptures(peripheral: HologramPeripheral?, player: ServerPlayer) {
    peripheral?.let { listeners.add(it) }

    keyCaptures.clear()
    sendCaptureStatePacket(player)
  }

  @Synchronized
  fun setClipboard(peripheral: HologramPeripheral?, player: ServerPlayer, text: String) {
    peripheral?.let { listeners.add(it) }

    CCHoloPacketHandler.channel.send(PacketDistributor.PLAYER.with { player }, S2CCanvasSetClipboardPacket(
      text = text
    ))
  }

  @Synchronized
  fun openLink(peripheral: HologramPeripheral?, player: ServerPlayer, url: String) {
    peripheral?.let { listeners.add(it) }

    CCHoloPacketHandler.channel.send(PacketDistributor.PLAYER.with { player }, S2CCanvasOpenLinkPacket(
      url = url
    ))
  }

  fun queueEvent(event: String, vararg args: Any) {
    listeners.forEach {
      it.queueEvent(event, *args)
    }
  }

  fun queuePlayerEvent(event: String, player: ServerPlayer, vararg args: Any) {
    queueEvent(event, player.gameProfile.name, player.gameProfile.id.toString(), *args)
  }

  internal fun removeListener(peripheral: HologramPeripheral) {
    listeners.remove(peripheral)
  }

  private fun removeImpl(id: Int): Boolean {
    if (objects.remove(id) == null) return false

    val children = childrenOf.remove(id)
    children?.let { clearImpl(it) }

    removed.add(id)
    return true
  }

  private fun clearImpl(objects: IntSet) {
    val iterator = objects.iterator()
    while (iterator.hasNext()) {
      val childId = iterator.nextInt()
      removeImpl(childId)
      iterator.remove()
    }
  }
}
