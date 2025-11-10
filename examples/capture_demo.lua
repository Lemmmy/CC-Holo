local strings = require("cc.strings")

local hologram = peripheral.find("hologram")
local username = "Dev" -- User to capture

local log = {}

hologram.clearAllCanvasesGlobally()

local canvas2d = hologram.getCanvas2d(username)
canvas2d.clear()
local textObj = canvas2d.addText(4, 4, "", 0xFFFFFFAA)
textObj.setShadow(true)

-- 3D canvas for raycast visualization
local canvas3d = hologram.getCanvas3d(username)
local block1Size, block2Size, entitySize = 0.2, 1.05, 0.5
local group3d, blockHitCube1, blockHitCube2, entityHitCube

-------------------------------------------------------------------------------
-- DEMO UTILITY FUNCTIONS - IGNORE
-------------------------------------------------------------------------------
function logPrint(msg)
  print(msg)
  table.insert(log, msg)
end

function stringifyValue(argN, val)
  if argN == 3 and type(val) == "string" and #val == 36 and val:sub(9, 9) == "-" then
    -- skip UUID arguments
    return "(uuid)"
  elseif type(val) == "number" then
    -- truncate numbers
    return string.format("%.1f", val)
  elseif type(val) == "table" then
    -- try to serialise the table, giving up if it fails
    ok, str = pcall(function()
      local serialised = textutils.serialiseJSON(val)
      local lines = strings.wrap(serialised, 160)
      local joinedOut = ""
      for i = 1, #lines do
        joinedOut = joinedOut .. "\n" .. lines[i]
      end
      return joinedOut
    end)

    if not ok or str ~= nil then
      return str
    end
  end

  return tostring(val)
end

function createRaycastCubes()
  if not group3d then
    group3d = canvas3d.create()
    -- Red semi-transparent cube for block exact position hits
    blockHitCube1 = group3d.addBox(0, 0, 0, block1Size, block1Size, block1Size, 0xFF000040)
    blockHitCube1.setDepthTested(false)
    -- Red semi-transparent cube for block itself hits
    blockHitCube2 = group3d.addBox(0, 0, 0, block2Size, block2Size, block2Size, 0xFF000020)
    blockHitCube2.setDepthTested(false)
    -- Blue semi-transparent cube for entity hits (1x1x1)
    entityHitCube = group3d.addBox(0, 0, 0, entitySize, entitySize, entitySize, 0x0000FF40)
    entityHitCube.setDepthTested(false)
  end
end

function removeRaycastCubes()
  if group3d then
    group3d.remove()
    group3d = nil
    blockHitCube1 = nil
    blockHitCube2 = nil
    entityHitCube = nil
  end
end

function updateRaycastVisualization(raycast)
  if not raycast then return end

  if raycast.blockHit and blockHitCube1 then
    local loc = raycast.blockHit.location
    local pos = raycast.blockHit.pos
    blockHitCube1.setPosition(loc.x - (block1Size / 2), loc.y - (block1Size / 2), loc.z - (block1Size / 2))
    blockHitCube2.setPosition(pos.x + (1 - block2Size) / 2, pos.y + (1 - block2Size) / 2, pos.z + (1 - block2Size) / 2)
  else
    blockHitCube1.setPosition(0, 0, 0)
    blockHitCube2.setPosition(0, 0, 0)
  end

  if raycast.entityHit and entityHitCube then
    local loc = raycast.entityHit.location
    entityHitCube.setPosition(loc.x - (entitySize / 2), loc.y - (entitySize / 2), loc.z - (entitySize / 2))
  else
    entityHitCube.setPosition(0, 0, 0)
  end
end

-------------------------------------------------------------------------------
-- FUNCTIONS
-------------------------------------------------------------------------------
-- Clear all key captures for a player (does not stop capture mode)
hologram.clearKeyCaptures(username)
-- Clearing all canvases will also clear and stop all captures server-wide, for every player
-- hologram.clearAllCanvasesGlobally()

-- Enter capture mode:
-- hologram.startCapture(username)
-- hologram.startCapture(username, true) -- include hologram_mouse_move events
-- hologram.startCapture(username, true, true) -- hide the mouse cursor
-- hologram.stopCapture(username)

-- Global key capture:
hologram.startKeyCapture(username, keys.g)
hologram.startKeyCapture(username, keys.h)
hologram.startKeyCapture(username, keys.j)
-- hologram.stopKeyCapture(username, key)
logPrint("Press G to start capture")
logPrint("Press H to start capture with hidden mouse")
logPrint("Press J to open a link")

-------------------------------------------------------------------------------
-- EVENTS
-------------------------------------------------------------------------------
-- `hologram_key`
--   Fired when a key is pressed while in capture mode or while a key is being captured.
--   Parameters: player:string, uuid:string, key:number, repeat:boolean, capture_mode:boolean
-- `hologram_key_up`
--   Fired when a key is released while in capture mode or while a key is being captured.
--   Parameters: player:string, uuid:string, key:number, capture_mode:boolean
-- `hologram_char`
--   Fired when a character is typed while in capture mode.
--   Parameters: player:string, uuid:string, char:string
-- `hologram_paste`
--   Fired when text is pasted while in capture mode.
--   Parameters: player:string, uuid:string, text:string
-- `hologram_mouse_click`
--   Fired when a mouse button is clicked while in capture mode.
--   Parameters: player:string, uuid:string, button:number, x:number, y:number, raycast:table
-- `hologram_mouse_up`
--   Fired when a mouse button is released while in capture mode.
--   Parameters: player:string, uuid:string, button:number, x:number, y:number, raycast:table
-- `hologram_mouse_drag`
--   Fired when the mouse is dragged (moved while a button is held) in capture mode.
--   Parameters: player:string, uuid:string, last_button:number, x:number, y:number, raycast:table
-- `hologram_mouse_move`
--   Fired when the mouse is moved (moved while nothing is held) in capture mode. Button is always 1.
--   Parameters: player:string, uuid:string, button:number, x:number, y:number, raycast:table
-- `hologram_mouse_scroll`
--   Fired when the mouse wheel is scrolled while in capture mode.
--   Parameters: player:string, uuid:string, direction:number, x:number, y:number, raycast:table
--
-- Raycast table format:
--   raycast = {
--     blockHit = {
--       pos = { x, y, z },           -- Block position (integer coordinates)
--       location = { x, y, z },      -- Exact hit location (decimal coordinates)
--       face = "up"|"down"|"north"|"south"|"east"|"west",  -- Face that was hit
--       block = "minecraft:stone",   -- Block ID
--       inside = false               -- Whether the hit was from inside the block
--     },
--     entityHit = {
--       id = 123,                    -- Entity ID
--       type = "minecraft:cow",      -- Entity type
--       name = "Cow",                -- Entity name (custom or default)
--       pos = { x, y, z },           -- Entity position
--       location = { x, y, z }       -- Exact hit location
--     }
--   }
--   Both blockHit and entityHit are nil if nothing was hit.
--
-- `hologram_capture_stop`
--   Fired when capture mode is stopped for a player.
--   Parameters: player:string, uuid:string, reason:string
-- `hologram_screen_size`
--   Fired when a player's screen size changes.
--   Parameters: player:string, uuid:string, screen_width:number, screen_height:number, gui_scaled_width:number,
--               gui_scaled_height:number, gui_scale:number

while true do
  local data = {os.pullEvent()}

  local event = data[1]
  local out = event

  if #data > 1 then
    -- event name
    out = out .. ": "

    -- event args
    for i = 2, #data do
      local val = data[i]
      out = out .. stringifyValue(i, val)
      if i < #data then
        out = out .. ", " -- comma-separated
      end
    end

    -- start capture when G pressed
    -- data[2] = username
    -- data[3] = uuid
    -- data[4] = key
    -- data[5] = repeat
    -- data[6] = capture mode (for entering capture mode, let's only focus on non-capture mode key inputs)
    if event == "hologram_key" and not data[5] then
      if data[4] == keys.g then
        logPrint("Starting capture")
        logPrint("Press S to stop capturing")
        createRaycastCubes()
        hologram.startCapture(data[3], true)
      elseif data[4] == keys.h then
        logPrint("Starting capture with hidden mouse")
        logPrint("Press S to stop capturing")
        createRaycastCubes()
        hologram.startCapture(data[3], true, true)
      elseif data[4] == keys.j then
        logPrint("Opening link")
        hologram.openLink(data[3], "https://google.com")
      elseif data[4] == keys.s and data[6] then
        logPrint("Stopping capture")
        hologram.stopCapture(data[3])
      end
      -- Update raycast visualization on mouse_move
      -- data[7] = raycast table
    elseif event == "hologram_mouse_move" then
      updateRaycastVisualization(data[7])
    end
  end

  -- Print to console
  logPrint(out)

  if event == "hologram_capture_stop" then
    logPrint("Capture stopped")
    removeRaycastCubes()
  end

  -- Render the last 10 log lines on the screen
  local logText = ""
  for i = math.max(1, #log - 9), #log do
    logText = logText .. log[i] .. "\n"
  end
  textObj.setText(logText)
end
