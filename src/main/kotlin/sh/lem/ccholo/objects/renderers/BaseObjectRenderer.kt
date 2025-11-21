package sh.lem.ccholo.objects.renderers

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.BaseObject

@SideOnly(Side.CLIENT)
interface BaseObjectRenderer<T : BaseObject> {
  /**
   * Draw this object
   *
   * @param obj The object to draw
   * @param root The canvas root
   * @param gg The graphics context to supply gui utility rendering functions (uses 'buf' as a buffer source)
   * @param buf The buffer source - same as above
   */
  fun draw(obj: T, root: CanvasRootClient, gg: GuiGraphics, buf: MultiBufferSource)

  companion object {
    /**
     * Prepare to draw a flat object
     */
    fun setupFlat() {
      RenderSystem.disableCull()
      RenderSystem.enableBlend()
      RenderSystem.setShader(GameRenderer::getPositionColorShader)
    }
  }
}
