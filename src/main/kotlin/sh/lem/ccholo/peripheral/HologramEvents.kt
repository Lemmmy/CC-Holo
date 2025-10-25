package sh.lem.ccholo.peripheral

object HologramEvents {
  /**
   * Event fired when a key is pressed while in capture mode or while a key is being captured.
   * Parameters: player:string, uuid:string, key:number, repeat:boolean, capture_mode:boolean
   */
  const val EVENT_KEY = "hologram_key"

  /**
   * Event fired when a key is released while in capture mode or while a key is being captured.
   * Parameters: player:string, uuid:string, key:number, capture_mode:boolean
   */
  const val EVENT_KEY_UP = "hologram_key_up"

  /**
   * Event fired when a character is typed while in capture mode.
   * Parameters: player:string, uuid:string, char:string
   */
  const val EVENT_CHAR = "hologram_char"

  /**
   * Event fired when a character is typed while in capture mode.
   * Parameters: player:string, uuid:string, text:string
   */
  const val EVENT_PASTE = "hologram_paste"

  /**
   * Event fired when a mouse button is clicked while in capture mode.
   * Parameters: player:string, uuid:string, button:number, x:number, y:number
   */
  const val EVENT_MOUSE_CLICK = "hologram_mouse_click"

  /**
   * Event fired when a mouse button is released while in capture mode.
   * Parameters: player:string, uuid:string, button:number, x:number, y:number
   */
  const val EVENT_MOUSE_UP = "hologram_mouse_up"

  /**
   * Event fired when the mouse is dragged (moved while a button is held) in capture mode.
   * Parameters: player:string, uuid:string, last_button:number, x:number, y:number
   */
  const val EVENT_MOUSE_DRAG = "hologram_mouse_drag"

  /**
   * Event fired when the mouse is moved (moved while nothing is held) in capture mode.
   * Parameters: player:string, uuid:string, x:number, y:number
   */
  const val EVENT_MOUSE_MOVE = "hologram_mouse_move"

  /**
   * Event fired when the mouse wheel is scrolled while in capture mode.
   * Parameters: player:string, uuid:string, direction:number, x:number, y:number
   */
  const val EVENT_MOUSE_SCROLL = "hologram_mouse_scroll"

  /**
   * Event fired when capture mode is stopped for a player.
   * Parameters: player:string, uuid:string, reason:string
   */
  const val EVENT_CAPTURE_STOP = "hologram_capture_stop"
}
