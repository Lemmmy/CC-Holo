/*
 * The following code is ported from CC: Tweaked 1.116.1's StringUtil.java, which is licensed under MPL-2.0.
 * The full terms of the license can be found in `LICENSES/MPL-2.0.txt`.
 *
 * This code is vendored here to reduce dependency on CC: Tweaked's core API, but more importantly, to allow
 * compatibility with CC: Tweaked 1.113.1.
 *
 * Modifications:
 * - Ported to kotlin
 * - Only included methods required by CC-Holo
 * - Allowed newlines in pastes, and convert CRLF to LF
 */
package sh.lem.ccholo.util

import java.nio.ByteBuffer

object CCStringUtil {
  const val MAX_PASTE_LENGTH = 512

  /**
   * Convert a Unicode character to a terminal one.
   *
   * @param chr The Unicode character.
   * @return The terminal character. This is either in the range [0, 255] (if a valid character) or `-1` if
   * it cannot be mapped to CC's charset.
   * @author SquidDev
   * @see <https://github.com/cc-tweaked/CC-Tweaked/blob/b9ed669/projects/core/src/main/java/dan200/computercraft/core/util/StringUtil.java#L24>
   */
  fun unicodeToTerminal(chr: Int): Int {
    // ASCII and latin1 map to themselves
    if (chr == 0 || chr == '\t'.code || chr == '\n'.code || chr == '\r'.code || (chr >= ' '.code && chr <= '~'.code) || (chr in 160..255)) {
      return chr
    }

    // Teletext block mosaics are *fairly* contiguous.
    if (chr in 0x1FB00..0x1FB13) return chr + (129 - 0x1fb00)
    if (chr in 0x1FB14..0x1FB1D) return chr + (150 - 0x1fb14)

    // Everything else is just a manual lookup. For now, we just use a big switch statement, which we spin into a
    // separate function to hopefully avoid inlining it here.
    return unicodeToCraftOsFallback(chr)
  }

  /**
   * @author SquidDev
   * @see <https://github.com/cc-tweaked/CC-Tweaked/blob/b9ed669/projects/core/src/main/java/dan200/computercraft/core/util/StringUtil.java#L39C1-L71C6>
   */
  private fun unicodeToCraftOsFallback(c: Int): Int = when (c) {
    0x263A -> 1
    0x263B -> 2
    0x2665 -> 3
    0x2666 -> 4
    0x2663 -> 5
    0x2660 -> 6
    0x2022 -> 7
    0x25D8 -> 8
    0x2642 -> 11
    0x2640 -> 12
    0x266A -> 14
    0x266B -> 15
    0x25BA -> 16
    0x25C4 -> 17
    0x2195 -> 18
    0x203C -> 19
    0x25AC -> 22
    0x21A8 -> 23
    0x2191 -> 24
    0x2193 -> 25
    0x2192 -> 26
    0x2190 -> 27
    0x221F -> 28
    0x2194 -> 29
    0x25B2 -> 30
    0x25BC -> 31
    0x1FB99 -> 127
    0x258C -> 149
    else -> -1
  }

  /**
   * Check if a character is capable of being input and passed to a `char` event.
   *
   * @param chr The character to check.
   * @return Whether this character can be typed.
   * @author SquidDev
   * @see <https://github.com/cc-tweaked/CC-Tweaked/blob/b9ed669/projects/core/src/main/java/dan200/computercraft/core/util/StringUtil.java#L84C1-L93C6>
   */
  fun isTypableChar(chr: Int): Boolean = chr in 0..255 && chr != 0

  /**
   * Convert a Java string to a Lua one (using the terminal charset), suitable for pasting into a computer.
   *
   * This removes special characters and strips to the first line of text.
   *
   * @param clipboard The text from the clipboard.
   * @return The encoded clipboard text.
   * @author SquidDev
   * @see <https://github.com/cc-tweaked/CC-Tweaked/blob/b9ed669/projects/core/src/main/java/dan200/computercraft/core/util/StringUtil.java#L110C1-L131C6>
   */
  fun getClipboardString(clipboard: String): ByteBuffer {
    val output = ByteArray(MAX_PASTE_LENGTH.coerceAtMost(clipboard.length))
    var idx = 0

    val iterator = clipboard.codePoints().iterator()
    var prevWasCR = false

    while (iterator.hasNext() && idx < output.size) {
      val nextCodePoint = iterator.next()
      val chr = unicodeToTerminal(nextCodePoint)
      if (chr < 0) continue // Strip out unconvertible characters

      // If the previous character was a CR, handle CRLF normalization.
      if (prevWasCR) {
        if (chr == '\n'.code) {
          // Convert CRLF to single LF.
          if (!isTypableChar('\n'.code)) break
          output[idx++] = '\n'.code.toByte()
          prevWasCR = false
          continue
        } else {
          // Standalone CR. Emit CR if typable and there is space, then fall through to handle current char.
          if (isTypableChar('\r'.code)) {
            output[idx++] = '\r'.code.toByte()
          }
          prevWasCR = false
          // Note: do not continue here so current chr is also processed below.
        }
      }

      if (chr == '\r'.code) {
        // Defer emitting until we know if the next char is LF.
        prevWasCR = true
        continue
      }

      if (!isTypableChar(chr)) break // Stop at untypable ones.
      if (idx >= output.size) break

      output[idx++] = chr.toByte()
    }

    // If input ended with a trailing CR, emit it (not part of CRLF).
    if (prevWasCR && idx < output.size && isTypableChar('\r'.code)) {
      output[idx++] = '\r'.code.toByte()
    }

    return ByteBuffer.wrap(output, 0, idx).asReadOnlyBuffer()
  }
}
