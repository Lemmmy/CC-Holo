package sh.lem.ccholo.objects.renderers.object2d

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.Tesselator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture.FULL_BRIGHT
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import sh.lem.ccholo.canvas.CanvasRootClient
import sh.lem.ccholo.objects.HorizontallyAlignable
import sh.lem.ccholo.objects.SplitTabulatedTextLines
import sh.lem.ccholo.objects.TextObject.Companion.EMPTY_LINES
import sh.lem.ccholo.objects.TextObject.Companion.TAB_WIDTH
import sh.lem.ccholo.objects.object2d.Text2d
import sh.lem.ccholo.objects.renderers.BaseObjectRenderer
import kotlin.math.round

@SideOnly(Side.CLIENT)
object Text2dRenderer: BaseObjectRenderer<Text2d> {
  private val mc by lazy { Minecraft.getInstance() }
  private val font by mc::font

  private val splitPattern = Regex("\r\n|\n|\r")
  private val tabPattern = Regex("\t")

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
      // We use 0xRRGGBBAA, but the font renderer expects 0xAARRGGBB, so we rotate the bits
      val colour = Integer.rotateRight(colour, 8)

      val plaintextLines = plaintextLines
      val componentLines = componentLines
      val componentLineWidths = componentLineWidths
      if (plaintextLines.lines.isEmpty() && componentLines == null) return
      if (componentLines != null && componentLineWidths.size != componentLines.size) return

      BaseObjectRenderer.setupFlat()

      val poseStack = gg.pose()
      poseStack.pushPose()
      poseStack.translate(position.x.toDouble(), position.y.toDouble(), 0.0)
      poseStack.scale(scale, scale, 1.0f)

      val builder = Tesselator.getInstance().builder
      val immediate = MultiBufferSource.immediate(builder)
      val pose = poseStack.last().pose()

      var y = 0.0f
      if (componentLines != null) {
        // Component rendering - supports full Minecraft text JSON
        componentLines.forEachIndexed { i, line ->
          val lineWidth = componentLineWidths[i]
          val startX = when (horizontalAlignment) {
            HorizontallyAlignable.Alignment.LEFT -> 0
            HorizontallyAlignable.Alignment.CENTER -> round(-(lineWidth / 2f))
            HorizontallyAlignable.Alignment.RIGHT -> -lineWidth
          }.toFloat()

          font.drawInBatch(
            line, startX, y,
            colour, dropShadow,
            pose, immediate, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT
          )

          y += lineHeight.toInt()
        }
      } else {
        // Plaintext rendering - supports newlines and tab stops
        plaintextLines.lines.forEach { line ->
          val startX = when (horizontalAlignment) {
            HorizontallyAlignable.Alignment.LEFT -> 0
            HorizontallyAlignable.Alignment.CENTER -> round(-(line.width / 2f))
            HorizontallyAlignable.Alignment.RIGHT -> -line.width
          }.toFloat()

          for (part in line.parts) {
            font.drawInBatch(
              part.text, startX + part.x, y,
              colour, dropShadow,
              pose, immediate, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT
            )
          }

          y += lineHeight.toInt()
        }
      }

      immediate.endBatch()

      RenderSystem.enableBlend()

      poseStack.popPose()
    }
  }

  internal fun initialiseTextObject(obj: Text2d) {
    with (obj) {
      plaintextLines = splitText(plaintext)
      componentLines = component?.let {
        // Account for the scale of the text when applying the max width, so the value stays relative to the absolute
        // positioning of the text object (not any subsequent parent frames, though)
        splitComponent(it, (maxWidth / scale).toInt())
      }?.also { lines ->
        componentLineWidths = lines.map { c -> font.width(c) }.toIntArray()
      }
    }
  }

  private fun splitText(text: String?): SplitTabulatedTextLines {
    if (text == null) return EMPTY_LINES

    val lines = splitPattern.split(text)

    val splitLines: MutableList<MutableList<String>> = MutableList(lines.size) { mutableListOf() }
    val format = StringBuilder()
    for ((i, line) in lines.withIndex()) {
      val tabs = line.split(tabPattern).toMutableList()
      splitLines[i] = tabs

      for ((j, tab) in tabs.withIndex()) {
        format.append(tab)
        appendFormat(format, format.toString().also { tabs[j] = it })
      }
    }

    val finalLines = mutableListOf<SplitTabulatedTextLines.Line>()
    for (fullLine in splitLines) {
      var x = 0
      val lineParts = mutableListOf<SplitTabulatedTextLines.TextPart>()

      for (tabSection in fullLine) {
        val width = font.width(tabSection)
        lineParts.add(SplitTabulatedTextLines.TextPart(tabSection, width, x))
        x += width
        x = x / TAB_WIDTH * TAB_WIDTH + TAB_WIDTH // Round the X coordinate to the next tab stop.
      }

      finalLines.add(SplitTabulatedTextLines.Line(lineParts, lineParts.sumOf { it.width }))
    }

    return SplitTabulatedTextLines(finalLines)
  }

  private fun appendFormat(builder: StringBuilder, text: String) {
    builder.setLength(0)

    val l = text.length
    var i = -1

    while (text.indexOf('\u00a7', i + 1).also { i = it } != -1) {
      if (i < l - 1) {
        val c0 = text[i + 1]

        if (isFormatColor(c0)) {
          builder.setLength(0)
          builder.append('\u00a7').append(c0)
        } else if (isFormatSpecial(c0)) {
          builder.append('\u00a7').append(c0)
        }
      }
    }
  }

  private fun isFormatColor(colorChar: Char) =
    (colorChar in '0'..'9' || colorChar >= 'a') && colorChar <= 'f' || colorChar in 'A'..'F'

  /**
   * Checks if the char code is O-K...lLrRk-o... used to set special formatting.
   */
  private fun isFormatSpecial(formatChar: Char) =
    (formatChar in 'k'..'o' || formatChar >= 'K') && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R'

  private fun splitComponent(text: Component, maxWidth: Int): List<FormattedCharSequence>
    = font.split(text, maxWidth)
}
