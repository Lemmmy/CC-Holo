package sh.lem.ccholo.util

import net.minecraft.core.Direction
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

fun FriendlyByteBuf.readVec2(): Vec2 =
  Vec2(readFloat(), readFloat())

fun FriendlyByteBuf.writeVec2(vec: Vec2) {
  writeFloat(vec.x)
  writeFloat(vec.y)
}

fun FriendlyByteBuf.readVec3(): Vec3 =
  Vec3(readDouble(), readDouble(), readDouble())

fun FriendlyByteBuf.writeVec3(vec: Vec3) {
  writeDouble(vec.x)
  writeDouble(vec.y)
  writeDouble(vec.z)
}

fun FriendlyByteBuf.readOptVec3(): Vec3? =
  if (readBoolean()) readVec3() else null

fun FriendlyByteBuf.writeOptVec3(vec: Vec3?) {
  if (vec == null) {
    writeBoolean(false)
  } else {
    writeBoolean(true)
    writeVec3(vec)
  }
}

fun FriendlyByteBuf.readOptResourceLocation(): ResourceLocation? =
  if (readBoolean()) readResourceLocation() else null

fun FriendlyByteBuf.writeOptResourceLocation(loc: ResourceLocation?) {
  if (loc == null) {
    writeBoolean(false)
  } else {
    writeBoolean(true)
    writeResourceLocation(loc)
  }
}

fun FriendlyByteBuf.readOptBlockHitResult(): BlockHitResult? {
  if (!readBoolean()) return null
  
  val location = readVec3()
  val direction = readEnum(Direction::class.java)
  val blockPos = readBlockPos()
  val inside = readBoolean()
  
  return BlockHitResult(location, direction, blockPos, inside)
}

fun FriendlyByteBuf.writeOptBlockHitResult(hit: BlockHitResult?) {
  if (hit == null) {
    writeBoolean(false)
  } else {
    writeBoolean(true)
    writeVec3(hit.location)
    writeEnum(hit.direction)
    writeBlockPos(hit.blockPos)
    writeBoolean(hit.isInside)
  }
}

data class EntityHitInfo(
  val entityId: Int,
  val location: Vec3
)

fun FriendlyByteBuf.readOptEntityHitInfo(): EntityHitInfo? {
  if (!readBoolean()) return null
  
  val entityId = readInt()
  val location = readVec3()
  
  return EntityHitInfo(entityId, location)
}

fun FriendlyByteBuf.writeOptEntityHitInfo(hit: EntityHitInfo?) {
  if (hit == null) {
    writeBoolean(false)
  } else {
    writeBoolean(true)
    writeInt(hit.entityId)
    writeVec3(hit.location)
  }
}

fun EntityHitResult.toEntityHitInfo() = EntityHitInfo(entity.id, location)
