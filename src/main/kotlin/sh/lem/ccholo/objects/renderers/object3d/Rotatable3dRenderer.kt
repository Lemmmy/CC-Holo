package sh.lem.ccholo.objects.renderers.object3d

import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.phys.Vec3
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object Rotatable3dRenderer {
  fun applyRotation(gg: GuiGraphics, rot: Vec3?, flipPitch: Boolean) {
    val mc = Minecraft.getInstance()
    val poseStack = gg.pose()

    if (rot == null) {
      val cam = mc.gameRenderer.mainCamera
      poseStack.mulPose(Axis.YP.rotationDegrees(180 - cam.yRot))
      poseStack.mulPose(Axis.XP.rotationDegrees(if (flipPitch) -cam.xRot else cam.xRot))
    } else {
      poseStack.mulPose(Axis.XP.rotationDegrees(rot.x.toFloat()))
      poseStack.mulPose(Axis.YP.rotationDegrees(rot.y.toFloat()))
      poseStack.mulPose(Axis.ZP.rotationDegrees(rot.z.toFloat()))
    }
  }
}
