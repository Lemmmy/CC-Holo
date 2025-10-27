package sh.lem.ccholo.objects.object2d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec2
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.LinesObject
import sh.lem.ccholo.objects.LinesObject.*
import sh.lem.ccholo.objects.LinesObject.Companion.DEFAULT_CAP
import sh.lem.ccholo.objects.LinesObject.Companion.DEFAULT_JOIN
import sh.lem.ccholo.objects.LinesObject.Companion.DEFAULT_MITER_LIMIT
import sh.lem.ccholo.objects.LinesObject.Companion.DEFAULT_ROUND_SEGMENTS_MAX
import sh.lem.ccholo.objects.LinesObject.Companion.DEFAULT_ROUND_SEGMENTS_MIN
import sh.lem.ccholo.objects.ObjectRegistry.LINES_2D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.util.*
import kotlin.math.*

class Lines2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : Polygon2d(id, parent, canvasRoot, LINES_2D), Scalable, LinesObject {
  override var points = mutableListOf<Vec2>()
  override val vertices: Int
    get() = points.size

  /** Line thickness */
  override var scale by DirtyingProperty(1.0f)

  override var join by DirtyingProperty(DEFAULT_JOIN)
  override var cap by DirtyingProperty(DEFAULT_CAP)
  override var miterLimit by DirtyingProperty(DEFAULT_MITER_LIMIT)
  override var roundSegmentsMin by DirtyingProperty(DEFAULT_ROUND_SEGMENTS_MIN)
  override var roundSegmentsMax by DirtyingProperty(DEFAULT_ROUND_SEGMENTS_MAX)

  data class Triangle(val v0: Vec2, val v1: Vec2, val v2: Vec2)
  var cachedTriangles: List<Triangle>? = null

  override fun readInitial(buf: FriendlyByteBuf) {
    super.readInitial(buf)
    scale = buf.readFloat()
    join = buf.readEnum(Join::class.java)
    cap = buf.readEnum(Cap::class.java)
    miterLimit = buf.readFloat()
    roundSegmentsMin = buf.readVarInt()
    roundSegmentsMax = buf.readVarInt()

    // Re-cache the triangles
    cachedTriangles = buildPolylineTriangles(points)
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    super.writeInitial(buf)
    buf.writeFloat(scale)
    buf.writeEnum(join)
    buf.writeEnum(cap)
    buf.writeFloat(miterLimit)
    buf.writeVarInt(roundSegmentsMin)
    buf.writeVarInt(roundSegmentsMax)
  }

  /**
   * Precompute the left/right offsets at each polyline vertex, according to join type.
   * For closed loops, pass isClosed=true to compute wrapped joins at endpoints.
   */
  private fun computeOffsets(points: List<Vec2>, isClosed: Boolean = false): List<ExtrudedVertex> {
    val n = points.size
    val half = scale * 0.5f
    val out = ArrayList<ExtrudedVertex>(n)

    // Precompute tangents + normals per segment
    val segT = ArrayList<Vec2>(max(0, n - 1))
    val segN = ArrayList<Vec2>(max(0, n - 1))

    for (i in 0 until n - 1) {
      val d = points[i + 1] - points[i]
      val t = d.normalized()

      if (t.length() < 1e-6f) {
        segT.add(Vec2(0f, 0f)); segN.add(Vec2(0f, 0f))
      } else {
        segT.add(t); segN.add(t.perp())
      }
    }

    fun joinOffsets(i: Int): ExtrudedVertex {
      // For open polylines, endpoints use simple segment normal (caps handle the rest)
      if (!isClosed && (i == 0 || i == n - 1)) {
        val idx = if (i == 0) 0 else n - 2
        val nrm = segN.getOrElse(idx) { Vec2(0f, 0f) }
        return ExtrudedVertex(nrm * half, nrm * -half)
      }

      // Get prev/next tangents, wrapping for closed loops
      val tPrev = if (i > 0) segT[i - 1] else if (isClosed) {
        val d = points[0] - points[n - 1]
        d.normalized()
      } else segT[0]
      
      val tNext = if (i < n - 1) segT[i] else if (isClosed) {
        val d = points[0] - points[n - 1]
        d.normalized()
      } else segT[n - 2]
      
      val nPrev = tPrev.perp()
      val nNext = tNext.perp()
      if (tPrev.length() < 1e-6f || tNext.length() < 1e-6f) {
        // Degenerate at vertex: fall back to available normal
        val nrm = if (tPrev.length() > 0f) nPrev else nNext
        return ExtrudedVertex(nrm * half, nrm * -half)
      }

      // If almost colinear, just use the next normal (prevents tiny numerical spikes)
      val dp = tPrev.dot(tNext)
      if (abs(dp) > 0.999f) {
        return ExtrudedVertex(nNext * half, nNext * -half)
      }

      val m = nPrev.add(nNext).normalized()
      val denom = m.dot(nNext) // projection term
      // U-turn or near 180°: fall back to bevel/round
      val valid = abs(denom) > 1e-4f

      if (join == Join.MITER && valid) {
        val mScale = half / denom
        if (mScale / half <= miterLimit) {
          return ExtrudedVertex(m * mScale, m * -mScale)
        }
        // else fall through to bevel
      }

      // Bevel or fallback
      if (join == Join.BEVEL || join == Join.ROUND || !valid) {
        // Pick the two simple offsets from adjacent normals. We'll stitch the bevel wedge during triangle emission.
        val mScale  = if (valid) half / denom else half
        val mScaled = m * mScale
        return ExtrudedVertex(mScaled, mScaled * -1f)
      }

      // Round: use the bisector direction for center; we still return simple offsets
      return ExtrudedVertex(nNext * half, nNext * -half)
    }

    for (i in points.indices) out += joinOffsets(i)
    return out
  }

  private fun buildPolylineTriangles(pointsIn: List<Vec2>): List<Triangle>? {
    // Clean + early outs
    val points = pointsIn.filterIndexed { i, _ ->
      i == 0 || (pointsIn[i] - pointsIn[i-1]).length() > 1e-6f
    }
    if (points.size < 2) return null

    // Detect closed loop: first and last points coincide
    val eps = 1e-6f
    val isClosed = points.size >= 3 && (points.first() - points.last()).length() <= eps
    val pts = if (isClosed) points.dropLast(1) else points
    if (pts.size < 2) return null

    val tris = mutableListOf<Triangle>()
    val half = scale * 0.5f
    val offsets = computeOffsets(pts, isClosed)

    // Helper to push a quad (as two triangles) for a segment.
    // For MITER joins we use precomputed vertex offsets so quads meet at the miter.
    // For BEVEL/ROUND joins, use per-segment normals so quads terminate at their own normals;
    // the join wedges (bevel/round) will be added separately below.
    fun emitSegment(i: Int) {
      val a = pts[i];   val b = pts[(i + 1) % pts.size]

      if (join == Join.MITER) {
        val offA = offsets[i]
        val offB = offsets[(i + 1) % pts.size]

        val aL = a + offA.left
        val aR = a + offA.right
        val bL = b + offB.left
        val bR = b + offB.right

        tris.add(Triangle(aL, aR, bL))
        tris.add(Triangle(bL, aR, bR))
      } else {
        val dir = (b - a).normalized()
        val nrm = dir.perp()

        val aL = a + nrm * half
        val aR = a - nrm * half
        val bL = b + nrm * half
        val bR = b - nrm * half

        tris.add(Triangle(aL, aR, bL))
        tris.add(Triangle(bL, aR, bR))
      }
    }

    // Emit all segment quads
    val numSegments = if (isClosed) pts.size else pts.size - 1
    for (i in 0 until numSegments) emitSegment(i)

    // Joins between segments
    val joinIndices = if (isClosed) (0 until pts.size) else (1 until pts.size - 1)
    for (i in joinIndices) {
      val iPrev = if (i > 0) i - 1 else pts.lastIndex
      val iNext = if (i < pts.lastIndex) i + 1 else 0
      val P = pts[i]
      val tPrev = (pts[i] - pts[iPrev]).normalized()
      val tNext = (pts[iNext] - pts[i]).normalized()
      val nPrev = tPrev.perp()
      val nNext = tNext.perp()

      when (join) {
        Join.MITER -> {
          // If computeOffsets fell back to bevel (miterLimit), nothing to do here. But if it used true miter, the
          // segment quads already meet at the mitered corner.
        }
        Join.BEVEL -> {
          // Create a wedge on the *outer* side only.
          val cross       = tPrev.x * tNext.y - tPrev.y * tNext.x
          val turningLeft = cross > 0f

          // Use the opposite normals (match ROUND) so we build the exterior wedge
          val a = P + (if (turningLeft) -nPrev else nPrev) * half
          val b = P + (if (turningLeft) -nNext else nNext) * half

          // Ensure consistent front-face winding (match ROUND logic):
          // For left turns (CCW sweep), order outer edge a->b then center P.
          // For right turns (CW sweep), reverse outer order to keep front-facing.
          if (turningLeft) {
            tris.add(Triangle(a, b, P))
          } else {
            tris.add(Triangle(b, a, P))
          }
        }
        Join.ROUND -> {
          val cross       = tPrev.x * tNext.y - tPrev.y * tNext.x
          val turningLeft = cross > 0f
          // Use the opposite normal set to target the visible outside wedge
          val outerStart  = if (turningLeft) -nPrev else nPrev
          val outerEnd    = if (turningLeft) -nNext else nNext

          // compute sweep direction (counterclockwise if turning left) over the small arc
          val ang0 = atan2(outerStart.y, outerStart.x)
          val ang1 = atan2(outerEnd.y, outerEnd.x)
          var delta = ang1 - ang0
          if (turningLeft && delta < 0f) delta += 2f * PI.toFloat()
          if (!turningLeft && delta > 0f) delta -= 2f * PI.toFloat()

          val arcSteps = max(
            roundSegmentsMin,
            min(roundSegmentsMax, (abs(delta) / (PI.toFloat() / 16f)).toInt() + 1)
          )

          val arcPts = ArrayList<Vec2>(arcSteps + 1)
          for (s in 0..arcSteps) {
            val a = ang0 + delta * (s.toFloat() / arcSteps)
            arcPts.add(P + Vec2(cos(a), sin(a)) * half)
          }

          // fan along outer side only
          for (k in 0 until arcPts.size - 1) {
            if (turningLeft) {
              tris.add(Triangle(arcPts[k], arcPts[k + 1], P))   // CCW
            } else {
              tris.add(Triangle(arcPts[k + 1], arcPts[k], P))   // CW
            }
          }
        }
      }
    }

    // Caps
    fun capAt(p: Vec2, t: Vec2, start: Boolean) {
      when (cap) {
        Cap.BUTT -> {} // nothing
        Cap.SQUARE -> {
          val shift = if (start) t * (-half) else t * half
          // shift the two end vertices along t and stitch a quad slice
          val nrm = t.perp()
          val A = p + nrm * half
          val B = p - nrm * half
          val C = A + shift
          val D = B + shift

          // two triangles: (A,B,C) (C,B,D)
          tris.add(Triangle(A, B, C))
          tris.add(Triangle(C, B, D))
        }
        Cap.ROUND -> {
          val dir = if (start) -t else t // facing outward
          val baseAngle  = atan2(dir.y, dir.x)
          val sweepStart = baseAngle - PI.toFloat() / 2f
          val sweepEnd   = baseAngle + PI.toFloat() / 2f
          val steps      = max(roundSegmentsMin, 8)

          val arcPts = ArrayList<Vec2>(steps + 1)
          for (i in 0..steps) {
            val a = sweepStart + (sweepEnd - sweepStart) * (i.toFloat() / steps)
            arcPts.add(p + Vec2(cos(a), sin(a)) * half)
          }

          for (k in 0 until arcPts.size - 1) {
            tris.add(Triangle(arcPts[k], arcPts[k + 1], p))
          }
        }
      }
    }

    if (!isClosed) {
      val t0 = (pts[1] - pts[0]).normalized()
      val t1 = (pts[pts.lastIndex] - pts[pts.lastIndex - 1]).normalized()
      capAt(pts.first(), t0, start = true)
      capAt(pts.last(),  t1, start = false)
    }

    return tris
  }
}
