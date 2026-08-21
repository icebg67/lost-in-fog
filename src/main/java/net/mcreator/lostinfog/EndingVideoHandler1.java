package net.mcreator.lostinfog;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.advancements.AdvancementHolder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class EndingVideoHandler1 {

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusRegister {
        @SubscribeEvent
        public static void registerNetwork(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("lostinfog");
            registrar.playToClient(PlayEndingVideoPayload.TYPE, PlayEndingVideoPayload.STREAM_CODEC, PlayEndingVideoPayload::handle);
            registrar.playToServer(KickAllPlayersPayload.TYPE, KickAllPlayersPayload.STREAM_CODEC, KickAllPlayersPayload::handle);
        }
    }

    public record PlayEndingVideoPayload() implements CustomPacketPayload {
        public static final Type<PlayEndingVideoPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "play_ending_video_1"));
        public static final StreamCodec<io.netty.buffer.ByteBuf, PlayEndingVideoPayload> STREAM_CODEC = StreamCodec.unit(new PlayEndingVideoPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(PlayEndingVideoPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.getSoundManager() != null) {
                    mc.getSoundManager().stop();
                }
                mc.setScreen(new EndingVideoScreen());
            });
        }
    }

    public record KickAllPlayersPayload() implements CustomPacketPayload {
        public static final Type<KickAllPlayersPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "kick_all_players_1"));
        public static final StreamCodec<io.netty.buffer.ByteBuf, KickAllPlayersPayload> STREAM_CODEC = StreamCodec.unit(new KickAllPlayersPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(KickAllPlayersPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player().getServer() != null) {
                    for (ServerPlayer serverPlayer : context.player().getServer().getPlayerList().getPlayers()) {
                        serverPlayer.connection.disconnect(Component.literal("Expired"));
                    }
                }
            });
        }
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME)
    public static class ServerTickHandler {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                CompoundTag nbt = player.getPersistentData();
                
                if (nbt.getBoolean("lostinfog_ending1_played")) {
                    return;
                }

                boolean hasDay9 = false;
                if (player.getServer() != null) {
                    AdvancementHolder advancement = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath("lostinfog", "day_9"));
                    if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                        hasDay9 = true;
                    }
                }

                if (hasDay9) {
                    int ticks = nbt.getInt("lostinfog_ending1_ticks");
                    ticks++;
                    nbt.putInt("lostinfog_ending1_ticks", ticks);

                    if (ticks >= 200) {
                        nbt.putBoolean("lostinfog_ending1_played", true);
                        PacketDistributor.sendToPlayer(player, new PlayEndingVideoPayload());
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class EndingVideoScreen extends Screen {
        private static final int FPS = 25;
        private static final long FRAME_DURATION = 1000L / FPS;

        private int currentFrame = 0;
        private int lastLoadedFrame = -1;
        private long videoStartTime = -1;
        private boolean videoFinished = false;
        private boolean packetSent = false;

        private SimpleSoundInstance soundInstance;
        private boolean soundStarted = false;

        private DynamicTexture dynamicTexture;
        private int textureWidth;
        private int textureHeight;

        private final ResourceLocation videoTexture = ResourceLocation.fromNamespaceAndPath("lostinfog", "ending_video_1");
        private final Path cadriDir;

        public EndingVideoScreen() {
            super(Component.literal("Ending Video 1"));
            Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
            this.cadriDir = gameDir.resolve("config/lostinfog/cadri/death/death1");
        }

        @Override
        protected void init() {
            super.init();
            currentFrame = 0;
            lastLoadedFrame = -1;
            videoStartTime = -1;
            videoFinished = false;
            packetSent = false;
            soundStarted = false;
            soundInstance = null;
        }

        @Override
        public void renderBackground(GuiGraphics g, int mX, int mY, float pT) {
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
            long now = System.currentTimeMillis();
            g.fill(0, 0, width, height, 0xFF000000);

            if (videoStartTime == -1) {
                videoStartTime = now;
            }

            if (!soundStarted) {
                soundInstance = new SimpleSoundInstance(
                        ResourceLocation.fromNamespaceAndPath("lostinfog", "death1"),
                        SoundSource.VOICE,
                        1.0F,
                        1.0F,
                        net.minecraft.util.RandomSource.create(),
                        false,
                        0,
                        SimpleSoundInstance.Attenuation.NONE,
                        0.0D,
                        0.0D,
                        0.0D,
                        true
                );
                Minecraft.getInstance().getSoundManager().play(soundInstance);
                soundStarted = true;
            }

            if (videoFinished) {
                boolean soundIsPlaying = soundInstance != null && Minecraft.getInstance().getSoundManager().isActive(soundInstance);
                if (!soundIsPlaying && !packetSent) {
                    packetSent = true;
                    if (dynamicTexture != null) {
                        Minecraft.getInstance().getTextureManager().release(videoTexture);
                        dynamicTexture.close();
                        dynamicTexture = null;
                    }
                    Minecraft.getInstance().setScreen(null);
                    PacketDistributor.sendToServer(new KickAllPlayersPayload());
                }
                return;
            }

            long elapsed = now - videoStartTime;
            currentFrame = (int) (elapsed / FRAME_DURATION);

            if (!loadFrame(currentFrame)) {
                if (currentFrame > 10) {
                    finishVideo();
                }
                return;
            }

            if (dynamicTexture != null) {
                renderVideo(g);
            }
        }

        private void renderVideo(GuiGraphics g) {
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            g.blit(
                    videoTexture,
                    0, 0, width, height,
                    0, 0, textureWidth, textureHeight,
                    textureWidth, textureHeight
            );
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private boolean loadFrame(int frameIndex) {
            if (frameIndex == lastLoadedFrame) {
                return dynamicTexture != null;
            }

            Path frameFile = cadriDir.resolve(String.format("frame_%04d.png", frameIndex + 1));

            if (!Files.exists(frameFile)) {
                return false;
            }

            try (InputStream input = Files.newInputStream(frameFile);
                 NativeImage image = NativeImage.read(input)) {

                if (image == null) {
                    return false;
                }

                if (dynamicTexture == null) {
                    textureWidth = image.getWidth();
                    textureHeight = image.getHeight();
                    dynamicTexture = new DynamicTexture(textureWidth, textureHeight, false);
                    Minecraft.getInstance().getTextureManager().register(videoTexture, dynamicTexture);
                }

                dynamicTexture.getPixels().copyFrom(image);
                dynamicTexture.upload();
                lastLoadedFrame = frameIndex;
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private void finishVideo() {
            if (videoFinished) {
                return;
            }
            videoFinished = true;
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public void removed() {
            if (dynamicTexture != null) {
                Minecraft.getInstance().getTextureManager().release(videoTexture);
                dynamicTexture.close();
                dynamicTexture = null;
            }
            super.removed();
        }
    }
}