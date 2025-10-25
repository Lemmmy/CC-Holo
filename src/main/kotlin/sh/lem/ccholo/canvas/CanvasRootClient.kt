package sh.lem.ccholo.canvas

import it.unimi.dsi.fastutil.ints.*
import it.unimi.dsi.fastutil.ints.IntIterator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.networking.C2SCanvasStopCapturePacket
import sh.lem.ccholo.networking.CCHoloPacketHandler
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectGroup
import sh.lem.ccholo.objects.renderers.ObjectRendererRegistry

object CanvasRootClient : CanvasRoot() {
  private val objects: Int2ObjectMap<BaseObject?> = Int2ObjectOpenHashMap()
  private val childrenOf: Int2ObjectMap<IntSortedSet> = Int2ObjectOpenHashMap()

  var capturePendingOpen = false

  init {
    initialise()
  }

  fun initialise() {
    objects.clear()
    childrenOf.clear()
    childrenOf.put(ID_2D, IntAVLTreeSet())
    childrenOf.put(ID_3D, IntAVLTreeSet())
    capturing = false
    capturePendingOpen = false
    keyCaptures.clear()

    if (Minecraft.getInstance().screen is CanvasCapturingScreen) {
      Minecraft.getInstance().setScreen(null)
    }
  }

  fun clientStopCapturing() {
    Minecraft.getInstance().setScreen(null)
    CCHoloPacketHandler.channel.sendToServer(C2SCanvasStopCapturePacket())
    capturing = false
  }

  fun updateCaptureState(
    capturing: Boolean,
    keyCaptures: IntSet
  ) {
    this.capturing = capturing
    if (capturing) this.capturePendingOpen = true
    this.keyCaptures.clear()
    this.keyCaptures.addAll(keyCaptures)
  }

  fun updateObject(obj: BaseObject) {
    val parent: IntSet? = childrenOf[obj.parent]
    if (parent == null) {
      CCHolo.log.error("Trying to add ${obj.id} to non-existent parent ${obj.parent} ($obj)")
      return
    }

    if (objects.put(obj.id, obj) == null) {
      // If this is a new instance, then set up the children
      parent.add(obj.id)
      if (obj is ObjectGroup) childrenOf.put(obj.id, IntAVLTreeSet())
    }
  }

  fun remove(id: Int) {
    val obj = objects.remove(id)
    childrenOf.remove(id) // We handle the removing of children in the canvas version

    if (obj != null) {
      // Remove from the parent set if needed.
      val parent: IntSet? = childrenOf[obj.parent]
      parent?.remove(id)
    }
  }

  fun getObject(id: Int) = objects[id]

  fun getChildren(id: Int): IntSet? {
    val children = childrenOf[id]
    return if (children.isEmpty()) null else children
  }

  @SideOnly(Side.CLIENT)
  fun drawChildren(children: IntIterator, gg: GuiGraphics?, buf: MultiBufferSource?) {
    while (children.hasNext()) {
      val id = children.nextInt()
      val obj = getObject(id) ?: continue
      val renderer = ObjectRendererRegistry.getRenderer<BaseObject>(obj.type) ?: continue
      renderer.draw(obj, this, gg!!, buf)
    }
  }
}
