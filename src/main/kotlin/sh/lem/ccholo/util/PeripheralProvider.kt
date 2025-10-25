package sh.lem.ccholo.util

import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.CapabilityToken
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent

class PeripheralProvider<O : BlockEntity>(
  private val blockEntity: O,
  private val factory: (O) -> IPeripheral,
) : ICapabilityProvider {
  private var peripheral: LazyOptional<IPeripheral>? = null

  private fun invalidate() {
    peripheral?.invalidate()
    peripheral = null
  }

  override fun <T> getCapability(
    capability: Capability<T>,
    direction: Direction?
  ): LazyOptional<T> {
    if (capability != CAPABILITY_PERIPHERAL) return LazyOptional.empty()
    if (blockEntity.isRemoved) return LazyOptional.empty()

    val p = peripheral ?: LazyOptional.of { factory(blockEntity) }
      .also { peripheral = it }
    return p.cast()
  }

  companion object {
    val CAPABILITY_PERIPHERAL = CapabilityManager.get<IPeripheral>(object : CapabilityToken<IPeripheral>() {})

    fun <O : BlockEntity>attach(
      event: AttachCapabilitiesEvent<BlockEntity>,
      blockEntity: O,
      peripheralId: ResourceLocation,
      factory: (O) -> IPeripheral,
    ) {
      val provider = PeripheralProvider(blockEntity, factory)
      event.addCapability(peripheralId, provider)
      event.addListener(provider::invalidate)
    }
  }
}
