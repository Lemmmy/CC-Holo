package sh.lem.ccholo.objects.renderers

import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import it.unimi.dsi.fastutil.ints.IntIterator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.joml.Matrix4f
import org.lwjgl.opengl.GL30
import sh.lem.ccholo.canvas.CanvasRootClient

@SideOnly(Side.CLIENT)
object FrameRenderer {
  private var oldFogEnd: Float? = null
  private var oldFogColor: FloatArray? = null

  private val mc by lazy { Minecraft.getInstance() }
  
  /**
   * Binds a framebuffer for rendering. This should be used instead of calling bindWrite directly
   * as it tracks state for proper unbinding.
   */
  fun bindFramebuffer(framebuffer: TextureTarget) {
    framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    framebuffer.clear(Minecraft.ON_OSX)
    framebuffer.bindWrite(true)
  }
  
  /**
   * Unbinds a framebuffer and restores the previous framebuffer binding and projection matrix state. If the framebuffer
   * stack is now empty, binds to the main render target and restores projection matrix normally.
   */
  fun unbindFramebuffer() {
    // Release from pool and fetch the previous framebuffer and projection state from the stack
    val (previousFramebuffer, projectionState) = FramebufferPool.release()

    if (previousFramebuffer != null) {
      // Restore previous framebuffer
      GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer)
    } else {
      // Iris/Oculus compatibility: this feels wrong, but technically we're supposed to re-bind back to the game's main
      // render target instead of the previous one. Iris/Oculus has a mixin to bindWrite which tracks the render state—
      // disabling shaders when binding to a non-main render target—and calling _glBindFramebuffer(old) here would
      // avoid that code path, breaking subsequent rendering. Hopefully, binding back to main here won't cause *other*
      // problems.
      mc.mainRenderTarget.bindWrite(true)
    }

    // Restore projection matrix state if we have it
    projectionState?.let { (projMatrix, sorting) ->
      RenderSystem.setProjectionMatrix(projMatrix, sorting)
    }
  }

  fun saveAndSetFog() {
    oldFogEnd = RenderSystem.getShaderFogEnd()
    oldFogColor = RenderSystem.getShaderFogColor()
    RenderSystem.setShaderFogEnd(2000.0f)
    RenderSystem.setShaderFogColor(0.0f, 0.0f, 0.0f, 0.0f)
  }

  fun restoreFog() {
    oldFogEnd?.let { RenderSystem.setShaderFogEnd(it) }
    oldFogColor?.let { RenderSystem.setShaderFogColor(it[0], it[1], it[2], it[3]) }
  }

  /**
   * Renders children into a framebuffer and returns the framebuffer.
   * Returns null if framebuffer acquisition fails.
   */
  fun renderToFramebuffer(
    width: Int,
    height: Int,
    children: IntIterator,
    canvasRootClient: CanvasRootClient,
    farPlane: Float
  ): TextureTarget? {
    val w = width.toFloat(); val h = height.toFloat()
    
    // Acquire framebuffer from pool
    val framebuffer = FramebufferPool.acquire(width, height) ?: return null

    // ==============================
    // Draw the children into the framebuffer

    bindFramebuffer(framebuffer)

    val matrix4f = Matrix4f().setOrtho(0.0f, w, h, 0.0f, -100.0f, farPlane)
    RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z)

    val pose = PoseStack().apply {
      setIdentity()
      translate(0.0, 0.0, -100.0)
    }

    val buf = MultiBufferSource.immediate(Tesselator.getInstance().builder)
    val gg = GuiGraphics(mc, pose, buf)
    canvasRootClient.drawChildren(children, gg, buf)

    gg.flush()
    framebuffer.unbindWrite()
    unbindFramebuffer()

    return framebuffer
  }

  /**
   * Draws a framebuffer as a textured quad.
   */
  fun drawFramebufferQuad(framebuffer: TextureTarget, poseStack: PoseStack, width: Float, height: Float) {
    val builder = Tesselator.getInstance().builder
    val pose = poseStack.last().pose()

    RenderSystem.setShader(GameRenderer::getPositionTexShader)
    RenderSystem.setShaderTexture(0, framebuffer.colorTextureId)
    RenderSystem.enableBlend()

    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
    builder.vertex(pose, 0.0f, height, 0.0f).uv(0.0f, 0.0f).endVertex()
    builder.vertex(pose, width, height, 0.0f).uv(1.0f, 0.0f).endVertex()
    builder.vertex(pose, width, 0.0f, 0.0f).uv(1.0f, 1.0f).endVertex()
    builder.vertex(pose, 0.0f, 0.0f, 0.0f).uv(0.0f, 1.0f).endVertex()
    BufferUploader.drawWithShader(builder.end())
  }
}
