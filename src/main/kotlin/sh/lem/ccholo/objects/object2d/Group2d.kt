package sh.lem.ccholo.objects.object2d

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.network.chat.Component
import sh.lem.ccholo.CCHolo
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_HEIGHT
import sh.lem.ccholo.canvas.CanvasRoot.Companion.BASE_FRAME_WIDTH
import sh.lem.ccholo.objects.DEFAULT_COLOUR
import sh.lem.ccholo.objects.ObjectGroup
import sh.lem.ccholo.objects.TextObject
import sh.lem.ccholo.util.*

/**
 * A group for 2D objects
 */
interface Group2d : ObjectGroup {
  /**
   * function(x:number, y:number, width:number, height:number[, colour:number]):Rectangle2d -- Create a new rectangle.
   */
  @LuaFunction
  fun addRectangle(args: IArguments): Rectangle2d {
    val pos = args.getVec2(0)
    val size = args.getVec2(2)
    val colour = args.optInt(4, DEFAULT_COLOUR.toInt())

    val rect = Rectangle2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    rect.position = pos
    rect.size = size
    rect.colour = colour

    canvasRootServer.add(rect)
    return rect
  }

  /**
   * function(startX:number, startY: number, endX:number, endY:number[, color:number][, thickness:number]):Line2d
   * -- Create a new line.
   */
  @LuaFunction
  fun addLine(args: IArguments): Line2d {
    val start = args.getVec2(0)
    val end = args.getVec2(2)
    val colour = args.optInt(4, DEFAULT_COLOUR.toInt())
    val thickness = args.optDouble(5, 1.0).toFloat()

    val line = Line2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    line.setVertex(0, start)
    line.setVertex(1, end)
    line.colour = colour
    line.scale = thickness

    canvasRootServer.add(line)
    return line
  }

  /**
   * function(x:number, y: number[, color:number][, size:number]):Dot2d -- Create a new dot.
   */
  @LuaFunction
  fun addDot(args: IArguments): Dot2d {
    val position = args.getVec2(0)
    val colour = args.optInt(2, DEFAULT_COLOUR.toInt())
    val size = args.optDouble(3, 1.0).toFloat()

    val dot = Dot2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    dot.position = position
    dot.colour = colour
    dot.scale = size

    canvasRootServer.add(dot)
    return dot
  }

  /**
   * function(x:number, y:number, contents:string[, colour:number[, size:number[, json:boolean]]]):Text2d -- Create a
   * new text object.
   */
  @LuaFunction
  fun addText(args: IArguments): Text2d {
    val position = args.getVec2(0)
    val contents = args.assertUtf8StringLength(2, 0, TextObject.MAX_LENGTH)
    val colour = args.optInt(3, DEFAULT_COLOUR.toInt())
    val size = args.optDouble(4, 1.0).toFloat()
    val json = args.optBoolean(5, false)

    val component = if (json) try {
      Component.Serializer.fromJson(contents)
    } catch (e: Exception) {
      CCHolo.log.error("Invalid JSON string: $contents", e)
      throw LuaException("Invalid JSON string")
    } else null

    val text = Text2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    text.position = position
    text.colour = colour
    text.scale = size

    if (json) {
      text.component = component
    } else {
      text.plaintext = contents
    }

    canvasRootServer.add(text)
    return text
  }

  /**
   * function(p1:table, p2:table, p3:table[, colour:number]):Triangle2d -- Create a new triangle, composed of three
   * points.
   */
  @LuaFunction
  fun addTriangle(args: IArguments): Triangle2d {
    val p1 = args.getVec2Table(0)
    val p2 = args.getVec2Table(1)
    val p3 = args.getVec2Table(2)
    val colour = args.optInt(3, DEFAULT_COLOUR.toInt())

    val triangle = Triangle2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    triangle.setVertex(0, p1)
    triangle.setVertex(0, p2)
    triangle.setVertex(0, p3)
    triangle.colour = colour

    canvasRootServer.add(triangle)
    return triangle
  }

  /**
   * function(points...:table[, color:number]):Polygon2d -- Create a new polygon, composed of many points.
   */
  @LuaFunction
  fun addPolygon(args: IArguments): Polygon2d {
    val points = args.getVec2Tables(1)
    val colour = args.optInt(points.size, DEFAULT_COLOUR.toInt())

    val polygon = Polygon2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    polygon.points = points

    canvasRootServer.add(polygon)
    return polygon
  }

  /**
   * function(points...:table[, color:number[, thickness:number]]):Lines2d -- Create a new line loop, composed of many
   * points.
   */
  @LuaFunction
  fun addLines(args: IArguments): Lines2d {
    val points = args.getVec2Tables(2)
    val colour = args.optInt(points.size, DEFAULT_COLOUR.toInt())
    val thickness = args.optDouble(points.size + 1, 1.0).toFloat()

    val lines = Lines2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    lines.points = points
    lines.colour = colour
    lines.scale = thickness

    canvasRootServer.add(lines)
    return lines
  }

  /**
   * function(x:number, y: number, item:string[, scale:number]):Item2d -- Create a new item.
   */
  @LuaFunction
  fun addItem(args: IArguments): Item2d {
    val pos = args.getVec2(0)
    val (item, nbt) = args.getItem(2)
    val scale = args.optDouble(3, 1.0).toFloat()

    val item2d = Item2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    item2d.position = pos
    item2d.item = item
    item2d.nbt = nbt
    item2d.scale = scale

    canvasRootServer.add(item2d)
    return item2d
  }

  /**
   * function(x:number, y:number):ObjectGroup2d -- Create a new object group.
   */
  @LuaFunction
  fun addGroup(args: IArguments): ObjectGroup2d {
    val pos = args.getVec2(0)

    val group = ObjectGroup2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    group.position = pos

    canvasRootServer.add(group)
    return group
  }

  /**
   * function(x:number, y:number, [width:number, height: number]):ObjectFrame2d -- Create a new 2D frame.
   */
  @LuaFunction
  fun addFrame(args: IArguments): ObjectFrame2d {
    val pos = args.getVec2(0)
    val width = args.optInt(2, BASE_FRAME_WIDTH)
    val height = args.optInt(3, BASE_FRAME_HEIGHT)

    val frame = ObjectFrame2d(canvasRootServer.newObjectId(), id, canvasRootServer)
    frame.position = pos
    frame.setSize(width, height)

    canvasRootServer.add(frame)
    return frame
  }
}
