package sh.lem.ccholo.peripheral

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.GameMasterBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor

class HologramBlock : Block(
  Properties.of()
    .mapColor(MapColor.STONE)
    .requiresCorrectToolForDrops()
    .strength(-1.0F, 3600000.0F)
    .noLootTable()
), EntityBlock, GameMasterBlock {
  override fun newBlockEntity(pos: BlockPos, state: BlockState) = HologramBlockEntity(pos, state)

  override fun onRemove(
    state: BlockState,
    level: Level,
    pos: BlockPos,
    newState: BlockState,
    movedByPiston: Boolean
  ) {
    if (state.`is`(newState.block)) return;

    level.getBlockEntity(pos)
      ?.let { it as? HologramBlockEntity }
      ?.onBreak()

    super.onRemove(state, level, pos, newState, movedByPiston)
  }
}
