package sh.lem.ccholo.datagen

import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import sh.lem.ccholo.CCHolo

@Mod.EventBusSubscriber(modid = CCHolo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object CCHoloDatagen {
  @SubscribeEvent
  fun onGatherData(e: GatherDataEvent) {
    val gen = e.generator
    val existingFileHelper = e.existingFileHelper

    val client = e.includeClient()
    val server = e.includeServer()

    gen.addProvider<BlockStateProvider> (client) { CCHoloBlockStateProvider(it, existingFileHelper) }
    gen.addProvider<ItemModelProvider> (client) { CCHoloItemModelProvider(it, existingFileHelper) }
    gen.addProvider(client, ::CCHoloLangProvider)
  }
}
