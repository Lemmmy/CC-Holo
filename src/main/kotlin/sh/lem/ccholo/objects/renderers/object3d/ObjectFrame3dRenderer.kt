package sh.lem.ccholo.objects.renderers.object3d

import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.joml.Matrix4f
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_WIDTH
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object3d.ObjectFrame3d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object ObjectFrame3dRenderer: BaseObjectRenderer<ObjectFrame3d> {
  val framebuffer by lazy {
    CCHolo.log.debug("Creating ObjectFrame3d framebuffer with size $BASE_WIDTH x $BASE_HEIGHT")
    TextureTarget(BASE_WIDTH, BASE_HEIGHT, true, true)
  }

  override fun draw(
    obj: ObjectFrame3d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource?
  ) {
    with (obj) {
      val children = canvasRootClient.getChildren(id) ?: return

      val mc = Minecraft.getInstance()
      val w = BASE_WIDTH.toFloat(); val h = BASE_HEIGHT.toFloat()

      // val oldBuffer = GlStateManager.getBoundFramebuffer()
      val oldFog = RenderSystem.getShaderFogEnd()
      val oldFogColor = RenderSystem.getShaderFogColor()
      RenderSystem.setShaderFogEnd(2000.0f)
      RenderSystem.setShaderFogColor(0.0f, 0.0f, 0.0f, 0.0f)

      // ==============================
      // Draw the children into the framebuffer

      RenderSystem.backupProjectionMatrix()

      val matrix4f = Matrix4f().setOrtho(0.0f, w, h, 0.0f, 100.0f, 300.0f)
      RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z)

      val innerPoseStack = PoseStack()
      innerPoseStack.setIdentity()
      innerPoseStack.translate(0.0, 0.0, -100.0)

      RenderSystem.colorMask(true, true, true, true)
      framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f)
      framebuffer.clear(Minecraft.ON_OSX)
      framebuffer.bindWrite(true)

      RenderSystem.disableDepthTest()
      val innerGg = GuiGraphics(mc, innerPoseStack, mc.renderBuffers().bufferSource())
      canvasRootClient.drawChildren(children.iterator(), innerGg, buf)

      RenderSystem.viewport(0, 0, mc.window.width, mc.window.height)
      RenderSystem.restoreProjectionMatrix()
      framebuffer.unbindWrite()

      // Iris/Oculus compatibility: this feels wrong, but technically we're supposed to re-bind back to the game's main
      // render target instead of the previous one. Iris/Oculus has a mixin to bindWrite which tracks the render state—
      // disabling shaders when binding to a non-main render target—and calling _glBindFramebuffer(old) here would
      // avoid that code path, breaking subsequent rendering. Hopefully, binding back to main here won't cause *other*
      // problems.
      mc.mainRenderTarget.bindWrite(false)
      // GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, oldBuffer)

      // ==============================
      // Draw the framebuffer in the world

      val poseStack = gg.pose()
      poseStack.pushPose()

      poseStack.translate(position.x, position.y, position.z)
      poseStack.scale(scale, -scale, scale)
      Rotatable3dRenderer.applyRotation(gg, rotation, true)

      if (hasDepthTest) {
        RenderSystem.enableDepthTest()
      } else {
        RenderSystem.disableDepthTest()
      }

      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()

      RenderSystem.setShader(GameRenderer::getPositionTexShader)
      RenderSystem.setShaderTexture(0, framebuffer.colorTextureId)
      RenderSystem.enableBlend()

      builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
      builder.vertex(pose, 0.0f, h, 0.0f).uv(0.0f, 0.0f).endVertex()
      builder.vertex(pose, w, h, 0.0f).uv(1.0f, 0.0f).endVertex()
      builder.vertex(pose, w, 0.0f, 0.0f).uv(1.0f, 1.0f).endVertex()
      builder.vertex(pose, 0.0f, 0.0f, 0.0f).uv(0.0f, 1.0f).endVertex()
      BufferUploader.drawWithShader(builder.end())

      RenderSystem.setShaderFogEnd(oldFog)
      RenderSystem.setShaderFogColor(oldFogColor[0], oldFogColor[1], oldFogColor[2], oldFogColor[3])

      poseStack.popPose()
    }
  }
}
