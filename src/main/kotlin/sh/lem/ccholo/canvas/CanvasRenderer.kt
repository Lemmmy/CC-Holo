package sh.lem.ccholo.canvas

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.IGuiOverlay
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import sh.lem.ccholo.objects.renderers.FramebufferPool

@Mod.EventBusSubscriber(Dist.CLIENT)
object CanvasRenderer {
  private val mc by lazy { Minecraft.getInstance() }

  @SubscribeEvent
  fun onRegisterGuiOverlays(event: RegisterGuiOverlaysEvent) {
    // Render 2D canvases AFTER the hotbar (and still before the spectator tablist; this differs from the Fabric mixin
    // which in turn was meant to mirror the original Forge event, which rendered before the hotbar)
    event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), Canvas2dOverlay.ID, Canvas2dOverlay)
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

  @SubscribeEvent
  fun onRenderTick(event: TickEvent.RenderTickEvent) {
    when (event.phase) {
      TickEvent.Phase.START -> FramebufferPool.beginFrame()
      TickEvent.Phase.END -> FramebufferPool.endFrame()
    }
  }

  fun renderCanvas2dOverlay() {
    val children = CanvasRootClient.getChildren(CanvasRoot.ID_2D) ?: return

    // If we've no text renderer then we're probably not quite ready yet
    if (mc.font == null) return

    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

    val innerPoseStack = PoseStack()
    innerPoseStack.setIdentity()
    innerPoseStack.translate(0.0, 0.0, -100.0)

    val buf = MultiBufferSource.immediate(Tesselator.getInstance().builder)
    val gg = GuiGraphics(mc, innerPoseStack, buf)

    // Remove the fog so that 3D canvases render properly from a distance
    val currentFog = RenderSystem.getShaderFogEnd()
    val currentFogColor = RenderSystem.getShaderFogColor()
    RenderSystem.setShaderFogEnd(2000.0f) // TODO: may need to grow this significantly
    RenderSystem.setShaderFogColor(0.0f, 0.0f, 0.0f, 0.0f)

    CanvasRootClient.drawChildren(children.iterator(), gg, buf)

    // Restore the renderer state
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    RenderSystem.enableCull()
    RenderSystem.defaultBlendFunc()
    RenderSystem.enableBlend()

    RenderSystem.setShaderFogEnd(currentFog)
    RenderSystem.setShaderFogColor(currentFogColor[0], currentFogColor[1], currentFogColor[2], currentFogColor[3])

    innerPoseStack.popPose()
  }

  fun renderCanvas3DOverlay(poseStack: PoseStack) {
    val children = CanvasRootClient.getChildren(CanvasRoot.ID_3D) ?: return
    val buf = mc.renderBuffers().bufferSource()
    val gg = GuiGraphics(mc, poseStack, buf)

    CanvasRootClient.drawChildren(children.iterator(), gg, buf)
  }

  object Canvas2dOverlay : IGuiOverlay {
    const val ID = "canvas_2d"

    override fun render(
      gui: ForgeGui?,
      gg: GuiGraphics?,
      partialTick: Float,
      screenWidth: Int,
      screenHeight: Int
    ) {
      mc.profiler.push("plethora:renderCanvas2DOverlay")
      renderCanvas2dOverlay()
      mc.profiler.pop()
    }
  }
}
