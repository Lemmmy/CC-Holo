package sh.lem.ccholo.objects.renderers.object2d

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.RemovalNotification
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.cache2k.Cache2kBuilder
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.CCHolo.MOD_ID
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.object2d.Image2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer
import java.io.Closeable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SideOnly(Side.CLIENT)
object Image2dRenderer: BaseObjectRenderer<Image2d> {
  private val mc by lazy { Minecraft.getInstance() }

  private val imageCache = object : Cache2kBuilder<String, CachedImage>() {}
    .entryCapacity(128)
    .idleScanTime(Duration.ofMinutes(2))
    .loader { url -> CachedImage(url).also { it.load() } }
    .build()

  private val imageCache2 = CacheBuilder.newBuilder()
    .expireAfterAccess(Duration.ofSeconds(5))
    .maximumSize(128)
    .removalListener { notif: RemovalNotification<String, CachedImage> ->
      notif.value?.close()
    }
    .build(CacheLoader.from { url: String ->
      CachedImage(url).also { it.load() }
    })

  private val httpClient = HttpClient.newHttpClient()
  private val imageFetcher = Executors.newFixedThreadPool(4, BasicThreadFactory.Builder()
    .namingPattern("CC-Holo-Image-Fetch-%d")
    .daemon(true)
    .build())
  private val textureId = AtomicInteger(0)

  override fun draw(
    obj: Image2d,
    root: CanvasRootClient,
    gg: GuiGraphics,
    buf: MultiBufferSource
  ) {
    with (obj) {
      BaseObjectRenderer.setupFlat()

      val minX = position.x; val minY = position.y
      val maxX = minX + imageWidth; val maxY = minY + imageHeight

      val poseStack = gg.pose()
      val builder = Tesselator.getInstance().builder
      val pose = poseStack.last().pose()

      val cachedImage = imageCache.get(imageUrl)
      val cachedTex = cachedImage?.texture

      if (cachedImage?.loaded == true && cachedTex != null) {
        // Loaded image
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader)
        RenderSystem.setShaderTexture(0, cachedTex.id)

        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX)

        builder.vertex(pose, minX, minY, 0.0f).color(r, g, b, a).uv(0f, 0f).endVertex()
        builder.vertex(pose, minX, maxY, 0.0f).color(r, g, b, a).uv(0f, 1f).endVertex()
        builder.vertex(pose, maxX, maxY, 0.0f).color(r, g, b, a).uv(1f, 1f).endVertex()

        builder.vertex(pose, minX, minY, 0.0f).color(r, g, b, a).uv(0f, 0f).endVertex()
        builder.vertex(pose, maxX, maxY, 0.0f).color(r, g, b, a).uv(1f, 1f).endVertex()
        builder.vertex(pose, maxX, minY, 0.0f).color(r, g, b, a).uv(1f, 0f).endVertex()
      } else {
        // Fallback rectangle
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)

        val r = fallbackColour shr 24 and 0xFF
        val g = fallbackColour shr 16 and 0xFF
        val b = fallbackColour shr 8 and 0xFF
        val a = fallbackColour and 0xFF

        builder.vertex(pose, minX, minY, 0.0f).color(r, g, b, a).endVertex()
        builder.vertex(pose, minX, maxY, 0.0f).color(r, g, b, a).endVertex()
        builder.vertex(pose, maxX, maxY, 0.0f).color(r, g, b, a).endVertex()

        builder.vertex(pose, minX, minY, 0.0f).color(r, g, b, a).endVertex()
        builder.vertex(pose, maxX, maxY, 0.0f).color(r, g, b, a).endVertex()
        builder.vertex(pose, maxX, minY, 0.0f).color(r, g, b, a).endVertex()
      }

      BufferUploader.drawWithShader(builder.end())
    }
  }

  class CachedImage(val url: String): Closeable {
    var texture: AbstractTexture? = null
    var loaded = false

    fun load() {
      imageFetcher.submit {
        loadImpl()
      }
    }

    private fun loadImpl() {
      CCHolo.log.debug("Loading image $url")

      try {
        val res = httpClient.send(
          HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "CC-Holo/${CCHolo.MOD_VERSION}")
            .header("Accept", "image/png;q=1.0, image/jpeg;q=0.8, image/*;q=0.5")
            .timeout(Duration.ofSeconds(5))
            .build(), HttpResponse.BodyHandlers.ofInputStream()
        )

        if (res.statusCode() != 200) {
          CCHolo.log.warn("Failed to load image $url: ${res.statusCode()}")
          return
        }

        val image = NativeImage.read(res.body())
        if (image.width > Image2d.MAX_IMAGE_WIDTH || image.height > Image2d.MAX_IMAGE_HEIGHT) {
          CCHolo.log.warn("Image $url is too large: ${image.width}x${image.height}")
          return
        }

        val id = ResourceLocation.tryBuild(MOD_ID, "image_${textureId.getAndIncrement()}")!!
        texture = DynamicTexture(image).also { mc.textureManager.register(id, it) }
        loaded = true
        CCHolo.log.debug("Loaded image $url")
      } catch (e: Exception) {
        CCHolo.log.warn("Failed to load image $url", e)
      } catch (e: VirtualMachineError) {
        CCHolo.log.warn("Failed to load image $url", e)
      } catch (e: LinkageError) {
        CCHolo.log.warn("Failed to load image $url", e)
      }
    }

    override fun close() {
      CCHolo.log.debug("Disposing image $url")
      texture?.close()
    }
  }
}
