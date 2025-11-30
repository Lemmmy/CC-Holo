package sh.lem.ccholo.canvas

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet
import it.unimi.dsi.fastutil.ints.IntIterator
import it.unimi.dsi.fastutil.ints.IntSet
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.networking.C2SCanvasScreenSizePacket
import sh.lem.ccholo.networking.C2SCanvasStopCapturePacket
import sh.lem.ccholo.networking.send
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ObjectGroup
import sh.lem.ccholo.objects.renderers.ObjectRendererRegistry

object CanvasRootClient : CanvasRoot() {
  var capturePendingOpen = false

  private val mc by lazy { Minecraft.getInstance() }

  override fun makeChildSet() = IntAVLTreeSet()

  override fun reset() {
    super.reset()

    updateScreenSize()

    capturePendingOpen = false

    if (mc.screen is CanvasCapturingScreen) {
      mc.setScreen(null)
    }
  }

  internal fun clientStopCapturing() {
    if (mc.screen is CanvasCapturingScreen) {
      mc.setScreen(null)
    }

    C2SCanvasStopCapturePacket().send()
    capturing = false
  }

  internal fun updateCaptureState(
    capturing: Boolean,
    capturingMouseMove: Boolean,
    hidingMouse: Boolean,
    keyCaptures: IntSet
  ) {
    val screen = mc.screen

    if (capturing && !this.capturing) {
      this.capturePendingOpen = true // Only open the GUI if we're not already capturing
    }

    this.capturing = capturing
    this.capturingMouseMove = capturing && capturingMouseMove
    this.hidingMouse = capturing && hidingMouse

    this.keyCaptures.clear()
    this.keyCaptures.addAll(keyCaptures)

    if (screen is CanvasCapturingScreen) {
      if (capturing) {
        screen.reset() // In case the capture state has changed, 'bump' the capture GUI
      } else {
        mc.setScreen(null) // If we're no longer capturing, close the screen
      }
    }
  }

  internal fun updateScreenSize() {
    screenWidth = mc.window.screenWidth
    screenHeight = mc.window.screenHeight
    guiScaledWidth = mc.window.guiScaledWidth
    guiScaledHeight = mc.window.guiScaledHeight
    guiScale = mc.window.guiScale
  }

  internal fun sendScreenSizePacket() {
    try {
      C2SCanvasScreenSizePacket(
        canvasId = 0,
        screenWidth, screenHeight, guiScaledWidth, guiScaledHeight, guiScale
      ).send()
    } catch (e: Exception) {
      CCHolo.log.error("Error while sending screen size packet", e)
    }
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
  fun drawChildren(children: IntIterator, gg: GuiGraphics, buf: MultiBufferSource) {
    while (children.hasNext()) {
      val id = children.nextInt()
      val obj = getObject(id) ?: continue
      val renderer = ObjectRendererRegistry.getRenderer<BaseObject>(obj.type) ?: continue
      renderer.draw(obj, this, gg, buf)
    }
  }
}
