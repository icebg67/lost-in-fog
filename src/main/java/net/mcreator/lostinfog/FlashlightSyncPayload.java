package net.mcreator.lostinfog.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record FlashlightSyncPayload(UUID playerId, boolean isOn, float dist, float bright, float angle, float size) implements CustomPacketPayload {
    public static final Type<FlashlightSyncPayload> TYPE = new Type<>(ResourceLocation.parse("lostinfog:flashlight_sync"));

    public static final StreamCodec<ByteBuf, FlashlightSyncPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, FlashlightSyncPayload::playerId,
            ByteBufCodecs.BOOL, FlashlightSyncPayload::isOn,
            ByteBufCodecs.FLOAT, FlashlightSyncPayload::dist,
            ByteBufCodecs.FLOAT, FlashlightSyncPayload::bright,
            ByteBufCodecs.FLOAT, FlashlightSyncPayload::angle,
            ByteBufCodecs.FLOAT, FlashlightSyncPayload::size,
            FlashlightSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}