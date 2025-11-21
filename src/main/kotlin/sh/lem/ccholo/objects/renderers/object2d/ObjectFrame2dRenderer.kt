package sh.lem.ccholo.objects.renderers.object2d

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object2d.ObjectFrame2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer
import sh.lem.ccholo.objects.renderers.FrameRenderer
import sh.lem.ccholo.objects.renderers.object3d.Rotatable3dRenderer

@SideOnly(Side.CLIENT)
object ObjectFrame2dRenderer: BaseObjectRenderer<ObjectFrame2d> {
  override fun draw(
    obj: ObjectFrame2d,
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
        ForgeHooksClient.getGuiFarPlane()
      )
      if (framebuffer == null) {
        FrameRenderer.restoreFog()
        return
      }

      // ==============================
      // Draw the framebuffer on the screen

      val poseStack = gg.pose()
      poseStack.pushPose()

      poseStack.translate(position.x.toDouble(), position.y.toDouble(), 0.0)
      poseStack.scale(scale, scale, 1.0f)
      Rotatable3dRenderer.applyRotation(gg, rotation, true)

      FrameRenderer.drawFramebufferQuad(framebuffer, poseStack, w, h)
      FrameRenderer.restoreFog()

      poseStack.popPose()
    }
  }
}
