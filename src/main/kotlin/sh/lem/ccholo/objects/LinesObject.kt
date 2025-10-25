package sh.lem.ccholo.objects

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.util.assertDoubleBetween
import sh.lem.ccholo.util.assertIntBetween

interface LinesObject {
  enum class Join { MITER, BEVEL, ROUND }
  enum class Cap { BUTT, SQUARE, ROUND }

  data class ExtrudedVertex(val left: Vec2, val right: Vec2) // offsets at a poly point

  var join: Join
  var cap: Cap
  var miterLimit: Float
  var roundSegmentsMin: Int
  var roundSegmentsMax: Int

  /**
   * function():string -- Get the join type for this object. Either "miter", "bevel", or "round".
   */
  @LuaFunction
  fun getJoin(): MethodResult = MethodResult.of(join.name)

  /**
   * function(string) -- Set the join type for this object. Must be "miter", "bevel", or "round".
   */
  @LuaFunction
  fun setJoin(args: IArguments) {
    this.join = args.getEnum(0, Join::class.java)
  }

  /**
   * function():string -- Get the cap type for this object. Either "butt", "square", or "round".
   */
  @LuaFunction
  fun getCap(): MethodResult = MethodResult.of(cap.name)

  /**
   * function(string) -- Set the cap type for this object. Must be "butt", "square", or "round".
   */
  @LuaFunction
  fun setCap(args: IArguments) {
    this.cap = args.getEnum(0, Cap::class.java)
  }

  /**
   * function():number -- Get the miter limit for this object.
   */
  @LuaFunction
  fun getMiterLimit(): MethodResult = MethodResult.of(miterLimit) // wrap in MethodResult to avoid signature conflict

  /**
   * function(number) -- Set the miter limit for this object. This is the maximum allowed ratio between the miter length
   * and the line half-width. Must be between 1.0 and 10.0.
   */
  @LuaFunction
  fun setMiterLimit(args: IArguments) {
    this.miterLimit = args.assertDoubleBetween(0, 1.0, 10.0, "Miter limit out of range (%s)").toFloat()
  }

  /**
   * function():number -- Get the minimum segment roundness for circular joins/caps on this line.
   */
  @LuaFunction
  fun getRoundSegmentsMin(): MethodResult = MethodResult.of(roundSegmentsMin)

  /**
   * function(number) -- Set the minimum segment roundness for circular joins/caps on this line. Used to control the
   * smoothness of arcs at small angles. Must be an integer between 1 and 8.
   */
  @LuaFunction
  fun setRoundSegmentsMin(args: IArguments) {
    this.roundSegmentsMin = args.assertIntBetween(0, 1, 8, "Round segments min out of range (%s)")
  }

  /**
   * function():number -- Get the maximum segment roundness for circular joins/caps on this line.
   */
  @LuaFunction
  fun getRoundSegmentsMax(): MethodResult = MethodResult.of(roundSegmentsMax)

  /**
   * function(number) -- Set the maximum segment roundness for circular joins/caps on this line. Used to control the
   * smoothness of 180° arcs. Must be an integer between 8 and 64.
   */
  @LuaFunction
  fun setRoundSegmentsMax(args: IArguments) {
    this.roundSegmentsMax = args.assertIntBetween(0, 8, 64, "Round segments max out of range (%s)")
  }

  companion object {
    val DEFAULT_JOIN = Join.MITER
    val DEFAULT_CAP = Cap.BUTT
    const val DEFAULT_MITER_LIMIT = 4f
    const val DEFAULT_ROUND_SEGMENTS_MIN = 4
    const val DEFAULT_ROUND_SEGMENTS_MAX = 16
  }
}
