package sh.lem.ccholo.objects.renderers.object3d

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object3d.Box3d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Box3dRenderer: BaseObjectRenderer<Box3d> {
  override fun draw(
    obj: Box3d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource?
  ) {
    with (obj) {
      BaseObjectRenderer.setupFlat()

      if (hasDepthTest) {
        RenderSystem.enableDepthTest()
      } else {
        RenderSystem.disableDepthTest()
      }

      val minX = position.x.toFloat(); val minY = position.y.toFloat(); val minZ = position.z.toFloat()
      val maxX = (minX + width).toFloat(); val maxY = (minY + height).toFloat(); val maxZ = (minZ + depth).toFloat()

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()

      builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

      // Down (Y-)
      // v0 v1 v2
      builder.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex()

      // Up (Y+)
      // v0 v1 v2
      builder.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex()

      // North (Z-)
      // v0 v1 v2
      builder.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex()

      // South (Z+)
      // v0 v1 v2
      builder.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex()

      // East (X+)
      // v0 v1 v2
      builder.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, maxX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, minY, maxZ).color(r, g, b, a).endVertex()

      // West (X-)
      // v0 v1 v2
      builder.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, minY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex()
      // v0 v2 v3
      builder.vertex(pose, minX, minY, minZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, maxY, maxZ).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, maxY, minZ).color(r, g, b, a).endVertex()

      BufferUploader.drawWithShader(builder.end())
    }
  }
}
