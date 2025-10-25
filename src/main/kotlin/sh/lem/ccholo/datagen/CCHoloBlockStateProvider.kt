package sh.lem.ccholo.datagen

import net.minecraft.data.PackOutput
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.common.data.ExistingFileHelper
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.CCHolo.Blocks

class CCHoloBlockStateProvider(
  out: PackOutput,
  existingFileHelper: ExistingFileHelper
) : BlockStateProvider(
  out,
  CCHolo.MOD_ID,
  existingFileHelper
) {
  override fun registerStatesAndModels() {
    simpleBlock(Blocks.HOLOGRAM_BLOCK.get())
  }
}
