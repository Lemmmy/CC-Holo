package sh.lem.ccholo.objects.renderers.object3d

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object3d.ObjectRoot3d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object ObjectRoot3dRenderer: BaseObjectRenderer<ObjectRoot3d> {
  override fun draw(
    obj: ObjectRoot3d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource?
  ) {
    with (obj) {
      val children = canvasRootClient.getChildren(id) ?: return

      val poseStack = gg.pose()
      val mc = Minecraft.getInstance()
      val entity = mc.cameraEntity ?: return
      if (entity.level().dimension().location() != levelKey) return

      val view = mc.gameRenderer.mainCamera.position

      poseStack.pushPose()
      poseStack.translate(origin.x - view.x, origin.y - view.y, origin.z - view.z)

      canvasRootClient.drawChildren(children.iterator(), gg, buf)

      poseStack.popPose()
    }
  }
}
