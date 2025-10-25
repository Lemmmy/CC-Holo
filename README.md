# CC-Holo

<!-- modrinth_exclude.start -->
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/cc-holo)](https://modrinth.com/mod/cc-holo/)

[![Download on Modrinth](https://i.imgur.com/hbYUrTZ.png)](https://modrinth.com/mod/cc-holo/)
<!-- modrinth_exclude.end -->

CC-Holo is a ComputerCraft/CC: Tweaked peripheral provider for Minecraft 1.20.1 (Forge) based on the 
[Plethora-Fabric](https://github.com/SwitchCraftCC/Plethora-Fabric) and 
[Plethora](https://github.com/SquidDev-CC/plethora) mods aimed at server admins and mapmakers. It provides a holographic 
display peripheral that server admins (anybody with creative mode, and permissions to place command blocks) can use to
display 2D and 3D objects on player's screens. It also allows for basic key capture during gameplay, and more detailed
mouse and keyboard capture through a 'capture mode' GUI.

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.4.9
- [CC: Tweaked](<https://modrinth.com/mod/cc-tweaked>) 1.113.1–1.116.1

## Documentation

The two demo files in [`examples/demo.lua`](examples/demo.lua) and 
[`examples/capture_demo.lua`](examples/capture_demo.lua) contain examples of how to use the mod's features and a list
of most of the available functions. You can also
[search for `@LuaFunction`](<https://github.com/search?q=repo%3ALemmmy%2FCC-Holo%20%40LuaFunction&type=code]>) in the 
source code to find every function if you prefer.

## Modpacks

Modpack use: **allowed**

## Disclaimer

This mod was created for a commission, so the feature set is largely limited to the needs of the commission. It is not
intended to be a port of, or replacement for Plethora, but a Forge port of Plethora may happen separately in the future.
Bug reports, feature requests, and pull requests are welcome, but maintenance of the mod will primarily suit the needs
of the commission.

## License

This mod and its source code is licensed under the [MIT license](LICENSES/MIT.txt). The code for the display objects and
rendering are largely based on [Plethora-Fabric](https://github.com/SwitchCraftCC/Plethora-Fabric), which is also under
the [MIT license](https://github.com/SwitchCraftCC/Plethora-Fabric/blob/e451a93/LICENSE). 

<!-- modrinth_exclude.start -->
A small amount of code from [CC: Tweaked](https://github.com/cc-tweaked/CC-Tweaked) 1.116.1 has been ported to provide
compatibility with CC: Tweaked 1.113.1, while still retaining bugfixes from later CC versions, and avoiding a dependency
on CC's internal methods. These files are
[CCAttachedComputerSet.kt](src/main/kotlin/sh/lem/ccholo/util/CCAttachedComputerSet.kt) and
[CCStringUtil.kt](src/main/kotlin/sh/lem/ccholo/util/CCStringUtil.kt), and the code they were based on was originally
licensed under [MPL-2.0](LICENSES/MPL-2.0.txt). These two ported files remain licensed under the MPL-2.0 license.
<!-- modrinth_exclude.end -->
