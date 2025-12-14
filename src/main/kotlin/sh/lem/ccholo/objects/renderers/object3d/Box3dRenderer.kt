package sh.lem.ccholo.objects.renderers.object3d

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.joml.Matrix4f
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object3d.Box3d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Box3dRenderer: BaseObjectRenderer<Box3d> {
  override fun draw(
    obj: Box3d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      BaseObjectRenderer.setupFlat()

      if (hasDepthTest) {
        RenderSystem.enableDepthTest()
      } else {
        RenderSystem.disableDepthTest()
      }

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder

      poseStack.pushPose()
      val pose = poseStack.last().pose()

      pose.translate(position.x.toFloat(), position.y.toFloat(), position.z.toFloat())

      pose.translate(width.toFloat() / 2f, height.toFloat() / 2f, depth.toFloat() / 2f)
      Rotatable3dRenderer.applyRotation(gg, rotation, true)
      pose.translate(-width.toFloat() / 2f, -height.toFloat() / 2f, -depth.toFloat() / 2f)

      builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

      // Down (Y-)
      // v0 v1 v2
      builder.vertex(pose, 0.0, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, 0.0, depth).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, 0.0, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, 0.0, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, 0.0, 0.0, depth).color(r, g, b, a).endVertex()

      // Up (Y+)
      // v0 v1 v2
      builder.vertex(pose, 0.0, height, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, 0.0, height, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, depth).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, 0.0, height, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, 0.0).color(r, g, b, a).endVertex()

      // North (Z-)
      // v0 v1 v2
      builder.vertex(pose, 0.0, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, 0.0, height, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, 0.0).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, 0.0, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, 0.0, 0.0).color(r, g, b, a).endVertex()

      // South (Z+)
      // v0 v1 v2
      builder.vertex(pose, 0.0, 0.0, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, 0.0, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, depth).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, 0.0, 0.0, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, 0.0, height, depth).color(r, g, b, a).endVertex()

      // East (X+)
      // v0 v1 v2
      builder.vertex(pose, width, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, depth).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, width, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, height, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, width, 0.0, depth).color(r, g, b, a).endVertex()

      // West (X-)
      // v0 v1 v2
      builder.vertex(pose, 0.0, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, 0.0, 0.0, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, 0.0, height, depth).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, 0.0, 0.0, 0.0).color(r, g, b, a).endVertex()
      builder.vertex(pose, 0.0, height, depth).color(r, g, b, a).endVertex()
      builder.vertex(pose, 0.0, height, 0.0).color(r, g, b, a).endVertex()

      BufferUploader.drawWithShader(builder.end())

      poseStack.popPose()
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun VertexConsumer.vertex(matrix: Matrix4f, x: Double, y: Double, z: Double) =
    vertex(matrix, x.toFloat(), y.toFloat(), z.toFloat())
}
