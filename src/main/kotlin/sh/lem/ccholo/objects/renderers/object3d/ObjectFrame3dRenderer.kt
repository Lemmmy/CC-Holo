package sh.lem.ccholo.objects.renderers.object3d

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object3d.ObjectFrame3d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer
import sh.lem.ccholo.objects.renderers.FrameRenderer

@SideOnly(Side.CLIENT)
object ObjectFrame3dRenderer: BaseObjectRenderer<ObjectFrame3d> {
  override fun draw(
    obj: ObjectFrame3d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      val children = canvasRootClient.getChildren(id) ?: return
      val w = width.toFloat(); val h = height.toFloat()

      // ==============================
      // Draw the children into the framebuffer

      FrameRenderer.saveAndSetFog()
      val framebuffer = FrameRenderer.renderToFramebuffer(
        width,
        height,
        children.iterator(),
        canvasRootClient,
        300.0f
      )
      if (framebuffer == null) {
        FrameRenderer.restoreFog()
        return
      }

      // ==============================
      // Draw the framebuffer in the world

      val poseStack = gg.pose()
      poseStack.pushPose()

      poseStack.translate(position.x, position.y, position.z)

      poseStack.scale(scale, -scale, scale)
      poseStack.translate(w / 2f, h / 2f, 0f)
      Rotatable3dRenderer.applyRotation(gg, rotation, false)
      poseStack.translate(-w / 2f, -h / 2f, 0f)

      if (hasDepthTest) {
        RenderSystem.enableDepthTest()
      } else {
        RenderSystem.disableDepthTest()
      }

      FrameRenderer.drawFramebufferQuad(framebuffer, poseStack, w, h)
      FrameRenderer.restoreFog()

      poseStack.popPose()
    }
  }
}
