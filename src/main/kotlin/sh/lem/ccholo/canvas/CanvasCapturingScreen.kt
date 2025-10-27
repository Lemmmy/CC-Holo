package sh.lem.ccholo.canvas

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import sh.lem.ccholo.CCHolo.MOD_ID
import sh.lem.ccholo.CCHoloClient
import sh.lem.ccholo.canvas.CanvasRoot.Companion.HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.MAX_KEY_CODE
import sh.lem.ccholo.canvas.CanvasRoot.Companion.WIDTH
import sh.lem.ccholo.canvas.CanvasRootClient.capturing
import sh.lem.ccholo.canvas.CanvasRootClient.capturingMouseMove
import sh.lem.ccholo.networking.*
import sh.lem.ccholo.networking.C2SCanvasCaptureMousePacket.Event
import sh.lem.ccholo.util.CCStringUtil
import java.util.*

private const val DRAG_INTERVAL_MS = 50
private const val MOVE_INTERVAL_MS = 50

class CanvasCapturingScreen: Screen(Component.translatable(
  "gui.${MOD_ID}.canvas.capturing.title",
  CCHoloClient.KeyBindings.CAPTURE_CLOSE.get().translatedKeyMessage
)) {
  // Tracked to clear the inputs when the window is unfocused
  private var lastMouseButton = -1
  private var lastMouseX = -1.0
  private var lastMouseY = -1.0
  private val keysDown = BitSet(MAX_KEY_CODE)

  private var pendingDragButton = -1
  private var pendingDragX = -1.0
  private var pendingDragY = -1.0
  private var timeLastDragSent = -1L

  private var pendingMoveX = -1.0
  private var pendingMoveY = -1.0
  private var timeLastMoveSent = -1L

  override fun render(gg: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    super.render(gg, mouseX, mouseY, partialTick)

    gg.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF)

    val time = System.currentTimeMillis()

    // Mouse drag events
    if (
      pendingDragButton in 0..2
      && pendingDragX >= 0
      && pendingDragY >= 0
      && time != -1L
      && (timeLastDragSent == -1L || time - timeLastDragSent >= DRAG_INTERVAL_MS)
    ) {
      CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureMousePacket(
        event = Event.DRAG,
        button = lastMouseButton + 1, // lua indexed
        x = pendingDragX,
        y = pendingDragY,
      ))
      timeLastDragSent = time
      pendingDragButton = -1
      pendingDragX = -1.0
      pendingDragY = -1.0
    }

    // Mouse move events
    if (
      pendingMoveX >= 0
      && pendingMoveY >= 0
      && time != -1L
      && (timeLastMoveSent == -1L || time - timeLastMoveSent >= MOVE_INTERVAL_MS)
    ) {
      CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureMousePacket(
        event = Event.MOVE,
        x = pendingMoveX,
        y = pendingMoveY,
      ))
      timeLastMoveSent = time
      pendingMoveX = -1.0
      pendingMoveY = -1.0
    }
  }

  override fun charTyped(ch: Char, modifiers: Int): Boolean {
    if (!capturing) return false

    val terminalChar = CCStringUtil.unicodeToTerminal(ch.code)
    if (CCStringUtil.isTypableChar(terminalChar)) {
      CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureCharPacket(char = terminalChar))
    }

    return true
  }

  override fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
    val captured = CanvasRootClient.keyCaptures.contains(key)

    if (key == GLFW.GLFW_KEY_ESCAPE) {
      CanvasRootClient.clientStopCapturing()
      return true
    } else if (capturing && isPaste(key)) {
      paste()
      return true
    } else if (key >= 0 && (capturing || captured)) {
      val repeat = keysDown.get(key)
      keysDown.set(key)

      CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureKeyPacket(
        key = key,
        down = true,
        repeat = repeat,
        captureMode = capturing
      ))

      return true
    }

    return false
  }

  private fun paste() {
    val clipboard = CCStringUtil.getClipboardString(Minecraft.getInstance().keyboardHandler.clipboard)
    if (clipboard.remaining() > 0) {
      CCHoloPacketHandler.channel.sendToServer(C2SCanvasCapturePastePacket(
        text = clipboard
      ))
    }
  }

  override fun keyReleased(key: Int, scanCode: Int, modifiers: Int): Boolean {
    if (key >= 0 && keysDown.get(key)) {
      keysDown.set(key, false)

      CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureKeyPacket(
        key = key,
        down = false,
        captureMode = capturing
      ))

      return true
    }

    return false
  }

  private fun mapPosition(x: Double, y: Double): Pair<Double, Double> =
    (x * WIDTH / width).coerceIn(0.0, WIDTH.toDouble()) to (y * HEIGHT / height).coerceIn(0.0, HEIGHT.toDouble())

  override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
    if (!capturing || button !in 0..2) return false

    val (x, y) = mapPosition(mouseX, mouseY)

    CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureMousePacket(
      event  = Event.CLICK,
      button = button + 1, // lua indexed
      x      = x,
      y      = y,
    ))

    timeLastDragSent = -1L // in case the clock resets
    lastMouseButton = button
    lastMouseX = x
    lastMouseY = y

    return true
  }

  override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
    if (!capturing || button !in 0..2) return false

    val (x, y) = mapPosition(mouseX, mouseY)

    if (lastMouseButton == button) {
      CCHoloPacketHandler.channel.sendToServer(
        C2SCanvasCaptureMousePacket(
          event = Event.UP,
          button = button + 1, // lua indexed
          x = x,
          y = y,
        )
      )
      lastMouseButton = -1
    }

    lastMouseX = x
    lastMouseY = y

    return true
  }

  override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
    if (!capturing || button !in 0..2) return false

    val (x, y) = mapPosition(mouseX, mouseY)

    if (lastMouseButton == button) {
      pendingDragButton = button
      pendingDragX = x
      pendingDragY = y

      lastMouseX = x
      lastMouseY = y
    }

    return true
  }

  override fun mouseMoved(mouseX: Double, mouseY: Double) {
    if (!capturingMouseMove || lastMouseButton >= 0) return

    // Don't update lastMouse events for this
    val (x, y) = mapPosition(mouseX, mouseY)
    pendingMoveX = x
    pendingMoveY = y
  }

  override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
    if (!capturing) return false

    val (x, y) = mapPosition(mouseX, mouseY)

    CCHoloPacketHandler.channel.sendToServer(
      C2SCanvasCaptureScrollPacket(
        direction = if (delta > 0) 1 else -1,
        x = x,
        y = y,
      )
    )

    return true
  }

  private fun clearInputs() {
    for (i in 0 until keysDown.size()) {
      if (keysDown.get(i)) {
        CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureKeyPacket(
          key = i,
          down = false,
          captureMode = capturing
        ))
      }
    }

    keysDown.clear()

    if (lastMouseButton >= 0) {
      CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureMousePacket(
        event = Event.UP,
        button = lastMouseButton + 1, // lua indexed
        x = lastMouseX,
        y = lastMouseY,
      ))
      lastMouseButton = -1
    }
  }

  override fun setFocused(focused: Boolean) {
    super.setFocused(focused)

    if (!focused) {
      clearInputs()
    }
  }

  override fun removed() {
    super.removed()

    clearInputs()
  }

  override fun isPauseScreen() = false
}
