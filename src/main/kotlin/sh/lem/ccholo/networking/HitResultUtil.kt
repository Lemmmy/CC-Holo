package sh.lem.ccholo.networking

import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.registries.ForgeRegistries
import sh.lem.ccholo.util.EntityHitInfo

typealias HitResultTable = Map<String, Any?>

object HitResultUtil {
  /**
   * Creates a Lua table (Map) containing both block and entity hit results.
   * Returns a map with structure: { blockHit = {...}, entityHit = {...} }
   */
  fun createHitResultTable(
    blockHit: BlockHitResult?,
    entityHit: EntityHitInfo?,
    level: Level
  ): HitResultTable {
    return mapOf(
      "blockHit" to blockHit?.let { serializeBlockHit(it, level) },
      "entityHit" to entityHit?.let { serializeEntityHit(it, level) }
    )
  }

  /**
   * Serializes a BlockHitResult to a Lua-friendly map.
   */
  private fun serializeBlockHit(hit: BlockHitResult, level: Level): Map<String, Any> {
    val blockState = level.getBlockState(hit.blockPos)
    val block = blockState.block
    val blockId = ForgeRegistries.BLOCKS.getKey(block).toString()
    
    return mapOf(
      "pos" to mapOf(
        "x" to hit.blockPos.x,
        "y" to hit.blockPos.y,
        "z" to hit.blockPos.z
      ),
      "location" to mapOf(
        "x" to hit.location.x,
        "y" to hit.location.y,
        "z" to hit.location.z
      ),
      "face" to hit.direction.name.lowercase(),
      "block" to blockId,
      "inside" to hit.isInside
    )
  }

  /**
   * Serializes an EntityHitInfo to a Lua-friendly map.
   */
  private fun serializeEntityHit(hit: EntityHitInfo, level: Level): Map<String, Any> {
    val entity = level.getEntity(hit.entityId)
    
    val baseMap = mutableMapOf<String, Any>(
      "id" to hit.entityId,
      "location" to mapOf(
        "x" to hit.location.x,
        "y" to hit.location.y,
        "z" to hit.location.z
      )
    )
    
    // Add entity type and name if entity is found
    if (entity != null) {
      val entityType = ForgeRegistries.ENTITY_TYPES.getKey(entity.type).toString()
      baseMap["type"] = entityType
      
      if (entity.hasCustomName()) {
        baseMap["name"] = entity.customName?.string ?: ""
      } else {
        baseMap["name"] = entity.name.string
      }
      
      // Add position
      baseMap["pos"] = mapOf(
        "x" to entity.x,
        "y" to entity.y,
        "z" to entity.z
      )
    }
    
    return baseMap
  }
}
