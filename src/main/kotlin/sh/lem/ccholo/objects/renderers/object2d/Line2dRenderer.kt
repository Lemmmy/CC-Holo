package sh.lem.ccholo.objects.renderers.object2d

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object2d.Line2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Line2dRenderer: BaseObjectRenderer<Line2d> {
  override fun draw(
    obj: Line2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      BaseObjectRenderer.setupFlat()

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()
      val normal = poseStack.last().normal()

      // OpenGL has some limitations on normal lines, so we instead will create a quad,
      // and use two triangles to "build" the quad.
      // @fatboychummy — https://github.com/ReconnectedCC/Re-Plethora/pull/7
      RenderSystem.setShader(GameRenderer::getPositionColorShader)
      builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

      // Calculate thickness and the perpendicular direction to do it in.
      val dx = end.y - start.y
      val dy = -(end.x - start.x)
      val length = kotlin.math.sqrt(dx * dx + dy * dy)
      val thickness = scale / 2f

      // Normalize and scale by thickness
      val offsetX = (dx / length) * thickness
      val offsetY = (dy / length) * thickness

      // Create four corners of the quad
      val x1 = start.x - offsetX
      val y1 = start.y - offsetY
      val x2 = start.x + offsetX
      val y2 = start.y + offsetY
      val x3 = end.x - offsetX
      val y3 = end.y - offsetY
      val x4 = end.x + offsetX
      val y4 = end.y + offsetY

      // First Triangle: x1/y1 -> x2/y2 -> x3/y3
      builder.vertex(pose, x1, y1, 0f).color(r, g, b, a).normal(normal, 0f, 1f, 0f).endVertex()
      builder.vertex(pose, x2, y2, 0f).color(r, g, b, a).normal(normal, 0f, 1f, 0f).endVertex()
      builder.vertex(pose, x3, y3, 0f).color(r, g, b, a).normal(normal, 0f, 1f, 0f).endVertex()

      // Second Triangle: x3/y3 -> x2/y2 -> x4/y4
      builder.vertex(pose, x3, y3, 0f).color(r, g, b, a).normal(normal, 0f, 1f, 0f).endVertex()
      builder.vertex(pose, x2, y2, 0f).color(r, g, b, a).normal(normal, 0f, 1f, 0f).endVertex()
      builder.vertex(pose, x4, y4, 0f).color(r, g, b, a).normal(normal, 0f, 1f, 0f).endVertex()

      BufferUploader.drawWithShader(builder.end())
    }
  }
}
