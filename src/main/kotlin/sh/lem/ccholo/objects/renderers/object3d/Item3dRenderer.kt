package sh.lem.ccholo.objects.renderers.object3d

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.Tesselator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture.FULL_BRIGHT
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
import net.minecraft.world.item.ItemDisplayContext
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.ItemObject
import sh.lem.ccholo.objects.object3d.Item3d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Item3dRenderer: BaseObjectRenderer<Item3d> {
  private val mc by lazy { Minecraft.getInstance() }
  private val itemRenderer by lazy { mc.itemRenderer }

  override fun draw(
    obj: Item3d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      val item = item ?: return
      val stack = stack ?: ItemObject.tryMakeStack(item, nbt).also { stack = it }
      if (stack.isEmpty) return

      val poseStack = gg.pose()
      poseStack.pushPose()

      poseStack.translate(position.x, position.y, position.z)
      poseStack.scale(scale, scale, scale)
      Rotatable3dRenderer.applyRotation(gg, rotation, true)

      val builder = Tesselator.getInstance().builder
      val immediate = MultiBufferSource.immediate(builder)

      if (hasDepthTest) {
        RenderSystem.enableDepthTest()
      } else {
        RenderSystem.disableDepthTest()
      }

      itemRenderer.renderStatic(stack, ItemDisplayContext.NONE, FULL_BRIGHT, NO_OVERLAY, poseStack,
        immediate, mc.level, 0)

      immediate.endBatch()

      poseStack.popPose()
    }
  }
}
