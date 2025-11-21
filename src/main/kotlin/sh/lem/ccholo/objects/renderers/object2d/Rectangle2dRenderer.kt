package sh.lem.ccholo.objects.renderers.object2d

import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object2d.Rectangle2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Rectangle2dRenderer: BaseObjectRenderer<Rectangle2d> {
  override fun draw(
    obj: Rectangle2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      BaseObjectRenderer.setupFlat()

      val minX = position.x; val minY = position.y
      val maxX = minX + width; val maxY = minY + height

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()

      builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

      builder.vertex(pose, minX, minY, 0.0f).color(r, g, b, a).endVertex()
      builder.vertex(pose, minX, maxY, 0.0f).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, 0.0f).color(r, g, b, a).endVertex()

      builder.vertex(pose, minX, minY, 0.0f).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, maxY, 0.0f).color(r, g, b, a).endVertex()
      builder.vertex(pose, maxX, minY, 0.0f).color(r, g, b, a).endVertex()

      BufferUploader.drawWithShader(builder.end())
    }
  }
}
