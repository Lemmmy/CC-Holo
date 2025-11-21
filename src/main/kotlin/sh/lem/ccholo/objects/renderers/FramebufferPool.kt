package sh.lem.ccholo.objects.renderers

import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexSorting
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.joml.Matrix4f
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.canvas.CanvasRoot.Companion.MAX_FRAME_HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.MAX_FRAME_WIDTH
import java.util.*
import kotlin.math.min

/**
 * Global pool of framebuffers for canvas rendering. Reuses framebuffers with the same dimensions to reduce allocation
 * overhead. Framebuffers are disposed after 30 seconds of inactivity to prevent VRAM leaks.
 */
@SideOnly(Side.CLIENT)
object FramebufferPool {
  private const val MAX_POOL_SIZE = 256
  private const val DISPOSAL_DELAY_FRAMES = 1800 // 30 seconds at 60fps

  private const val MAX_FRAMEBUFFER_WIDTH = 8192 // should match CanvasRoot.MAX_FRAME_WIDTH (13 bits)
  private const val MAX_FRAMEBUFFER_HEIGHT = 8192 // should match CanvasRoot.MAX_FRAME_HEIGHT (13 bits)
  
  // Pack width and height into a single long key: 
  // - Bits 0-12: height (13 bits, max 8192)
  // - Bits 13-25: width (13 bits, max 8192)
  // - Bits 26-63: nesting level (38 bits, supports up to 274 billion nested framebuffers per dimension)
  // Key format: (nestingLevel shl 26) | (width shl 13) | height
  private val pool = Long2ObjectOpenHashMap<PooledFramebuffer>()
  private val usedThisFrame = LongOpenHashSet(MAX_POOL_SIZE)
  private val activeFramebuffers = LongOpenHashSet(MAX_POOL_SIZE)
  private var frameCounter = 0L
  
  // Stack tracking for nested framebuffers
  data class FramebufferStackEntry(
    val key: Long,
    val previousFramebuffer: Int?,
    val savedProjectionMatrix: Matrix4f,
    val savedVertexSorting: VertexSorting
  )
  
  private val framebufferStack = LinkedList<FramebufferStackEntry>()
  
  data class PooledFramebuffer(
    val framebuffer: TextureTarget,
    var lastUsedFrame: Long,
    var savedProjectionMatrix: Matrix4f,
    var savedVertexSorting: VertexSorting
  )
  
  /**
   * Get or create a framebuffer with the specified dimensions. Returns null if the pool is full, the dimensions are not
   * already in the pool, or the framebuffer is currently active (being rendered to). Dimensions will be constrained to
   * MAX_FRAME_WIDTH, MAX_FRAME_HEIGHT.
   * 
   * Captures the current framebuffer binding to restore it later when the framebuffer is released.
   */
  fun acquire(width: Int, height: Int): TextureTarget? {
    val w = width.coerceIn(1, min(MAX_FRAME_WIDTH, MAX_FRAMEBUFFER_WIDTH))
    val h = height.coerceIn(1, min(MAX_FRAME_HEIGHT, MAX_FRAMEBUFFER_HEIGHT))
    var key = packDimensions(w, h)

    // Check if this dimension is already in the stack (nesting). Since framebuffers are always nested in LIFO order, we
    // can traverse backwards and stop at the first match to get the current nesting level
    var nestingLevel = 0
    for (entry in framebufferStack.reversed()) {
      val entryKey = entry.key and 0x3FFFFFFL // Mask out nesting suffix (top 38 bits)
      if (entryKey == key) {
        nestingLevel++
        break
      }
    }
    
    // If nesting, append incrementing digit to key
    if (nestingLevel > 0) {
      key = key or ((nestingLevel.toLong() and 0x3FFFFFFFFL) shl 26)
    }

    // Get current framebuffer if we're in the stack, otherwise leave it null so we can bind back to the main
    // framebuffer at the end of rendering
    val previousFramebuffer = if (nestingLevel > 0) GlStateManager.getBoundFramebuffer() else null
    val savedProjectionMatrix = Matrix4f(RenderSystem.getProjectionMatrix())
    val savedVertexSorting = RenderSystem.getVertexSorting()
    
    // Don't allow acquiring a framebuffer that's currently being rendered to
    if (activeFramebuffers.contains(key)) {
      CCHolo.log.warn("Refusing to acquire framebuffer $w x $h - already active (nested frame?)")
      return null
    }
    
    usedThisFrame.add(key)
    
    val pooled = pool.get(key)
    if (pooled != null) {
      pooled.lastUsedFrame = frameCounter
      // Store the saved projection matrix state in the pooled framebuffer
      pooled.savedProjectionMatrix = savedProjectionMatrix
      pooled.savedVertexSorting = savedVertexSorting
      activeFramebuffers.add(key)
      framebufferStack.addLast(FramebufferStackEntry(key, previousFramebuffer, savedProjectionMatrix, savedVertexSorting))
      return pooled.framebuffer
    }
    
    // Check if we've hit the pool size limit
    if (pool.size >= MAX_POOL_SIZE) {
      CCHolo.log.warn("Framebuffer pool is full (${pool.size}/$MAX_POOL_SIZE), refusing to create $w x $h framebuffer")
      return null
    }
    
    // Create new framebuffer
    CCHolo.log.debug("Creating framebuffer with size $w x $h (pool size: ${pool.size + 1}/$MAX_POOL_SIZE)")
    val framebuffer = TextureTarget(w, h, true, true)
    pool.put(key, PooledFramebuffer(framebuffer, frameCounter, savedProjectionMatrix, savedVertexSorting))
    activeFramebuffers.add(key)
    framebufferStack.addLast(FramebufferStackEntry(key, previousFramebuffer, savedProjectionMatrix, savedVertexSorting))
    
    return framebuffer
  }

  fun release(): Pair<Int?, Pair<Matrix4f, VertexSorting>?> {
    if (framebufferStack.isEmpty()) {
      return Pair(null, null)
    }
    
    val entry = framebufferStack.removeLast()
    activeFramebuffers.remove(entry.key)
    
    val projectionState = entry.savedProjectionMatrix to entry.savedVertexSorting

    return Pair(entry.previousFramebuffer, projectionState)
  }

  /**
   * Called at the start of each frame to track which framebuffers are in use.
   */
  fun beginFrame() {
    frameCounter++
    usedThisFrame.clear()
  }
  
  /**
   * Called at the end of each frame to clean up unused framebuffers.
   */
  fun endFrame() {
    val iterator = pool.long2ObjectEntrySet().iterator()
    
    while (iterator.hasNext()) {
      val entry = iterator.next()
      val key = entry.longKey
      val pooled = entry.value
      
      // Dispose framebuffers that haven't been used recently
      if (!usedThisFrame.contains(key) && frameCounter - pooled.lastUsedFrame > DISPOSAL_DELAY_FRAMES) {
        val (width, height) = unpackDimensions(key)
        CCHolo.log.debug("Disposing unused framebuffer $width x $height (last used ${frameCounter - pooled.lastUsedFrame} frames ago)")
        pooled.framebuffer.destroyBuffers()
        iterator.remove()
      }
    }
  }
  
  /**
   * Dispose all framebuffers in the pool.
   */
  fun clear() {
    pool.values.forEach { it.framebuffer.destroyBuffers() }
    pool.clear()
    usedThisFrame.clear()
    activeFramebuffers.clear()
    framebufferStack.clear()
  }

  private fun packDimensions(width: Int, height: Int): Long {
    return (width.toLong() and 0x1FFFL shl 13) or (height.toLong() and 0x1FFFL)
  }
  
  private fun unpackDimensions(key: Long): Pair<Int, Int> {
    val width = ((key shr 13) and 0x1FFFL).toInt()
    val height = (key and 0x1FFFL).toInt()
    return width to height
  }
}
