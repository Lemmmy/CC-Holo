package sh.lem.ccholo.objects.renderers.object2d

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.Tesselator
import net.minecraft.CrashReport
import net.minecraft.ReportedException
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture.FULL_BRIGHT
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.registries.ForgeRegistries
import org.joml.Matrix4f
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.ItemObject
import sh.lem.ccholo.objects.object2d.Item2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer

@SideOnly(Side.CLIENT)
object Item2dRenderer: BaseObjectRenderer<Item2d> {
  private val mc by lazy { Minecraft.getInstance() }

  override fun draw(
    obj: Item2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      val item = item ?: return
      val stack = stack ?: ItemObject.tryMakeStack(item, nbt).also { stack = it }
      if (stack.isEmpty) return

      val poseStack = gg.pose()

      RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
      RenderSystem.enableDepthTest()
      RenderSystem.enableBlend()
      RenderSystem.blendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO
      )

      poseStack.pushPose()
      poseStack.translate(position.x, position.y, 0.0f)
      poseStack.scale(scale, scale, 1f)

      val builder = Tesselator.getInstance().builder
      val immediate = MultiBufferSource.immediate(builder)

      // Item renderer code based on GuiGraphics.renderItem to allow customising the display context
      val player = mc.player
      val model = mc.itemRenderer.getModel(stack, player?.level(), player, id)
      poseStack.translate(8f, 8f, 150f)

      try {
        poseStack.mulPoseMatrix(Matrix4f().scaling(1f, -1f, 1f)) // no idea, but it flips the normals if you remove this
        poseStack.scale(16f, 16f, 16f)
        val unlit = !model.usesBlockLight() || forceUnlit
        if (unlit) Lighting.setupForFlatItems()

        mc.itemRenderer.render(stack, displayContext, false, poseStack, immediate, FULL_BRIGHT, NO_OVERLAY, model)

        RenderSystem.disableDepthTest()
        immediate.endBatch()
        RenderSystem.enableDepthTest()

        if (unlit) Lighting.setupFor3DItems()
      } catch (e: Throwable) {
        val report = CrashReport.forThrowable(e, "Rendering item")
        val cat = report.addCategory("Item being rendered")
        cat.setDetail("Item Type") { stack.item.toString() }
        cat.setDetail("Registry Name") { ForgeRegistries.ITEMS.getKey(stack.item).toString() }
        cat.setDetail("Item Damage") { stack.damageValue.toString() }
        cat.setDetail("Item NBT") { stack.tag.toString() }
        cat.setDetail("Item Foil") { stack.hasFoil().toString() }
        throw ReportedException(report)
      }

      poseStack.popPose()

      RenderSystem.enableBlend()
    }
  }
}
