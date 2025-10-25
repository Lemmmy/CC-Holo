package sh.lem.ccholo.objects.renderers.object2d

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.Tesselator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture.FULL_BRIGHT
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.TextObject.Companion.TAB_WIDTH
import sh.lem.ccholo.objects.object2d.Text2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Text2dRenderer: BaseObjectRenderer<Text2d> {
  override fun draw(
    obj: Text2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource?
  ) {
    with (obj) {
      // If the alpha channel doesn't match a 0xFC, then the font renderer
      // will make it opaque. We also early exit here if we're transparent.
      val alpha = colour and 0xFF
      if (alpha == 0) return
      if (alpha and 0xFC == 0) colour = colour or 0x4

      val plaintext = plaintext
      val component = component
      if (plaintext == null && component == null) return

      BaseObjectRenderer.setupFlat()

      val font = Minecraft.getInstance().font

      val poseStack = gg.pose()
      poseStack.pushPose()
      poseStack.translate(position.x.toDouble(), position.y.toDouble(), 0.0)
      poseStack.scale(scale, scale, 1.0f)

      val builder = Tesselator.getInstance().builder
      val immediate = MultiBufferSource.immediate(builder)
      val pose = poseStack.last().pose()

      if (component != null) {
        // Component rendering - supports full Minecraft text JSON
        font.drawInBatch(
          component, 0f, 0f, colour, dropShadow, pose, immediate, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT
        )
      } else {
        // Plaintext rendering - supports newlines and tab stops
        var y = 0
        for (fullLine in lines) {
          var x = 0
          for (tabSection in fullLine) {
            // We use 0xRRGGBBAA, but the font renderer expects 0xAARRGGBB, so we rotate the bits
            x = font.drawInBatch(
              tabSection, x.toFloat(), y.toFloat(), Integer.rotateRight(colour, 8), dropShadow, pose, immediate,
              Font.DisplayMode.NORMAL, 0, FULL_BRIGHT
            )

            // Round the X coordinate to the next tab stop.
            x = x / TAB_WIDTH * TAB_WIDTH + TAB_WIDTH
          }

          y += lineHeight.toInt()
        }
      }

      immediate.endBatch()

      RenderSystem.enableBlend()

      poseStack.popPose()
    }
  }
}
