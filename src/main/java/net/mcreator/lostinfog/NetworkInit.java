package net.mcreator.lostinfog;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD)
public class NetworkInit {
    
    public record SyncFrostbitePacket(int frostbite, int freezeTimer, int warmthTimer) implements CustomPacketPayload {
        public static final Type<SyncFrostbitePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "sync_frostbite"));

        public static final StreamCodec<FriendlyByteBuf, SyncFrostbitePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.frostbite());
                buf.writeInt(packet.freezeTimer());
                buf.writeInt(packet.warmthTimer());
            },
            buf -> new SyncFrostbitePacket(buf.readInt(), buf.readInt(), buf.readInt())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0.0");
        registrar.playToClient(
            SyncFrostbitePacket.TYPE,
            SyncFrostbitePacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    handleClient(payload);
                }
            })
        );
    }

    private static void handleClient(SyncFrostbitePacket payload) {
        if (Minecraft.getInstance().player != null) {
            FrostbiteData data = Minecraft.getInstance().player.getData(LostinfogModAttachments.FROSTBITE);
            data.frostbite = payload.frostbite();
            data.freezeTimer = payload.freezeTimer();
            data.warmthTimer = payload.warmthTimer();
        }
    }
}