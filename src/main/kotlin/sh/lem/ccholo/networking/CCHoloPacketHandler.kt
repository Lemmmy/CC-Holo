package sh.lem.ccholo.networking

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
import net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER
import net.minecraftforge.network.NetworkRegistry
import sh.lem.ccholo.CCHolo

object CCHoloPacketHandler {
  const val PROTOCOL_VERSION = "1";
  val channel = NetworkRegistry.newSimpleChannel(
    ResourceLocation.tryBuild(CCHolo.MOD_ID, "main"),
    { PROTOCOL_VERSION },
    // TODO: consider accepting 'ABSENT' here, though the mod has registry entries anyway so it's unlikely to be useful
    PROTOCOL_VERSION::equals,
    PROTOCOL_VERSION::equals
  )

  @Suppress("AssignedValueIsNeverRead")
  internal fun setup() {
    var id = 0

    // S->C
    channel.messageBuilder(S2CCanvasInitPacket::class.java, id++, PLAY_TO_CLIENT)
      .encoder(S2CCanvasInitPacket::encode)
      .decoder(S2CCanvasInitPacket::decode)
      .consumerMainThread(S2CCanvasInitPacket::handle)
      .add()

    channel.messageBuilder(S2CCanvasRemovePacket::class.java, id++, PLAY_TO_CLIENT)
      .encoder(S2CCanvasRemovePacket::encode)
      .decoder(S2CCanvasRemovePacket::decode)
      .consumerMainThread(S2CCanvasRemovePacket::handle)
      .add()

    channel.messageBuilder(S2CCanvasUpdatePacket::class.java, id++, PLAY_TO_CLIENT)
      .encoder(S2CCanvasUpdatePacket::encode)
      .decoder(S2CCanvasUpdatePacket::decode)
      .consumerMainThread(S2CCanvasUpdatePacket::handle)
      .add()

    channel.messageBuilder(S2CCanvasCaptureStatePacket::class.java, id++, PLAY_TO_CLIENT)
      .encoder(S2CCanvasCaptureStatePacket::encode)
      .decoder(S2CCanvasCaptureStatePacket::decode)
      .consumerMainThread(S2CCanvasCaptureStatePacket::handle)
      .add()

    // C->S
    channel.messageBuilder(C2SCanvasStopCapturePacket::class.java, id++, PLAY_TO_SERVER)
      .encoder(C2SCanvasStopCapturePacket::encode)
      .decoder(C2SCanvasStopCapturePacket::decode)
      .consumerMainThread(C2SCanvasStopCapturePacket::handle)
      .add()

    channel.messageBuilder(C2SCanvasCaptureCharPacket::class.java, id++, PLAY_TO_SERVER)
      .encoder(C2SCanvasCaptureCharPacket::encode)
      .decoder(C2SCanvasCaptureCharPacket::decode)
      .consumerMainThread(C2SCanvasCaptureCharPacket::handle)
      .add()

    channel.messageBuilder(C2SCanvasCaptureKeyPacket::class.java, id++, PLAY_TO_SERVER)
      .encoder(C2SCanvasCaptureKeyPacket::encode)
      .decoder(C2SCanvasCaptureKeyPacket::decode)
      .consumerMainThread(C2SCanvasCaptureKeyPacket::handle)
      .add()

    channel.messageBuilder(C2SCanvasCaptureMousePacket::class.java, id++, PLAY_TO_SERVER)
      .encoder(C2SCanvasCaptureMousePacket::encode)
      .decoder(C2SCanvasCaptureMousePacket::decode)
      .consumerMainThread(C2SCanvasCaptureMousePacket::handle)
      .add()

    channel.messageBuilder(C2SCanvasCapturePastePacket::class.java, id++, PLAY_TO_SERVER)
      .encoder(C2SCanvasCapturePastePacket::encode)
      .decoder(C2SCanvasCapturePastePacket::decode)
      .consumerMainThread(C2SCanvasCapturePastePacket::handle)
      .add()

    channel.messageBuilder(C2SCanvasCaptureScrollPacket::class.java, id++, PLAY_TO_SERVER)
      .encoder(C2SCanvasCaptureScrollPacket::encode)
      .decoder(C2SCanvasCaptureScrollPacket::decode)
      .consumerMainThread(C2SCanvasCaptureScrollPacket::handle)
      .add()
  }
}
