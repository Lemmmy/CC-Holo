local hologram = peripheral.find("hologram")
local username = "Dev" -- User to display the demo to

-------------------------------------------------------------------------------
-- UTILITIES
-------------------------------------------------------------------------------
-- Convenience function to get the position of the hologram peripheral
local x, y, z = hologram.getPosition()
-- Convenience function to get the name of the world the hologram peripheral is in
local world = hologram.getDimension()

-- Clear all holograms server-wide, for every player and world
hologram.clearAllCanvasesGlobally()

-------------------------------------------------------------------------------
-- 3D OBJECTS
-------------------------------------------------------------------------------
-- Per-player root for all 3D objects. Calling this twice will give you the same root (though it will be a different Lua
-- object). You can keep this reference around even if the player rejoins the server, as it will be persisted for as
-- long as the server is running.
local canvas3d = hologram.getCanvas3d(username)
-- canvas3d.clear()

-- To show objects, you need to create a group within the 'canvas root':
local group3d = canvas3d.create() -- Centered in the hologram peripheral's world at 0, 0, 0
-- You can also center a group elsewhere, for example:
--   canvas3d.create(true) - centered at the center of the hologram peripheral's block
--   canvas3d.create(x, y, z)
--   canvas3d.create({ x=0, y=0, z=0 })

-- Box3d
--   function(x:number, y:number, z:number[, width:number, height:number, depth:number][, colour:number]):Box3d
local box1 = group3d.addBox(x, y + 1, z - 2)
local box2 = group3d.addBox(x, y + 2, z    , 1.25, 1.25, 1.25) -- with size
local box3 = group3d.addBox(x, y + 2, z + 2, 0xFF000055) -- with colour in RRGGBBAA format
-- Box3d.setPosition(x:number, y:number, z:number)
-- Box3d.setSize(width:number, height:number, depth:number)
-- Box3d.setColour(rrggbbaa:number)
-- Box3d.setColour(r:number, g:number, b:number[, a:number])
-- Box3d.setAlpha(alpha:number)
-- Box3d.setDepthTested(depthTested:boolean)

local boxX, boxY, boxZ = box1.getPosition() -- all properties have getters
box1.setPosition(boxX, boxY + 1, boxZ)

box2.setColour(0xFFFF00FF) -- in RRGGBBAA format
box2.setColour(255, 255, 0) -- red, green, blue
box2.setColour(255, 255, 0, 255) -- red, green, blue, alpha
box2.setAlpha(255)
box2.setDepthTested(false) -- defaults to 'true'. set to 'false' to render this in front of other blocks in the world

-- Item3d
--   function(x:number, y:number, z:number, itemId:string[, scale:number]):Item3d
local item3d1 = group3d.addItem(x, y + 4, z - 2, "minecraft:stone")
local item3d2 = group3d.addItem(x, y + 4, z    , "minecraft:diamond_pickaxe", 2) -- with scale
local item3d3 = group3d.addItem(x, y + 4, z + 2, "minecraft:diamond_pickaxe", 0.5)
-- Item3d.setItem(itemId:string)
-- Item3d.setPosition(x:number, y:number, z:number)
-- Item3d.setRotation(x:number, y:number, z:number)
-- Item3d.setRotation()
-- Item3d.setScale(scale:number)
-- Item3d.setDepthTested(depthTested:boolean)

item3d1.setRotation(0, 0, 0) -- reset rotation (defaults to 0, 0, 0)
item3d2.setRotation(45, 45, 0) -- in degrees
item3d3.setRotation() -- always face the player

-- ObjectFrame3d
--   function(x:number, y:number, z:number):ObjectFrame3d
-- Creates a 2D canvas that is rendered in 3D space. All of the methods from the 2D canvas (shown below) are available.
-- The canvas is 512x288 (16:9), the same as the screen-space 2D canvas.
local frame3d = group3d.addFrame(x - 2, y + 4, z - 2)
frame3d.setRotation(45, 45, 45) -- in degrees
frame3d.addRectangle(0, 0, 16, 16, 0xFF0000FF)
-- ObjectFrame3d.setPosition(x:number, y:number, z:number)
-- ObjectFrame3d.setRotation(x:number, y:number, z:number)
-- ObjectFrame3d.setRotation()
-- ObjectFrame3d.setScale(scale:number) -- defaults to 1/64
-- ObjectFrame3d.setDepthTested(depthTested:boolean)
-- plus all of the add* methods from the 2D canvas (shown below)

-------------------------------------------------------------------------------
-- 2D OBJECTS
-------------------------------------------------------------------------------
-- Per-player root for all 2D objects. Calling this twice will give you the same root (though it will be a different Lua
-- object). You can keep this reference around even if the player rejoins the server, as it will be persisted for as
-- long as the server is running.
-- The 2D canvas is a fixed 512x288 (16:9) canvas that's stretched to fill the player's screen.
local canvas2d = hologram.getCanvas2d(username)
local canvasW, canvasH = canvas2d.getSize()
-- canvas2d.clear()

-- Rectangle2d
--   function(x:number, y:number, width:number, height:number[, colour:number]):Rectangle2d
local rect1 = canvas2d.addRectangle(4, 10, 32, 16, 0xFF0000FF) -- with colour in RRGGBBAA format
-- Rectangle2d.setPosition(x:number, y:number)
-- Rectangle2d.setSize(width:number, height:number)
-- Rectangle2d.setColour(rrggbbaa:number)
-- Rectangle2d.setColour(r:number, g:number, b:number[, a:number])
-- Rectangle2d.setAlpha(alpha:number)

local rect2 = canvas2d.addRectangle(0, 0, 1, 1)
rect2.remove() -- all objects can be removed

-- Line2d
--   function(startX:number, startY: number, endX:number, endY:number[, color:number][, thickness:number]):Line2d
local line1 = canvas2d.addLine(16, 4, 24, 24)
local line2 = canvas2d.addLine(24, 4, 16, 24, 0x0000FFFF, 3)
-- Line2d.getPoint(idx:number):number, number
-- Line2d.setPoint(idx:number, x:number, y:number)
-- Line2d.setColour(rrggbbaa:number)
-- Line2d.setColour(r:number, g:number, b:number[, a:number])
-- Line2d.setAlpha(alpha:number)
-- Line2d.setScale(thickness:number) -- setScale sets the thickness of the line

-- Dot2d
--   function(x:number, y: number[, colour:number][, size:number]):Dot2d
local dot1 = canvas2d.addDot(4, 4)
local dot2 = canvas2d.addDot(8, 4, 0xFF0000FF) -- with colour in RRGGBBAA format
local dot3 = canvas2d.addDot(12, 4, 0xFF0000FF, 3) -- with scale
-- Dot2d.setPosition(x:number, y:number)
-- Dot2d.setColour(rrggbbaa:number)
-- Dot2d.setColour(r:number, g:number, b:number[, a:number])
-- Dot2d.setAlpha(alpha:number)
-- Dot2d.setScale(scale:number)

-- Text2d
--   function(x:number, y: number[, color:number][, size:number]):Text2d
local text1 = canvas2d.addText(4, 48, "graphics design is my passion")
local text2 = canvas2d.addText(4, 60, "hello, world!\nline 2", 0xFF0000FF) -- newlines are supported
local text3 = canvas2d.addText(4, 82, "AAA", 0xFF0000FF, 3) -- with scale
text3.setShadow(true) -- with drop shadow
-- Text2d.setPosition(x:number, y:number)
-- Text2d.setColour(rrggbbaa:number)
-- Text2d.setColour(r:number, g:number, b:number[, a:number])
-- Text2d.setAlpha(alpha:number)
-- Text2d.setScale(scale:number)
-- Text2d.setText(text:string)
-- Text2d.setShadow(shadow:boolean)
-- Text2d.setLineHeight(lineHeight:number) -- defaults to 9

-- Triangle2d
--   function(p1:table, p2:table, p3:table[, colour:number]):Triangle2d
local tri = canvas2d.addTriangle({ x=128, y=4 }, { x=140, y=4 }, { x=128, y=32 })
-- Triangle2d.getPoint(idx:number):number, number
-- Triangle2d.setPoint(idx:number, x:number, y:number)
-- Triangle2d.setColour(rrggbbaa:number)
-- Triangle2d.setColour(r:number, g:number, b:number[, a:number])
-- Triangle2d.setAlpha(alpha:number)

-- Polygon2d
--   function(points...:table[, color:number]):Polygon2d
-- Convex polygons up to 255 points. Example pentagon rendering code:
local function makePolygon(cx, cy, sides, diameter, addFinal)
  local radius = diameter / 2
  local points = {}

  for i = 0, sides - 1 do
    local angle = (-math.pi / 2) + i * (2 * math.pi / sides)
    local px = cx + math.cos(angle) * radius
    local py = cy + math.sin(angle) * radius
    table.insert(points, { x=px, y=py })
  end

  if addFinal then
    table.insert(points, points[1])
  end

  return points
end

-- Workaround for table.unpack not working on LHS of comma
local function appendUnpack(t, ...)
  local values = {table.unpack(t)}
  for i = 1, select("#", ...) do
    values[#values + 1] = select(i, ...)
  end
  return table.unpack(values)
end

local poly = canvas2d.addPolygon(table.unpack(makePolygon(80, 20, 5, 32)))
-- Polygon2d.getPointCount():number, number
-- Polygon2d.getPoint(idx:number):number
-- Polygon2d.setPoint(idx:number, x:number, y:number)
-- Polygon2d.insertPoint([idx:number, ]x:number, y:number)
-- Polygon2d.removePoint(idx:number)
-- Polygon2d.setColour(rrggbbaa:number)
-- Polygon2d.setColour(r:number, g:number, b:number[, a:number])
-- Polygon2d.setAlpha(alpha:number)

-- Lines2d
--   function(points...:table[, color:number[, thickness:number]]):Lines2d
-- Same as polygons, but for lines with a given thickness. Note the final point must be added manually to connect
-- the polygon as a line loop if desired.
local lines1 = canvas2d.addLines(appendUnpack(makePolygon(120, 28, 5, 36, true), 0xFF0000FF, 8))
lines1.setJoin("miter") -- the default
lines1.setCap("butt") -- the default
local lines2 = canvas2d.addLines(appendUnpack(makePolygon(170, 28, 5, 36), 0xFF0000FF, 8))
lines2.setJoin("bevel")
lines2.setCap("square")
local lines3 = canvas2d.addLines(appendUnpack(makePolygon(230, 40, 5, 64), 0xFF0000FF, 8))
lines3.setJoin("round")
lines3.setCap("round")
-- Lines2d.getPointCount():number, number
-- Lines2d.getPoint(idx:number):number
-- Lines2d.setPoint(idx:number, x:number, y:number)
-- Lines2d.insertPoint([idx:number, ]x:number, y:number)
-- Lines2d.removePoint(idx:number)
-- Lines2d.setColour(rrggbbaa:number)
-- Lines2d.setColour(r:number, g:number, b:number[, a:number])
-- Lines2d.setAlpha(alpha:number)
-- Lines2d.setScale(thickness:number) -- setScale sets the thickness of the line
-- Lines2d.setJoin(type:string) -- join type for the lines. must be "miter", "bevel", or "round". default = "miter"
-- Lines2d.setCap(type:string) -- cap type for the lines. must be "butt", "square", or "round". default = "butt"
-- Lines2d.setMiterLimit(limit:number) -- maximum allowed ratio between the miter length and line half-width.
--                                        must be between 1.0 and 10.0. default = 4.0
-- Lines2d.setRoundSegmentsMin(limit:number) -- minimum segment roundness for circular joins/caps on this line.
--                                              used to control the smoothness of arcs at small angles.
--                                              must be an integer between 1 and 8. default = 4
-- Lines2d.setRoundSegmentsMax(limit:number) -- maximum segment roundness for circular joins/caps on this line.
--                                              used to control the smoothness of 180 degree arcs.
--                                              must be an integer between 8 and 64. default = 16

-- Item2d
--   function(x:number, y:number, itemId:string[, scale:number]):Item2d
local item2d1 = canvas2d.addItem(4, 32, "minecraft:stone")
local item2d2 = canvas2d.addItem(24, 32, "minecraft:diamond_pickaxe", 2) -- with scale
local item2d3 = canvas2d.addItem(64, 32, "minecraft:diamond_pickaxe", 0.5)
-- Item2d.setItem(itemId:string)
-- Item2d.setPosition(x:number, y:number)
-- Item2d.setScale(scale:number)

-- ObjectGroup2d
--   function(x:number, y:number):ObjectGroup2d
local group = canvas2d.addGroup(canvasW / 2, 0)
-- ObjectGroup2d.setPosition(x:number, y:number)
-- plus all of the add* methods from the 2D canvas
group.addRectangle(0, 0, 16, 16, 0xFFFF00FF)

while true do
  rect1.setSize((math.sin(os.epoch("utc") / 1000) * 16) + 32, 16)
  item2d2.setScale(math.sin(os.epoch("utc") / 1000) + 1.5)
  sleep(0.05)
end
