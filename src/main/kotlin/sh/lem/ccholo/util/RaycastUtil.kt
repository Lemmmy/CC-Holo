package sh.lem.ccholo.util

import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import sh.lem.ccholo.canvas.DEFAULT_RAYCAST_RANGE
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.toVec3
import kotlin.math.tan

object RaycastUtil {
  data class RaycastResult(
    val blockHit: BlockHitResult?,
    val entityHit: EntityHitResult?
  )

  /**
   * Performs a raycast from the given screen coordinates.
   * 
   * @param screenX The x coordinate on the screen (0 to screen width)
   * @param screenY The y coordinate on the screen (0 to screen height)
   * @param screenWidth The width of the screen
   * @param screenHeight The height of the screen
   * @param reach The maximum distance to raycast (default: 20 blocks)
   * @return A RaycastResult containing both block and entity hit results
   */
  fun raycastFromScreen(
    screenX: Double,
    screenY: Double,
    screenWidth: Int,
    screenHeight: Int,
    reach: Double = DEFAULT_RAYCAST_RANGE
  ): RaycastResult {
    val mc = Minecraft.getInstance()
    val camera = mc.gameRenderer.mainCamera
    val entity = mc.cameraEntity ?: return RaycastResult(null, null)
    
    // Use GameRenderer's getFov method directly (made public via access transformer)
    val fov = mc.gameRenderer.getFov(camera, 1.0f, true)
    
    // Get the ray direction from screen coordinates
    val rayDir = getRayDirectionFromScreen(
      camera,
      screenX,
      screenY,
      screenWidth,
      screenHeight,
      fov
    )
    
    val eyePos = camera.position
    val reachVec = eyePos.add(rayDir.scale(reach))
    
    // Perform block raycast
    val blockHit = mc.level?.clip(
      ClipContext(
        eyePos,
        reachVec,
        ClipContext.Block.OUTLINE,
        ClipContext.Fluid.NONE,
        entity
      )
    )
    
    // Perform entity raycast
    val entityHit = raycastEntity(entity, eyePos, reachVec)
    
    return RaycastResult(blockHit, entityHit)
  }

  /**
   * Converts screen coordinates to a ray direction in world space.
   */
  private fun getRayDirectionFromScreen(
    camera: Camera,
    screenX: Double,
    screenY: Double,
    screenWidth: Int,
    screenHeight: Int,
    fov: Double
  ): Vec3 {
    // Convert screen coordinates to NDC (Normalized Device Coordinates)
    // NDC range: -1 to 1 for both x and y
    val ndcX = (2.0 * screenX / screenWidth) - 1.0
    val ndcY = 1.0 - (2.0 * screenY / screenHeight) // Flip Y axis
    
    // Calculate the aspect ratio
    val aspectRatio = screenWidth.toDouble() / screenHeight.toDouble()
    
    // Convert FOV to radians and calculate half angles
    val fovRad = Math.toRadians(fov)
    val halfHeight = tan(fovRad / 2.0)
    val halfWidth = halfHeight * aspectRatio
    
    // Calculate the ray direction in camera space
    val camSpaceX = ndcX * halfWidth
    val camSpaceY = ndcY * halfHeight
    val camSpaceZ = 1.0 // Forward direction
    
    // Get camera rotation vectors
    val forward = camera.lookVector.toVec3()
    val up = camera.upVector.toVec3()
    val right = forward.cross(up).normalize()
    val actualUp = right.cross(forward).normalize()
    
    // Transform from camera space to world space
    val worldDir = forward.scale(camSpaceZ)
      .add(right.scale(camSpaceX))
      .add(actualUp.scale(camSpaceY))
      .normalize()
    
    return worldDir
  }

  /**
   * Performs entity raycasting.
   */
  private fun raycastEntity(
    entity: Entity,
    start: Vec3,
    end: Vec3
  ): EntityHitResult? {
    val level = entity.level()
    val aabb = entity.boundingBox.expandTowards(end.subtract(start))
    
    return ProjectileUtil.getEntityHitResult(
      level,
      entity,
      start,
      end,
      aabb,
      { target -> !target.isSpectator && target.isPickable }
    )
  }
}
