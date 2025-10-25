package sh.lem.ccholo.objects.object3d

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import net.minecraftforge.registries.ForgeRegistries
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ItemObject
import sh.lem.ccholo.objects.ObjectRegistry.ITEM_3D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.util.*

class Item3d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : BaseObject(id, parent, ITEM_3D, canvasRoot), Scalable, Positionable3d, DepthTestable, ItemObject, Rotatable3d {
  override var position: Vec3 by DirtyingProperty(Vec3.ZERO)
  override var rotation: Vec3? by DirtyingProperty(Vec3.ZERO)
  override var hasDepthTest by DirtyingProperty(true)
  override var scale by DirtyingProperty(1.0f)

  internal var stack: ItemStack? = null
  override var item: Item? by DirtyingProperty(Items.STONE) { _, _, _ -> stack = null }
  // TODO: Block states? Metadata? Can we use the NBT parser from /give?

  override fun readInitial(buf: FriendlyByteBuf) {
    position = buf.readVec3()
    rotation = buf.readOptVec3()
    scale = buf.readFloat()

    val id = ResourceLocation.tryParse(buf.readUtf())
    item = ForgeRegistries.ITEMS.getValue(id)

    hasDepthTest = buf.readBoolean()
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeVec3(position)
    buf.writeOptVec3(rotation)
    buf.writeFloat(scale)
    buf.writeUtf(ForgeRegistries.ITEMS.getKey(item).toString())
    buf.writeBoolean(hasDepthTest)
  }
}
