package sh.lem.ccholo.objects.object2d

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec2
import net.minecraftforge.registries.ForgeRegistries
import sh.lem.ccholo.canvas.CanvasRoot
import sh.lem.ccholo.objects.BaseObject
import sh.lem.ccholo.objects.ItemObject
import sh.lem.ccholo.objects.ObjectRegistry.ITEM_2D
import sh.lem.ccholo.objects.Scalable
import sh.lem.ccholo.util.DirtyingProperty
import sh.lem.ccholo.util.readVec2
import sh.lem.ccholo.util.writeVec2

class Item2d(
  id: Int,
  parent: Int,
  canvasRoot: CanvasRoot,
) : BaseObject(id, parent, ITEM_2D, canvasRoot), Scalable, ItemObject, Positionable2d {
  override var position: Vec2 by DirtyingProperty(Vec2.ZERO)
  override var scale by DirtyingProperty(1.0f)

  internal var stack: ItemStack? = null
  override var item: Item? by DirtyingProperty(Items.STONE) { _, _, _ -> stack = null }
  override var nbt: CompoundTag? by DirtyingProperty(null) { _, _, _ -> stack = null }

  override fun readInitial(buf: FriendlyByteBuf) {
    position = buf.readVec2()
    scale = buf.readFloat()

    val id = ResourceLocation.tryParse(buf.readUtf())
    item = ForgeRegistries.ITEMS.getValue(id)
    nbt = buf.readNullable(FriendlyByteBuf::readNbt)
  }

  override fun writeInitial(buf: FriendlyByteBuf) {
    buf.writeVec2(position)
    buf.writeFloat(scale)
    buf.writeUtf(ForgeRegistries.ITEMS.getKey(item).toString())
    buf.writeNullable(nbt, FriendlyByteBuf::writeNbt)
  }
}
