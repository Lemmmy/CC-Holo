package sh.lem.ccholo.objects.renderers.object2d

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object2d.Item2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Item2dRenderer: BaseObjectRenderer<Item2d> {
  override fun draw(
    obj: Item2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource?
  ) {
    with (obj) {
      val item = item ?: return

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

      val stack = stack ?: ItemStack(item).also { stack = it }

      // val renderStack = RenderSystem.getModelViewStack()
      // renderStack.pushPose()
      // renderStack.mulPoseMatrix(poseStack.last().pose())
      // RenderSystem.applyModelViewMatrix()

      poseStack.pushPose()
      poseStack.translate(position.x, position.y, 0.0f)
      poseStack.scale(scale, scale, 1f)
      gg.renderItem(stack, 0, 0)
      poseStack.popPose()

      // renderStack.popPose()
      // RenderSystem.applyModelViewMatrix()
      RenderSystem.enableBlend()
    }
  }
}
