package sh.lem.ccholo.objects.renderers.object2d

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.ItemObject
import sh.lem.ccholo.objects.object2d.Item2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Item2dRenderer: BaseObjectRenderer<Item2d> {
  private val mc by lazy { Minecraft.getInstance() }

  override fun draw(
    obj: Item2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      val item = item ?: return
      val stack = stack ?: ItemObject.tryMakeStack(item, nbt).also { stack = it }
      if (stack.isEmpty) return

      val poseStack = gg.pose()

      RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
      RenderSystem.enableDepthTest()
      RenderSystem.enableBlend()
      RenderSystem.blendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO
      )

      poseStack.pushPose()
      poseStack.translate(position.x, position.y, 0.0f)
      poseStack.scale(scale, scale, 1f)

      val player = mc.player
      if (player != null) {
        gg.renderItem(player, stack, 0, 0, id)
      } else {
        gg.renderItem(stack, 0, 0, id)
      }

      poseStack.popPose()

      RenderSystem.enableBlend()
    }
  }
}
