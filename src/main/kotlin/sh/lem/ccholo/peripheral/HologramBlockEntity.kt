package sh.lem.ccholo.peripheral

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.util.PeripheralProvider.Companion.CAPABILITY_PERIPHERAL

class HologramBlockEntity(
  pos: BlockPos,
  state: BlockState,
) : BlockEntity(
  CCHolo.BlockEntities.HOLOGRAM_BLOCK_ENTITY.get(),
  pos,
  state,
) {
  fun onBreak() {
    getCapability(CAPABILITY_PERIPHERAL).ifPresent { p ->
      (p as? HologramPeripheral)?.onBreak()
    }
  }
}
