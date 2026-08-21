package net.mcreator.lostinfog;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.mcreator.ffmpeglib.FFmpegOrchestrator;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@EventBusSubscriber(modid = "lostinfog", value = Dist.CLIENT)
public class MainMenuVideo {
    private static boolean hasPlayedIntro = false;
    private static boolean preparingVideo = false;
    public static volatile boolean isVideoReady = false;

    private static final Path baseConfigDir;
    private static final Path ffmpegExe;
    private static final Path cadriDir;
    private static final Path videoDir;

    static {
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
        baseConfigDir = gameDir.resolve("config/lostinfog");
        ffmpegExe = gameDir.resolve("config/ffmpeglib/ffmpeg.exe");
        cadriDir = baseConfigDir.resolve("cadri/menuvideo");
        videoDir = baseConfigDir.resolve("analog");
    }

    @SubscribeEvent
    public static void onMenuOpen(ScreenEvent.Opening event) {
        if (!(event.getScreen() instanceof TitleScreen) || hasPlayedIntro) {
            return;
        }

        event.setCanceled(true);
        hasPlayedIntro = true;

        try {
            Files.createDirectories(baseConfigDir);
            Files.createDirectories(cadriDir);
            Files.createDirectories(videoDir);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Minecraft.getInstance().setScreen(new IntroVideoScreen());

        if (!preparingVideo) {
            preparingVideo = true;
            isVideoReady = false;

            CompletableFuture.runAsync(() -> {
                try {
                    if (!Files.exists(ffmpegExe) || !Files.isRegularFile(ffmpegExe)) {
                        return;
                    }

                    Path videoFile = videoDir.resolve("pornovideojava.mp4");

                    if (!Files.exists(videoFile) || Files.size(videoFile) == 0) {
                        extractVideoResource(videoFile);
                    }

                    if (!Files.exists(videoFile) || Files.size(videoFile) == 0) {
                        return;
                    }

                    Path firstFrame = cadriDir.resolve("frame_0001.png");

                    if (!Files.exists(firstFrame) || Files.size(firstFrame) == 0) {
                        deleteOldFrames();
                        FFmpegOrchestrator.extractFrames(
                                ffmpegExe.toFile(),
                                videoFile.toFile(),
                                cadriDir.toFile(),
                                25
                        ).join();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    preparingVideo = false;
                    isVideoReady = true;
                }
            });
        }
    }

    private static void extractVideoResource(Path videoFile) throws Exception {
        String resourcePath = "/assets/lostinfog/startvideo/pornovideojava.mp4";
        try (InputStream input = MainMenuVideo.class.getResourceAsStream(resourcePath)) {
            if (input != null) {
                Files.copy(input, videoFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void deleteOldFrames() {
        if (!Files.exists(cadriDir)) {
            return;
        }
        
        try (Stream<Path> paths = Files.list(cadriDir)) {
            paths.filter(p -> p.toString().toLowerCase().endsWith(".png"))
                 .forEach(p -> {
                     try {
                         Files.deleteIfExists(p);
                     } catch (Exception ignored) {}
                 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class IntroVideoScreen extends Screen {
        private static final int FPS = 25;
        private static final long FRAME_DURATION = 1000L / FPS;
        private static final long SKIP_HOLD_TIME = 2000L;
        private static final long HINT_DELAY = 5000L;
        private static final long HINT_DURATION = 5000L;
        private static final long HINT_FADE_TIME = 700L;
        private static final int HINT_MAX_ALPHA = 0x70;

        private int currentFrame = 0;
        private int lastLoadedFrame = -1;
        private long videoStartTime = -1;
        private long skipStartTime = -1;
        private long hintStartTime = -1;

        private boolean videoFinished = false;
        private boolean skipping = false;
        private boolean soundStarted = false;

        private DynamicTexture dynamicTexture;
        private int textureWidth;
        private int textureHeight;

        private SimpleSoundInstance videoSound;

        private final ResourceLocation videoTexture = ResourceLocation.fromNamespaceAndPath("lostinfog", "intro_video");

        protected IntroVideoScreen() {
            super(Component.literal("Video"));
        }

        @Override
        protected void init() {
            super.init();
            currentFrame = 0;
            lastLoadedFrame = -1;
            videoStartTime = -1;
            skipStartTime = -1;
            hintStartTime = -1;
            videoFinished = false;
            skipping = false;
            soundStarted = false;
            videoSound = null;
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
            long now = System.currentTimeMillis();
            g.fill(0, 0, width, height, 0xFF000000);

            if (videoFinished) {
                return;
            }

            if (!MainMenuVideo.isVideoReady) {
                String text = "Loading video...";
                int textWidth = Minecraft.getInstance().font.width(text);
                g.drawString(Minecraft.getInstance().font, text, (width - textWidth) / 2, height / 2, 0xFFFFFFFF, false);
                return;
            }

            if (videoStartTime == -1) {
                videoStartTime = now;
                hintStartTime = now;
                startVideoSound();
            }

            long elapsed = now - videoStartTime;
            currentFrame = (int)(elapsed / FRAME_DURATION);

            if (!loadFrame(currentFrame)) {
                if (currentFrame > 10) {
                    finishVideo();
                }
                return;
            }

            if (dynamicTexture != null) {
                renderVideo(g);
            }

            drawSkipText(g, now);
        }

        private void startVideoSound() {
            if (soundStarted) {
                return;
            }
            SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "startsoundvideo"));
            videoSound = SimpleSoundInstance.forUI(soundEvent, 1.0F);
            Minecraft.getInstance().getSoundManager().play(videoSound);
            soundStarted = true;
        }

        private void stopVideoSound() {
            if (videoSound != null) {
                Minecraft.getInstance().getSoundManager().stop(videoSound);
                videoSound = null;
            }
            soundStarted = false;
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

        private void drawSkipText(GuiGraphics g, long now) {
            int alpha;

            if (skipping) {
                long heldTime = now - skipStartTime;
                if (heldTime >= SKIP_HOLD_TIME) {
                    finishVideo();
                    return;
                }
                alpha = HINT_MAX_ALPHA;
                String textString = "Skipping: " + (heldTime / 1000L) + "s / 2s";
                drawCenteredText(g, textString, alpha);
                return;
            }

            long hintElapsed = now - hintStartTime;

            if (hintElapsed < HINT_DELAY) {
                return;
            }

            long visibleElapsed = hintElapsed - HINT_DELAY;

            if (visibleElapsed >= HINT_DURATION) {
                return;
            }

            if (visibleElapsed < HINT_FADE_TIME) {
                float progress = (float) visibleElapsed / (float) HINT_FADE_TIME;
                progress = Math.max(0.0F, Math.min(1.0F, progress));
                progress = progress * progress * (3.0F - 2.0F * progress);
                alpha = (int)(HINT_MAX_ALPHA * progress);
            } else if (visibleElapsed > HINT_DURATION - HINT_FADE_TIME) {
                float progress = (float)(HINT_DURATION - visibleElapsed) / (float) HINT_FADE_TIME;
                progress = Math.max(0.0F, Math.min(1.0F, progress));
                progress = progress * progress * (3.0F - 2.0F * progress);
                alpha = (int)(HINT_MAX_ALPHA * progress);
            } else {
                alpha = HINT_MAX_ALPHA;
            }

            drawCenteredText(g, "Hold SPACE to skip", alpha);
        }

        private void drawCenteredText(GuiGraphics g, String textString, int alpha) {
            if (alpha <= 4) {
                return;
            }
            int clampedAlpha = Math.min(255, Math.max(4, alpha));
            Component text = Component.literal(textString);
            int textWidth = Minecraft.getInstance().font.width(text);
            int x = (width - textWidth) / 2;
            int y = height - 40;
            int color = (clampedAlpha << 24) | 0x00FFFFFF;
            g.drawString(Minecraft.getInstance().font, text, x, y, color, false);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == InputConstants.KEY_SPACE) {
                if (!skipping) {
                    skipping = true;
                    skipStartTime = System.currentTimeMillis();
                }
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            if (keyCode == InputConstants.KEY_SPACE) {
                skipping = false;
                skipStartTime = -1;
                return true;
            }
            return super.keyReleased(keyCode, scanCode, modifiers);
        }

        private void finishVideo() {
            if (videoFinished) {
                return;
            }
            videoFinished = true;
            hasPlayedIntro = true;
            stopVideoSound();

            if (dynamicTexture != null) {
                Minecraft.getInstance().getTextureManager().release(videoTexture);
                dynamicTexture.close();
                dynamicTexture = null;
            }

            Minecraft.getInstance().setScreen(new TitleScreen(false));
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
            stopVideoSound();
            if (dynamicTexture != null) {
                Minecraft.getInstance().getTextureManager().release(videoTexture);
                dynamicTexture.close();
                dynamicTexture = null;
            }
            super.removed();
        }
    }
}