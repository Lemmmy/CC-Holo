package sh.lem.ccholo.datagen

import net.minecraft.data.PackOutput
import net.minecraftforge.common.data.LanguageProvider
import sh.lem.ccholo.CCHolo.Blocks
import sh.lem.ccholo.CCHolo.MOD_ID

class CCHoloLangProvider(out: PackOutput) : LanguageProvider(
  out,
  MOD_ID,
  "en_us",
) {
  override fun addTranslations() {
    addBlock(Blocks.HOLOGRAM_BLOCK, "Hologram")
    add("gui.${MOD_ID}.canvas.capturing.title", "Press %s to exit")
    add("gui.${MOD_ID}.canvas.capturing.title_hidden_mouse", "Mouse hidden. Press %s to exit")
    add("key.${MOD_ID}.capture_close", "Press %s to exit")
  }
}
