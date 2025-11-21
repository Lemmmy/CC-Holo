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
import sh.lem.ccholo.objects.object2d.Triangle2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Triangle2dRenderer: BaseObjectRenderer<Triangle2d> {
  override fun draw(
    obj: Triangle2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      if (points.size != 3) return; // should never happen

      BaseObjectRenderer.setupFlat()

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()

      builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

      points.forEach {
        builder.vertex(pose, it.x, it.y, 0f).color(r, g, b, a).endVertex()
      }

      BufferUploader.drawWithShader(builder.end())
    }
  }
}
