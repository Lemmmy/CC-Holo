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
import sh.lem.ccholo.objects.object2d.Dot2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Dot2dRenderer: BaseObjectRenderer<Dot2d> {
  override fun draw(
    obj: Dot2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      BaseObjectRenderer.setupFlat()

      val x = position.x; val y = position.y
      val delta = scale / 2

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()

      builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

      builder.vertex(pose, x - delta, y - delta, 0f).color(r, g, b, a).endVertex()
      builder.vertex(pose, x - delta, y + delta, 0f).color(r, g, b, a).endVertex()
      builder.vertex(pose, x + delta, y + delta, 0f).color(r, g, b, a).endVertex()

      builder.vertex(pose, x - delta, y - delta, 0f).color(r, g, b, a).endVertex()
      builder.vertex(pose, x + delta, y + delta, 0f).color(r, g, b, a).endVertex()
      builder.vertex(pose, x + delta, y - delta, 0f).color(r, g, b, a).endVertex()

      BufferUploader.drawWithShader(builder.end())
    }
  }
}
