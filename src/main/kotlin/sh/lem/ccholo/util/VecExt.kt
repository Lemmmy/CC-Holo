package sh.lem.ccholo.util

import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

fun Vec2?.toResult(): MethodResult =
  this?.let { MethodResult.of(it.x, it.y) } ?: MethodResult.of()
fun Vec3?.toResult(): MethodResult =
  this?.let { MethodResult.of(it.x, it.y, it.z) } ?: MethodResult.of()
fun BlockPos?.toResult(): MethodResult =
  this?.let { MethodResult.of(it.x, it.y, it.z) } ?: MethodResult.of()

fun Vec2.perp(): Vec2 = Vec2(-y, x)
