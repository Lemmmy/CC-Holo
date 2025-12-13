package sh.lem.ccholo.objects.renderers

import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.renderers.object2d.*
import sh.lem.ccholo.objects.renderers.object3d.Box3dRenderer
import sh.lem.ccholo.objects.renderers.object3d.Item3dRenderer
import sh.lem.ccholo.objects.renderers.object3d.ObjectFrame3dRenderer
import sh.lem.ccholo.objects.renderers.object3d.ObjectRoot3dRenderer

object ObjectRendererRegistry {
  private val renderers by lazy {
    arrayOf(
      // 2D
      Rectangle2dRenderer,
      Line2dRenderer,
      Dot2dRenderer,
      Text2dRenderer,
      Triangle2dRenderer,
      Polygon2dRenderer,
      Lines2dRenderer,
      Item2dRenderer,
      ObjectGroup2dRenderer,
      ObjectFrame2dRenderer,
      Image2dRenderer,

      // 3D
      ObjectRoot3dRenderer,
      ObjectFrame3dRenderer,
      Box3dRenderer,
      Item3dRenderer,
      null, // Line3dRenderer,
    )
  }

  private val warnedRenderers = mutableSetOf<Byte>()

  fun <T : BaseObject>getRenderer(type: Byte): BaseObjectRenderer<T>? {
    if (type < 0 || type >= renderers.size) {
      if (warnedRenderers.add(type)) {
        // Log only once to avoid spamming the log
        CCHolo.log.warn("Unknown renderer type $type")
      }
      return null
    }

    val renderer = renderers[type.toInt()]
    if (renderer == null && warnedRenderers.add(type)) {
      // Log only once to avoid spamming the log
      CCHolo.log.warn("No renderer for type $type")
    }

    return renderer as? BaseObjectRenderer<T>
  }
}
