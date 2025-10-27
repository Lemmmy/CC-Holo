package sh.lem.ccholo.canvas

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(Dist.CLIENT)
object CanvasRenderer {
  private val mc by lazy { Minecraft.getInstance() }

  @SubscribeEvent
  fun onOverlayPre(event: RenderGuiOverlayEvent.Pre) {
    // Render 2D canvases before the hotbar (and before the spectator tablist; this mirrors the Fabric mixin which in
    // turn was meant to mirror the original Forge event)
    if (event.overlay === VanillaGuiOverlay.HOTBAR.type()) {
      mc.profiler.push("plethora:renderCanvas2DOverlay")
      renderCanvas2DOverlay(event.guiGraphics)
      mc.profiler.pop()
    }
  }

  @SubscribeEvent
  fun onRenderLevelStage(event: RenderLevelStageEvent) {
    // Render 3D canvases after translucent blocks, so they appear in the world properly
    if (event.stage === RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
      mc.profiler.push("plethora:renderCanvas3DOverlay")
      renderCanvas3DOverlay(event.poseStack)
      mc.profiler.pop()
    }
  }

  fun renderCanvas2DOverlay(gg: GuiGraphics) {
    val children = CanvasRootClient.getChildren(CanvasRoot.ID_2D) ?: return

    // If we've no text renderer then we're probably not quite ready yet
    if (mc.font == null) return

    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

    // Remove the fog so that 3D canvases render properly from a distance
    val currentFog = RenderSystem.getShaderFogEnd()
    val currentFogColor = RenderSystem.getShaderFogColor()
    RenderSystem.setShaderFogEnd(2000.0f) // TODO: may need to grow this significantly
    RenderSystem.setShaderFogColor(0.0f, 0.0f, 0.0f, 0.0f)

    val poseStack = gg.pose()
    poseStack.pushPose()

    // The hotbar renders at -90 (see Gui#renderHotbar)
    poseStack.translate(0.0, 0.0, -200.0)
    poseStack.scale(mc.window.guiScaledWidth.toFloat() / CanvasRoot.WIDTH, mc.window.guiScaledHeight.toFloat() / CanvasRoot.HEIGHT, 1f)

    CanvasRootClient.drawChildren(children.iterator(), gg, null)

    // Restore the renderer state
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    RenderSystem.enableCull()
    RenderSystem.defaultBlendFunc()
    RenderSystem.enableBlend()

    RenderSystem.setShaderFogEnd(currentFog)
    RenderSystem.setShaderFogColor(currentFogColor[0], currentFogColor[1], currentFogColor[2], currentFogColor[3])

    poseStack.popPose()
  }

  fun renderCanvas3DOverlay(poseStack: PoseStack) {
    val children = CanvasRootClient.getChildren(CanvasRoot.ID_3D) ?: return
    val buf = mc.renderBuffers().bufferSource()
    val gg = GuiGraphics(mc, poseStack, buf)

    CanvasRootClient.drawChildren(children.iterator(), gg, buf)
  }
}
