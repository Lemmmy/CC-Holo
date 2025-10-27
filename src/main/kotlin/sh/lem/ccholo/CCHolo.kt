package sh.lem.ccholo

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.GameMasterBlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import org.slf4j.LoggerFactory
import sh.lem.ccholo.CCHolo.Items.HOLOGRAM_BLOCK_ITEM
import sh.lem.ccholo.networking.CCHoloPacketHandler
import sh.lem.ccholo.peripheral.HologramBlock
import sh.lem.ccholo.peripheral.HologramBlockEntity
import sh.lem.ccholo.peripheral.HologramPeripheral
import sh.lem.ccholo.util.PeripheralProvider
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(CCHolo.MOD_ID)
object CCHolo {
  const val MOD_ID: String = "ccholo"

  @JvmField
  val log = LoggerFactory.getLogger(MOD_ID)!!

  object Blocks {
    internal val REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID)

    val HOLOGRAM_BLOCK = REGISTRY.register("hologram", ::HologramBlock)
  }

  object BlockEntities {
    internal val REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID)

    val HOLOGRAM_BLOCK_ENTITY = REGISTRY.register("hologram") {
      BlockEntityType.Builder.of(
        ::HologramBlockEntity,
        Blocks.HOLOGRAM_BLOCK.get(),
      ).build(null)
    }
  }

  object Items {
    internal val REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)

    val HOLOGRAM_BLOCK_ITEM = REGISTRY.register("hologram") {
      GameMasterBlockItem(Blocks.HOLOGRAM_BLOCK.get(), Item.Properties().rarity(Rarity.EPIC))
    }
  }

  object Peripherals {
    val HOLOGRAM_PERIPHERAL_ID = ResourceLocation.tryBuild(MOD_ID, "hologram")!!

    @SubscribeEvent
    fun onAttachCapabilities(e: AttachCapabilitiesEvent<BlockEntity>) {
      when (val be = e.`object`) {
        is HologramBlockEntity ->
          PeripheralProvider.attach(e, be, HOLOGRAM_PERIPHERAL_ID, ::HologramPeripheral)
      }
    }
  }

  object CreativeTabs {
    // Add the peripheral to CC's creative tab
    internal val TAB = ResourceLocation.tryBuild("computercraft", "tab")

    fun onCreativeTabBuildContents(event: BuildCreativeModeTabContentsEvent) {
      if (event.tabKey.location() == TAB) {
        event.accept(HOLOGRAM_BLOCK_ITEM)
      }
    }
  }

  init {
    // Registries
    Blocks.REGISTRY.register(MOD_BUS)
    BlockEntities.REGISTRY.register(MOD_BUS)
    Items.REGISTRY.register(MOD_BUS)

    // Event listeners
    MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity::class.java, Peripherals::onAttachCapabilities)
    MOD_BUS.addListener(CreativeTabs::onCreativeTabBuildContents)

    // Networking
    CCHoloPacketHandler.setup()

    DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable { CCHoloClient.init() } }
  }
}
