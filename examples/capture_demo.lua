local hologram = peripheral.find("hologram")
local username = "Dev" -- User to capture

local log = {}

local canvas2d = hologram.getCanvas2d(username)
canvas2d.clear()
local textObj = canvas2d.addText(4, 4, "", 0x000000FF)

function logPrint(msg)
  print(msg)
  table.insert(log, msg)
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
--   Parameters: player:string, uuid:string, button:number, x:number, y:number
-- `hologram_mouse_up`
--   Fired when a mouse button is released while in capture mode.
--   Parameters: player:string, uuid:string, button:number, x:number, y:number
-- `hologram_mouse_drag`
--   Fired when the mouse is dragged (moved while a button is held) in capture mode.
--   Parameters: player:string, uuid:string, last_button:number, x:number, y:number
-- `hologram_mouse_move`
--   Fired when the mouse is moved (moved while nothing is held) in capture mode. Button is always 1.
--   Parameters: player:string, uuid:string, button:number, x:number, y:number
-- `hologram_mouse_scroll`
--   Fired when the mouse wheel is scrolled while in capture mode.
--   Parameters: player:string, uuid:string, direction:number, x:number, y:number
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

      if i == 3 and type(val) == "string" and #val == 36 and val:sub(9, 9) == "-" then
        -- skip UUID arguments
        out = out .. "(uuid)"
      elseif type(val) == "number" then
        -- truncate numbers
        out = out .. string.format("%.1f", val)
      else
        out = out .. tostring(val)
      end

      -- comma-separated
      if i < #data then
        out = out .. ", "
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
        hologram.startCapture(data[3], true)
      elseif data[4] == keys.h then
        logPrint("Starting capture with hidden mouse")
        logPrint("Press S to stop capturing")
        hologram.startCapture(data[3], true, true)
      elseif data[4] == keys.j then
        logPrint("Opening link")
        hologram.openLink(data[3], "https://google.com")
      elseif data[4] == keys.s and data[6] then
        logPrint("Stopping capture")
        hologram.stopCapture(data[3])
      end
    end
  end

  -- Print to console
  logPrint(out)

  if event == "hologram_capture_stop" then
    logPrint("Capture stopped")
  end

  -- Render the last 10 log lines on the screen
  local logText = ""
  for i = math.max(1, #log - 9), #log do
    logText = logText .. log[i] .. "\n"
  end
  textObj.setText(logText)
end
