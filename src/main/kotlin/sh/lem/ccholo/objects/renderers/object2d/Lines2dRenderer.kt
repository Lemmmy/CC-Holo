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
import sh.lem.ccholo.objects.object2d.Lines2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Lines2dRenderer: BaseObjectRenderer<Lines2d> {
  override fun draw(
    obj: Lines2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      val tris = cachedTriangles ?: return

      BaseObjectRenderer.setupFlat()

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()

      builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

      tris.forEach {
        builder.vertex(pose, it.v0.x, it.v0.y, 0f).color(r, g, b, a).endVertex()
        builder.vertex(pose, it.v1.x, it.v1.y, 0f).color(r, g, b, a).endVertex()
        builder.vertex(pose, it.v2.x, it.v2.y, 0f).color(r, g, b, a).endVertex()
      }

      BufferUploader.drawWithShader(builder.end())
    }
  }
}
