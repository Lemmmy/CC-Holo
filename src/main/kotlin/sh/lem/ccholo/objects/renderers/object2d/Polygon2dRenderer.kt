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
import sh.lem.ccholo.objects.object2d.Polygon2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Polygon2dRenderer: BaseObjectRenderer<Polygon2d> {
  override fun draw(
    obj: Polygon2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      BaseObjectRenderer.setupFlat()

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()

      builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

      val pA = points[0]
      for (i in 1 until points.size - 1) {
        val pB = points[i]
        val pC = points[i + 1]
        builder.vertex(pose, pA.x, pA.y, 0f).color(r, g, b, a).endVertex()
        builder.vertex(pose, pB.x, pB.y, 0f).color(r, g, b, a).endVertex()
        builder.vertex(pose, pC.x, pC.y, 0f).color(r, g, b, a).endVertex()
      }

      BufferUploader.drawWithShader(builder.end())
    }
  }
}
