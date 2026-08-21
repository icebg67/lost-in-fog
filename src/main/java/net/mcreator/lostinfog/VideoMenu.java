package net.mcreator.lostinfog;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.mcreator.ffmpeglib.FFmpegOrchestrator;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VideoMenu {

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusRegister {
        @SubscribeEvent
        public static void registerNetwork(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("lostinfog");
            registrar.playToServer(TryWatchPayload.TYPE, TryWatchPayload.STREAM_CODEC, TryWatchPayload::handle);
            registrar.playToClient(OpenVideoPayload.TYPE, OpenVideoPayload.STREAM_CODEC, OpenVideoPayload::handle);
            registrar.playToServer(FinishVideoPayload.TYPE, FinishVideoPayload.STREAM_CODEC, FinishVideoPayload::handle);
            registrar.playToClient(StopWorldSoundPayload.TYPE, StopWorldSoundPayload.STREAM_CODEC, StopWorldSoundPayload::handle);
        }
    }

    public record TryWatchPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<TryWatchPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "try_watch"));
        public static final StreamCodec<io.netty.buffer.ByteBuf, TryWatchPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, TryWatchPayload::pos, TryWatchPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(TryWatchPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    ServerTracker.handleWatchRequest(player, payload.pos());
                }
            });
        }
    }

    public record OpenVideoPayload(BlockPos pos, int day, int awareness, int startFrame) implements CustomPacketPayload {
        public static final Type<OpenVideoPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "open_video"));
        public static final StreamCodec<io.netty.buffer.ByteBuf, OpenVideoPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, OpenVideoPayload::pos,
                ByteBufCodecs.INT, OpenVideoPayload::day,
                ByteBufCodecs.INT, OpenVideoPayload::awareness,
                ByteBufCodecs.INT, OpenVideoPayload::startFrame,
                OpenVideoPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(OpenVideoPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> Minecraft.getInstance().setScreen(new ClientTracker.VideoScreen(payload.pos(), payload.day(), payload.awareness(), payload.startFrame())));
        }
    }

    public record FinishVideoPayload(BlockPos pos, boolean skipped) implements CustomPacketPayload {
        public static final Type<FinishVideoPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "finish_video"));
        public static final StreamCodec<io.netty.buffer.ByteBuf, FinishVideoPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, FinishVideoPayload::pos,
                ByteBufCodecs.BOOL, FinishVideoPayload::skipped,
                FinishVideoPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(FinishVideoPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    ServerTracker.handleVideoFinish(player, payload.pos(), payload.skipped());
                }
            });
        }
    }

    public record StopWorldSoundPayload(int day) implements CustomPacketPayload {
        public static final Type<StopWorldSoundPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "stop_world_sound"));
        public static final StreamCodec<io.netty.buffer.ByteBuf, StopWorldSoundPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, StopWorldSoundPayload::day,
                StopWorldSoundPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(StopWorldSoundPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> ClientTracker.stopWorldSound(payload.day()));
        }
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientTracker {
        private static final File baseConfigDir;
        private static final File ffmpegExe;
        private static final File cadriDir;
        private static final File videoDir;

        static {
            File gameDir = Minecraft.getInstance().gameDirectory;
            baseConfigDir = new File(gameDir, "config/lostinfog");
            ffmpegExe = new File(gameDir, "config/ffmpeglib/ffmpeg.exe");
            cadriDir = new File(baseConfigDir, "cadri");
            videoDir = new File(baseConfigDir, "analog");
        }

        public static void stopWorldSound(int day) {
            ResourceLocation soundRes = ResourceLocation.fromNamespaceAndPath("lostinfog", "day" + day);
            Minecraft.getInstance().getSoundManager().stop(soundRes, net.minecraft.sounds.SoundSource.RECORDS);
        }

        @SubscribeEvent
        public static void onGuiOpen(ScreenEvent.Init.Post event) {
            if (event.getScreen() instanceof TitleScreen) {
                if (!baseConfigDir.exists()) baseConfigDir.mkdirs();
                if (!cadriDir.exists()) cadriDir.mkdirs();
                if (!videoDir.exists()) videoDir.mkdirs();
                
                if (!ffmpegExe.exists() || !ffmpegExe.isFile()) {
                    Minecraft.getInstance().setScreen(new FfmpegMissingScreen(event.getScreen()));
                }
            }
        }

        @SubscribeEvent
        public static void onClientLogin(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
            if (!ffmpegExe.exists() || !ffmpegExe.isFile()) {
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().setScreen(new FfmpegMissingScreen(null));
                });
                return;
            }

            CompletableFuture.runAsync(() -> {
                try {
                    File death1Video = new File(videoDir, "death1.mp4");
                    File death2Video = new File(videoDir, "death2.mp4");
                    File death1Dir = new File(cadriDir, "death/death1");
                    File death2Dir = new File(cadriDir, "death/death2");
                    extractDeathVideoResource("death1.mp4", death1Video);
                    extractDeathVideoResource("death2.mp4", death2Video);
                    extractDeathFrames(death1Video, death1Dir);
                    extractDeathFrames(death2Video, death2Dir);

                    for (int d = 1; d <= 10; d++) {
                        deleteOldFiles(new File(cadriDir, "video" + d));
                        deleteOldFiles(new File(videoDir, "anal" + d + ".mp4"));
                    }

                    for (int d = 1; d <= 9; d++) {
                        File targetDir = new File(cadriDir, "videoRE" + d);
                        File videoFile = new File(videoDir, "analRE" + d + ".mp4");
                        
                        if (!videoFile.exists()) extractVideoResource(d, videoFile);
                        
                        if (!targetDir.exists()) targetDir.mkdirs();
                        File[] existingFrames = targetDir.listFiles();
                        
                        if (existingFrames == null || existingFrames.length == 0) {
                            FFmpegOrchestrator.extractFrames(ffmpegExe, videoFile, targetDir, 25).join();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        private static void extractDeathVideoResource(String fileName, File videoFile) throws IOException {
            if (videoFile.exists()) return;
            String resPath = "/assets/lostinfog/death/" + fileName;
            try (InputStream in = VideoMenu.class.getResourceAsStream(resPath)) {
                if (in == null) throw new FileNotFoundException("Resource not found: " + resPath);
                videoFile.getParentFile().mkdirs();
                try (OutputStream out = new FileOutputStream(videoFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) out.write(buffer, 0, bytesRead);
                }
            }
        }

        private static void extractDeathFrames(File videoFile, File targetDir) {
            if (!videoFile.exists()) return;
            if (!targetDir.exists()) targetDir.mkdirs();
            File[] existingFrames = targetDir.listFiles();
            if (existingFrames == null || existingFrames.length == 0) {
                FFmpegOrchestrator.extractFrames(ffmpegExe, videoFile, targetDir, 25).join();
            }
        }

        private static void deleteOldFiles(File file) {
            if (!file.exists()) return;
            if (file.isDirectory()) {
                File[] contents = file.listFiles();
                if (contents != null) {
                    for (File f : contents) {
                        deleteOldFiles(f);
                    }
                }
            }
            file.delete();
        }

        public static class FfmpegMissingScreen extends Screen {
            private final Screen previousScreen;

            protected FfmpegMissingScreen(Screen previousScreen) {
                super(Component.literal("FFmpegLib Required"));
                this.previousScreen = previousScreen;
            }

            @Override
            protected void init() {
                super.init();
                int centerX = this.width / 2;
                int y = this.height / 2 + 40;
                
                this.addRenderableWidget(Button.builder(Component.literal("CurseForge"), b -> Util.getPlatform().openUri("https://www.curseforge.com/minecraft/mc-mods/ffmpeglib")).pos(centerX - 105, y).size(100, 20).build());
                this.addRenderableWidget(Button.builder(Component.literal("Modrinth"), b -> Util.getPlatform().openUri("https://modrinth.com/mod/ffmpeglib")).pos(centerX + 5, y).size(100, 20).build());
                this.addRenderableWidget(Button.builder(Component.literal("Continue Without Videos"), b -> onClose()).pos(centerX - 100, y + 30).size(200, 20).build());
            }

            @Override
            public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
                g.fill(0, 0, this.width, this.height, 0xDD000000);
                int centerX = this.width / 2;
                int y = this.height / 2 - 50;
                
                g.drawCenteredString(Minecraft.getInstance().font, "⚠ FFmpegLib Not Found ⚠", centerX, y, 0xFFFF5555);
                g.drawCenteredString(Minecraft.getInstance().font, "Videos require the FFmpegLib mod to function.", centerX, y + 20, 0xFFFFFFFF);
                g.drawCenteredString(Minecraft.getInstance().font, "Please download and install it:", centerX, y + 40, 0xFFFFFFFF);
                
                super.render(g, mouseX, mouseY, partialTicks);
            }

            @Override
            public void renderBackground(GuiGraphics g, int mX, int mY, float pT) {
            }

            @Override 
            public void onClose() { 
                if (previousScreen != null) Minecraft.getInstance().setScreen(previousScreen); 
            }
            
            @Override 
            public boolean shouldCloseOnEsc() { return true; }
        }

        public static class VideoScreen extends Screen {
            public final BlockPos pos;
            public final int day;
            private final int awareness;
            private final int startFrame;
            private int currentFrame = -1;
            private int lastLoadedFrame = -1;

            private static final int FPS = 25;
            private static final long FRAME_DURATION = 1000L / FPS;

            private long videoStartTime = -1;
            private boolean videoFinished = false;
            private long spacePressedTime = -1;
            private long hintVisibleUntil = System.currentTimeMillis() + 3000;

            private final ResourceLocation videoTextureRL = ResourceLocation.fromNamespaceAndPath("lostinfog", "video_frame_buffer");
            private DynamicTexture dynamicTexture = null;

            protected VideoScreen(BlockPos pos, int day, int awareness, int startFrame) {
                super(Component.literal("Video"));
                this.pos = pos;
                this.day = day;
                this.awareness = awareness;
                this.startFrame = startFrame;
            }

            @Override
            protected void init() {
                super.init();
                currentFrame = startFrame;
                lastLoadedFrame = -1;
                videoFinished = false;
                videoStartTime = -1;
                spacePressedTime = -1;
                hintVisibleUntil = System.currentTimeMillis() + 3000;
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                hintVisibleUntil = System.currentTimeMillis() + 3000;
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public void renderBackground(GuiGraphics g, int mX, int mY, float pT) {
            }

            @Override
            public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
                long now = System.currentTimeMillis();
                g.fill(0, 0, width, height, 0xFF000000);

                if (videoStartTime == -1) {
                    videoStartTime = now - (startFrame * FRAME_DURATION);
                }

                if (now >= videoStartTime) {
                    long elapsed = now - videoStartTime;
                    currentFrame = (int) (elapsed / FRAME_DURATION);
                    updateFrameTexture(currentFrame);
                }

                if (videoFinished) return;

                if (dynamicTexture != null) {
                    g.blit(videoTextureRL, 0, 0, 0, 0, width, height, width, height);
                }

                net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
                g.drawString(font, "Awareness " + awareness + "/10", 10, height - 20, 0xFFFFFFFF, false);

                boolean spaceDown = com.mojang.blaze3d.platform.InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 32);
                
                String hintText = "Press SPACE to skip";

                if (spaceDown) {
                    if (spacePressedTime == -1) {
                        spacePressedTime = now;
                    }
                    long heldTime = now - spacePressedTime;
                    if (heldTime >= 5000) {
                        finishVideo(true);
                        return;
                    }
                    hintText = "Skipping: " + (heldTime / 1000) + "s / 5s";
                } else {
                    spacePressedTime = -1;
                }
                
                if (now < hintVisibleUntil || spaceDown) {
                    g.drawString(font, hintText, (width - font.width(hintText)) / 2, height - 20, 0xFFFFFFFF, false);
                }
            }

            private void updateFrameTexture(int frameIdx) {
                if (frameIdx == lastLoadedFrame) return;

                File videoFolder = new File(cadriDir, "videoRE" + day);
                File frameFile = new File(videoFolder, String.format("frame_%04d.png", frameIdx + 1));

                if (!frameFile.exists()) {
                    File nextFrameFile = new File(videoFolder, String.format("frame_%04d.png", frameIdx + 2));
                    if (!nextFrameFile.exists()) {
                        finishVideo(false);
                    }
                    return;
                }

                try (FileInputStream fis = new FileInputStream(frameFile);
                     NativeImage nativeImage = NativeImage.read(fis)) {

                    if (nativeImage == null) return;

                    if (dynamicTexture == null) {
                        dynamicTexture = new DynamicTexture(nativeImage.getWidth(), nativeImage.getHeight(), false);
                        Minecraft.getInstance().getTextureManager().register(videoTextureRL, dynamicTexture);
                    }

                    dynamicTexture.getPixels().copyFrom(nativeImage);
                    dynamicTexture.upload();

                    lastLoadedFrame = frameIdx;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void finishVideo(boolean skipped) {
                if (videoFinished) return;
                videoFinished = true;

                if (dynamicTexture != null) {
                    Minecraft.getInstance().getTextureManager().release(videoTextureRL);
                    dynamicTexture.close();
                    dynamicTexture = null;
                }

                PacketDistributor.sendToServer(new FinishVideoPayload(this.pos, skipped));
                Minecraft.getInstance().setScreen(null);
            }

            @Override
            public boolean shouldCloseOnEsc() { return false; }
            @Override
            public boolean isPauseScreen() { return false; }

            @Override
            public void removed() {
                if (!videoFinished) {
                    finishVideo(true);
                }
                super.removed();
            }
        }

        private static void extractVideoResource(int day, File videoFile) throws IOException {
            String resPath = "/assets/lostinfog/analog/analRE" + day + ".mp4";
            if (videoFile.exists()) return;
            
            try (InputStream in = VideoMenu.class.getResourceAsStream(resPath)) {
                if (in == null) throw new FileNotFoundException("Resource not found: " + resPath);
                videoFile.getParentFile().mkdirs();
                try (OutputStream out = new FileOutputStream(videoFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onBlockClick(PlayerInteractEvent.RightClickBlock event) {
            if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;

            if (event.getLevel().isClientSide()) {
                if (event.getEntity().isShiftKeyDown()) return;

                BlockState state = event.getLevel().getBlockState(event.getPos());
                ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());

                if (blockKey.getNamespace().equals("lostinfog") && blockKey.getPath().equals("tvon")) {
                    ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
                    if (!(itemKey.getNamespace().equals("lostinfog") && itemKey.getPath().equals("cassette"))) {
                        if (!ffmpegExe.exists()) {
                            Minecraft.getInstance().player.displayClientMessage(
                                Component.literal("FFmpegLib not found! Videos are disabled."), true);
                            return;
                        }
                        PacketDistributor.sendToServer(new TryWatchPayload(event.getPos()));
                    }
                }
            }
        }
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME)
    public static class ServerTracker {
        public static class ActiveTvSession {
            public final int day;
            public final long startTick;
            public final BlockPos pos;
            public final Set<UUID> currentWatchers = new HashSet<>();

            public ActiveTvSession(int day, long startTick, BlockPos pos) {
                this.day = day;
                this.startTick = startTick;
                this.pos = pos;
            }
        }

        private static final Map<BlockPos, ActiveTvSession> activeSessions = new HashMap<>();

        @SubscribeEvent
        public static void onServerBlockClick(PlayerInteractEvent.RightClickBlock event) {
            if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
            if (event.getLevel().isClientSide()) return;
            
            ServerPlayer player = (ServerPlayer) event.getEntity();
            BlockState state = event.getLevel().getBlockState(event.getPos());
            ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());

            if (blockKey.getNamespace().equals("lostinfog") && blockKey.getPath().equals("tvon")) {
                ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
                if (itemKey.getNamespace().equals("lostinfog") && itemKey.getPath().equals("cassette")) {
                    CompoundTag nbt = player.getPersistentData();
                    if (nbt.getBoolean("lostinfog:snapped") || hasServerAdvancement(player, "lostinfog:snapped")) {
                        player.displayClientMessage(Component.literal("The cassette is broken and won't turn on"), true);
                        return;
                    }
                    
                    ResourceKey<Level> fogDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("lostinfog:the_fog_forest"));
                    ServerLevel fogLevel = player.server.getLevel(fogDim);
                    if (fogLevel != null) {
                        BlockPos campSpawn = StartSpawnHandler.ensureCampGenerated(fogLevel);
                        if (campSpawn == null) campSpawn = new BlockPos(0, 100, 0);
                        final BlockPos finalCampSpawn = campSpawn;

                        for (ServerPlayer p : player.server.getPlayerList().getPlayers()) {
                            CompoundTag pNbt = p.getPersistentData();
                            if (!pNbt.getBoolean("lostinfog_in_fog")) {
                                pNbt.putBoolean("lostinfog_in_fog", true);
                                pNbt.putBoolean("lostinfog_watching", false);
                                pNbt.putDouble("lostinfog_return_x", p.getX());
                                pNbt.putDouble("lostinfog_return_y", p.getY());
                                pNbt.putDouble("lostinfog_return_z", p.getZ());
                                pNbt.putString("lostinfog_return_dim", p.level().dimension().location().toString());
                                
                                net.minecraft.nbt.ListTag savedInv = new net.minecraft.nbt.ListTag();
                                p.getInventory().save(savedInv);
                                pNbt.put("lostinfog_saved_inventory", savedInv);
                                p.getInventory().clearContent();

                                pNbt.putLong("lostinfog_fog_enter_tick", fogLevel.getGameTime());
                                p.teleportTo(fogLevel, finalCampSpawn.getX() + 0.5, finalCampSpawn.getY(), finalCampSpawn.getZ() + 0.5, 0, 0);

                                p.setInvulnerable(true);
                                int untilTick = p.server.getTickCount() + 60;
                                p.server.tell(new net.minecraft.server.TickTask(untilTick, () -> {
                                    if (p.connection != null && !p.isRemoved()) {
                                        p.setInvulnerable(false);
                                    }
                                }));
                            }
                        }
                    }
                }
            }
        }

        private static final int FOG_TIME_LIMIT_TICKS = 20 * 60 * 5;

        @SubscribeEvent
        public static void onFogPlayerTick(PlayerTickEvent.Post event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (player.tickCount % 20 != 0) return;

                CompoundTag nbt = player.getPersistentData();
                if (!nbt.getBoolean("lostinfog_in_fog")) return;
                if (!"lostinfog:the_fog_forest".equals(player.level().dimension().location().toString())) return;

                long enterTick = nbt.getLong("lostinfog_fog_enter_tick");
                long elapsed = player.level().getGameTime() - enterTick;

                if (elapsed >= FOG_TIME_LIMIT_TICKS) {
                    returnFromFog(player, false);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                CompoundTag nbt = player.getPersistentData();
                if (nbt.getBoolean("lostinfog_in_fog") && "lostinfog:the_fog_forest".equals(player.level().dimension().location().toString())) {
                    event.setCanceled(true);
                    player.setHealth(player.getMaxHealth());
                    player.clearFire();
                    returnFromFog(player, true);
                }
            }
        }

        private static void returnFromFog(ServerPlayer player, boolean died) {
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("lostinfog_in_fog", false);

            if (died) {
                nbt.putBoolean("lostinfog:snapped", true);
                grantServerAdvancement(player, "lostinfog:snapped");
            }

            String dimString = nbt.getString("lostinfog_return_dim");
            if (!dimString.isEmpty()) {
                ResourceKey<Level> returnDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimString));
                ServerLevel returnLevel = player.server.getLevel(returnDim);
                if (returnLevel != null) {
                    player.teleportTo(returnLevel, nbt.getDouble("lostinfog_return_x"), nbt.getDouble("lostinfog_return_y"), nbt.getDouble("lostinfog_return_z"), player.getYRot(), player.getXRot());
                }
            }

            if (nbt.contains("lostinfog_saved_inventory")) {
                player.getInventory().load(nbt.getList("lostinfog_saved_inventory", 10));
                nbt.remove("lostinfog_saved_inventory");
            }
        }

        public static void handleWatchRequest(ServerPlayer player, BlockPos pos) {
            long gameTime = player.level().getGameTime();
            ActiveTvSession session = activeSessions.get(pos);

            if (session != null) {
                long age = gameTime - session.startTick;
                if (age >= 7200) {
                    activeSessions.remove(pos);
                    session = null;
                }
            }

            if (session == null) {
                int dayIdx = 1;
                if (hasServerAdvancement(player, "lostinfog:day_9")) dayIdx = 9;
                else if (hasServerAdvancement(player, "lostinfog:day_8")) dayIdx = 8;
                else if (hasServerAdvancement(player, "lostinfog:day_7")) dayIdx = 7;
                else if (hasServerAdvancement(player, "lostinfog:day_6")) dayIdx = 6;
                else if (hasServerAdvancement(player, "lostinfog:day_5")) dayIdx = 5;
                else if (hasServerAdvancement(player, "lostinfog:day_4")) dayIdx = 4;
                else if (hasServerAdvancement(player, "lostinfog:day_3")) dayIdx = 3;
                else if (hasServerAdvancement(player, "lostinfog:day_2")) dayIdx = 2;

                CompoundTag nbt = player.getPersistentData();
                if (nbt.getBoolean("lostinfog_watched_" + dayIdx)) {
                    player.displayClientMessage(Component.literal("The broadcast has ended..."), true);
                    return;
                }

                session = new ActiveTvSession(dayIdx, gameTime, pos);
                activeSessions.put(pos, session);

                SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "day" + dayIdx));
                player.serverLevel().playSound(null, pos, soundEvent, net.minecraft.sounds.SoundSource.RECORDS, 3.0F, 1.0F);

                List<ServerPlayer> nearby = player.serverLevel().getPlayers(p -> p.blockPosition().closerThan(pos, 10));
                for (ServerPlayer p : nearby) {
                    CompoundTag pNbt = p.getPersistentData();
                    if (!pNbt.getBoolean("lostinfog_watching")) {
                        session.currentWatchers.add(p.getUUID());
                        setupPlayerWatching(p, dayIdx, 7200);
                        int awareness = pNbt.getInt("lostinfog_awareness");
                        p.displayClientMessage(Component.literal("Searching for a signal and the correct channel... please wait"), true);
                        PacketDistributor.sendToPlayer(p, new OpenVideoPayload(pos, dayIdx, awareness, 0));
                    }
                }
            } else {
                CompoundTag nbt = player.getPersistentData();
                if (session.currentWatchers.contains(player.getUUID())) return;

                long age = gameTime - session.startTick;
                int remainingTicks = 7200 - (int) age;
                int startFrame = (int) (age * 25 / 20);

                session.currentWatchers.add(player.getUUID());
                setupPlayerWatching(player, session.day, remainingTicks);

                int awareness = nbt.getInt("lostinfog_awareness");
                player.displayClientMessage(Component.literal("Connecting to the active broadcast..."), true);
                PacketDistributor.sendToPlayer(player, new OpenVideoPayload(pos, session.day, awareness, startFrame));
            }
        }

        private static void setupPlayerWatching(ServerPlayer player, int day, int timer) {
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("lostinfog_watching", true);
            nbt.putInt("lostinfog_timer", timer);
            nbt.putInt("lostinfog_current_watching_day", day);
            nbt.putDouble("lostinfog_x", player.getX());
            nbt.putDouble("lostinfog_y", player.getY());
            nbt.putDouble("lostinfog_z", player.getZ());
            if (!nbt.contains("lostinfog_awareness")) nbt.putInt("lostinfog_awareness", 0);
        }

        public static void handleVideoFinish(ServerPlayer player, BlockPos pos, boolean skipped) {
            CompoundTag nbt = player.getPersistentData();
            if (nbt.getBoolean("lostinfog_watching")) {
                int currentDay = nbt.getInt("lostinfog_current_watching_day");
                nbt.putBoolean("lostinfog_watched_" + currentDay, true);
                nbt.putBoolean("lostinfog_watching", false);
                player.setInvulnerable(false);

                if (!skipped) {
                    int aw = nbt.getInt("lostinfog_awareness") - 3;
                    nbt.putInt("lostinfog_awareness", Math.max(0, aw));
                }

                ActiveTvSession session = activeSessions.get(pos);
                if (session != null) {
                    session.currentWatchers.remove(player.getUUID());
                    if (session.currentWatchers.isEmpty()) {
                        activeSessions.remove(pos);
                        List<ServerPlayer> nearby = player.serverLevel().getPlayers(p -> p.blockPosition().closerThan(pos, 20));
                        for (ServerPlayer p : nearby) PacketDistributor.sendToPlayer(p, new StopWorldSoundPayload(session.day));
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                CompoundTag nbt = player.getPersistentData();

                if (player.tickCount % 20 == 0) {
                    long gameTime = player.level().getGameTime();
                    activeSessions.entrySet().removeIf(entry -> (gameTime - entry.getValue().startTick) >= 7200);
                }

                if (nbt.getBoolean("lostinfog_watching")) {
                    if (player.swinging && player.swingingArm == net.minecraft.world.InteractionHand.OFF_HAND) {
                        BlockPos pos = null;
                        for (Map.Entry<BlockPos, ActiveTvSession> entry : activeSessions.entrySet()) {
                            if (entry.getValue().currentWatchers.contains(player.getUUID())) {
                                pos = entry.getKey();
                                break;
                            }
                        }
                        if (pos != null) handleVideoFinish(player, pos, true);
                        else nbt.putBoolean("lostinfog_watching", false);
                        player.swinging = false;
                    } else {
                        int timer = nbt.getInt("lostinfog_timer");
                        if (timer <= 0) {
                            BlockPos pos = null;
                            for (Map.Entry<BlockPos, ActiveTvSession> entry : activeSessions.entrySet()) {
                                if (entry.getValue().currentWatchers.contains(player.getUUID())) {
                                    pos = entry.getKey();
                                    break;
                                }
                            }
                            if (pos != null) handleVideoFinish(player, pos, false);
                            else nbt.putBoolean("lostinfog_watching", false);
                        } else {
                            nbt.putInt("lostinfog_timer", timer - 1);
                            player.setInvulnerable(true);
                            player.teleportTo(nbt.getDouble("lostinfog_x"), nbt.getDouble("lostinfog_y"), nbt.getDouble("lostinfog_z"));
                            player.setDeltaMovement(0, 0, 0);
                        }
                    }
                }

                if (player.tickCount % 20 == 0) {
                    long currentWorldDay = player.level().getDayTime() / 24000L;
                    if (!nbt.contains("lostinfog_last_day")) {
                        nbt.putLong("lostinfog_last_day", currentWorldDay);
                        grantServerAdvancement(player, "lostinfog:day_1");
                    }
                    long lastDay = nbt.getLong("lostinfog_last_day");
                    if (currentWorldDay > lastDay) {
                        nbt.putLong("lostinfog_last_day", currentWorldDay);
                        int aw = nbt.getInt("lostinfog_awareness") + 3;
                        nbt.putInt("lostinfog_awareness", Math.min(10, aw));
                        if (!hasServerAdvancement(player, "lostinfog:day_9")) {
                            if (hasServerAdvancement(player, "lostinfog:day_8")) grantServerAdvancement(player, "lostinfog:day_9");
                            else if (hasServerAdvancement(player, "lostinfog:day_7")) grantServerAdvancement(player, "lostinfog:day_8");
                            else if (hasServerAdvancement(player, "lostinfog:day_6")) grantServerAdvancement(player, "lostinfog:day_7");
                            else if (hasServerAdvancement(player, "lostinfog:day_5")) grantServerAdvancement(player, "lostinfog:day_6");
                            else if (hasServerAdvancement(player, "lostinfog:day_4")) grantServerAdvancement(player, "lostinfog:day_5");
                            else if (hasServerAdvancement(player, "lostinfog:day_3")) grantServerAdvancement(player, "lostinfog:day_4");
                            else if (hasServerAdvancement(player, "lostinfog:day_2")) grantServerAdvancement(player, "lostinfog:day_3");
                            else if (hasServerAdvancement(player, "lostinfog:day_1")) grantServerAdvancement(player, "lostinfog:day_2");
                        }
                    }
                }
            }
        }

        public static boolean hasServerAdvancement(ServerPlayer player, String id) {
            AdvancementHolder holder = player.getServer().getAdvancements().get(ResourceLocation.parse(id));
            return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
        }

        public static boolean isTvActiveNear(Player player) {
            if (player.getPersistentData().getBoolean("lostinfog_watching")) return true;
            long gameTime = player.level().getGameTime();
            for (Map.Entry<BlockPos, ActiveTvSession> entry : activeSessions.entrySet()) {
                if (gameTime - entry.getValue().startTick < 7200) {
                    if (!entry.getValue().currentWatchers.isEmpty() && entry.getKey().closerThan(player.blockPosition(), 10)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static void grantServerAdvancement(ServerPlayer player, String id) {
            AdvancementHolder holder = player.getServer().getAdvancements().get(ResourceLocation.parse(id));
            if (holder != null) {
                net.minecraft.advancements.AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
                if (!progress.isDone()) for (String criterion : progress.getRemainingCriteria()) player.getAdvancements().award(holder, criterion);
            }
        }
    }
}