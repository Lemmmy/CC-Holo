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
   * @param gg The graphics context - for a 2D canvas this is the real GuiGraphics instance, for a 3D canvas this is a
   *           fake one
   * @param buf The buffer source - same as above
   */
  fun draw(obj: T, root: CanvasRootClient, gg: GuiGraphics, buf: MultiBufferSource?)

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
