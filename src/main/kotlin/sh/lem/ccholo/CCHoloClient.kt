package sh.lem.ccholo

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.util.Lazy
import org.lwjgl.glfw.GLFW
import sh.lem.ccholo.CCHolo.MOD_ID
import thedarkcolour.kotlinforforge.forge.MOD_BUS

object CCHoloClient {
  object KeyBindings {
    internal val CAPTURE_CLOSE: net.minecraftforge.common.util.Lazy<KeyMapping> = Lazy.of {
      KeyMapping(
        "key.${MOD_ID}.capture_close",
        KeyConflictContext.GUI,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_ESCAPE,
        "key.categories.gameplay"
      )
    }

    fun onRegisterKeyMappings(event: RegisterKeyMappingsEvent) {
      event.register(CAPTURE_CLOSE.get())
    }
  }

  fun init() {
    MOD_BUS.addListener(KeyBindings::onRegisterKeyMappings)
  }
}
