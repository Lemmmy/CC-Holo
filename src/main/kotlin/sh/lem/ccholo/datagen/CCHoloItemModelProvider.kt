package sh.lem.ccholo.datagen

import net.minecraft.data.PackOutput
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper
import sh.lem.ccholo.CCHolo

class CCHoloItemModelProvider(
  out: PackOutput,
  existingFileHelper: ExistingFileHelper
) : ItemModelProvider(
  out,
  CCHolo.MOD_ID,
  existingFileHelper
) {
  override fun registerModels() {
    withExistingParent("hologram", modLoc("block/hologram"))
  }
}
