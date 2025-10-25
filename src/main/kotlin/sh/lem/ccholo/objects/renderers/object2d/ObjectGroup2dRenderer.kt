package sh.lem.ccholo.objects.renderers.object2d

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object2d.ObjectGroup2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object ObjectGroup2dRenderer: BaseObjectRenderer<ObjectGroup2d> {
  override fun draw(
    obj: ObjectGroup2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource?
  ) {
    with (obj) {
      val children = canvasRootClient.getChildren(id) ?: return

      val poseStack = gg.pose()

      poseStack.pushPose()
      poseStack.translate(position.x, position.y, 0f)

      canvasRootClient.drawChildren(children.iterator(), gg, buf)

      poseStack.popPose()
    }
  }
}
