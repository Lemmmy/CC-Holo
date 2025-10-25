package sh.lem.ccholo.canvas

import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.lwjgl.glfw.GLFW
import sh.lem.ccholo.networking.C2SCanvasCaptureKeyPacket
import sh.lem.ccholo.networking.CCHoloPacketHandler

@Mod.EventBusSubscriber(Dist.CLIENT)
object CanvasInputHandler {
  private val mc by lazy { Minecraft.getInstance() }

  @SubscribeEvent
  fun onClientTick(event: TickEvent.ClientTickEvent) {
    // Open the key capture screen if an open is pending
    if (event.phase === TickEvent.Phase.END && CanvasRootClient.capturePendingOpen && mc.screen == null) {
      mc.setScreen(CanvasCapturingScreen())
      CanvasRootClient.capturePendingOpen = false
    }
  }

  @SubscribeEvent
  fun onKeyInput(event: InputEvent.Key) {
    // Forward subscribed key captures
    if (mc.screen == null && CanvasRootClient.keyCaptures.contains(event.key)) {
      CCHoloPacketHandler.channel.sendToServer(C2SCanvasCaptureKeyPacket(
        key = event.key,
        down = event.action == GLFW.GLFW_PRESS || event.action == GLFW.GLFW_REPEAT,
        repeat = event.action == GLFW.GLFW_REPEAT,
        captureMode = false
      ))
    }
  }
}
