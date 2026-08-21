package net.mcreator.lostinfog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnalogSurveyEvent {

    public record OpenSurveyPayload() implements CustomPacketPayload {
        public static final Type<OpenSurveyPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "open_survey"));

        public static final StreamCodec<io.netty.buffer.ByteBuf, OpenSurveyPayload> STREAM_CODEC =
                StreamCodec.unit(new OpenSurveyPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(OpenSurveyPayload payload, IPayloadContext context) {
            context.enqueueWork(() ->
                    Minecraft.getInstance().setScreen(new AnalogSurveyScreen())
            );
        }
    }

    public record SubmitSurveyAnswersPayload(String answer1, String answer2, String answer3) implements CustomPacketPayload {
        public static final Type<SubmitSurveyAnswersPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "submit_survey"));

        public static final StreamCodec<io.netty.buffer.ByteBuf, SubmitSurveyAnswersPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, SubmitSurveyAnswersPayload::answer1,
                        ByteBufCodecs.STRING_UTF8, SubmitSurveyAnswersPayload::answer2,
                        ByteBufCodecs.STRING_UTF8, SubmitSurveyAnswersPayload::answer3,
                        SubmitSurveyAnswersPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(SubmitSurveyAnswersPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    CompoundTag nbt = player.getPersistentData();
                    nbt.putString("lostinfog_survey_answer_1", payload.answer1());
                    nbt.putString("lostinfog_survey_answer_2", payload.answer2());
                    nbt.putString("lostinfog_survey_answer_3", payload.answer3());
                    nbt.putBoolean("lostinfog_survey_completed", true);
                    nbt.putLong("lostinfog_survey_completed_time", player.level().getGameTime());
                }
            });
        }
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusRegister {
        @SubscribeEvent
        public static void registerNetwork(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("lostinfog");
            registrar.playToClient(
                    OpenSurveyPayload.TYPE,
                    OpenSurveyPayload.STREAM_CODEC,
                    OpenSurveyPayload::handle
            );
            registrar.playToServer(
                    SubmitSurveyAnswersPayload.TYPE,
                    SubmitSurveyAnswersPayload.STREAM_CODEC,
                    SubmitSurveyAnswersPayload::handle
            );
        }
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME)
    public static class ServerHandler {

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            if (player.tickCount % 20 != 0) {
                return;
            }

            CompoundTag nbt = player.getPersistentData();

            if (!VideoMenu.ServerTracker.hasServerAdvancement(player, "lostinfog:day_4")) {
                return;
            }

            if (nbt.getBoolean("lostinfog_survey_triggered")) {
                return;
            }

            if (!nbt.getBoolean("lostinfog_survey_delayed")) {
                nbt.putBoolean("lostinfog_survey_delayed", true);
                nbt.putInt("lostinfog_survey_timer", 4800);
            }

            if (!VideoMenu.ServerTracker.isTvActiveNear(player)
                    && !nbt.getBoolean("lostinfog_watching")) {

                int timer = nbt.getInt("lostinfog_survey_timer");

                if (timer <= 0) {
                    nbt.putBoolean("lostinfog_survey_triggered", true);
                    PacketDistributor.sendToPlayer(player, new OpenSurveyPayload());
                } else {
                    nbt.putInt("lostinfog_survey_timer", timer - 20);
                }
            }
        }
    }

    public static class SurveyButton extends Button {

        private final int normalColor;
        private final int hoverColor;

        public SurveyButton(
                int x,
                int y,
                int width,
                int height,
                Component message,
                OnPress onPress,
                int normalColor,
                int hoverColor
        ) {
            super(
                    x,
                    y,
                    width,
                    height,
                    message,
                    onPress,
                    Button.DEFAULT_NARRATION
            );

            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
        }

        @Override
        protected void renderWidget(
                GuiGraphics g,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            int color = this.isHovered() ? hoverColor : normalColor;

            g.fill(
                    this.getX(),
                    this.getY(),
                    this.getX() + this.width,
                    this.getY() + this.height,
                    0xFF090909
            );

            g.fill(
                    this.getX(),
                    this.getY(),
                    this.getX() + this.width,
                    this.getY() + 1,
                    color
            );

            g.fill(
                    this.getX(),
                    this.getY() + this.height - 1,
                    this.getX() + this.width,
                    this.getY() + this.height,
                    color
            );

            g.fill(
                    this.getX(),
                    this.getY(),
                    this.getX() + 1,
                    this.getY() + this.height,
                    color
            );

            g.fill(
                    this.getX() + this.width - 1,
                    this.getY(),
                    this.getX() + this.width,
                    this.getY() + this.height,
                    color
            );

            int textColor = this.isHovered()
                    ? 0xFFFFFFFF
                    : 0xFFB8B8B8;

            g.drawCenteredString(
                    Minecraft.getInstance().font,
                    this.getMessage(),
                    this.getX() + this.width / 2,
                    this.getY() + (this.height - 8) / 2,
                    textColor
            );
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    public static class AnalogSurveyScreen extends Screen {

        private static final int INTRO_TIME = 100;
        private static final int PHOTO_TIME = 100;
        private static final int TOTAL_PHOTOS = 8;
        private static final int NOISE_TIME = 6;

        private static final String[] QUESTION_TITLES = {
                "SUBJECT IDENTIFICATION",
                "ENVIRONMENTAL RECORD",
                "AUDIO ANOMALY",
                "PROXIMITY REPORT",
                "TEMPORAL DISCREPANCY",
                "BEHAVIORAL FLAG"
        };

        private static final String[] QUESTION_TEXTS = {
                "Do you recognize this place?",
                "Have you seen this fog before?",
                "Can you hear something inside the fog?",
                "Has it come closer to your home?",
                "Did the recording date feel wrong to you?",
                "Have you dreamed about this location?"
        };

        private int stage = 0;
        private int timer = 0;
        private int currentPhoto = 1;
        private int question = 0;

        private int randomSoundTimer = 0;
        private int nextRandomSound = 120;

        private static final net.minecraft.sounds.SoundEvent TRANSITION_SOUND =
                net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath("lostinfog", "startgameshelk")
                );

        private int noiseTimer = 0;
        private int shakeX = 0;
        private int shakeY = 0;

        private int anomalyTimer = 0;
        private int nextAnomalyCheck = 140;

        private int subliminalTimer = 0;
        private boolean subliminalShown = false;

        private final int[] selectedQuestions = new int[3];
        private final List<String> answers = new ArrayList<>();

        private final Random random = new Random();

        public AnalogSurveyScreen() {
            super(Component.literal("Archive"));
        }

        @Override
        protected void init() {
            super.init();
            pickQuestions();
            rebuildWidgets();
            playTransitionSound(0.7F, 1.0F);
            nextRandomSound = 120 + random.nextInt(180);
        }

        private void pickQuestions() {
            List<Integer> pool = new ArrayList<>();
            for (int i = 0; i < QUESTION_TITLES.length; i++) {
                pool.add(i);
            }
            for (int i = 0; i < 3; i++) {
                int index = random.nextInt(pool.size());
                selectedQuestions[i] = pool.remove(index);
            }
        }

        @Override
        protected void rebuildWidgets() {
            this.clearWidgets();

            if (stage == 2 || stage == 3 || stage == 4) {
                int centerX = this.width / 2;
                int y = this.height / 2 + 65;

                this.addRenderableWidget(
                        new SurveyButton(
                                centerX - 170,
                                y,
                                100,
                                28,
                                Component.literal("YES"),
                                b -> answerQuestion("YES"),
                                0xFF7A1313,
                                0xFFB31C1C
                        )
                );

                this.addRenderableWidget(
                        new SurveyButton(
                                centerX - 50,
                                y,
                                100,
                                28,
                                Component.literal("NO"),
                                b -> answerQuestion("NO"),
                                0xFF5A1010,
                                0xFF981818
                        )
                );

                this.addRenderableWidget(
                        new SurveyButton(
                                centerX + 70,
                                y,
                                100,
                                28,
                                Component.literal("UNKNOWN"),
                                b -> answerQuestion("UNKNOWN"),
                                0xFF5A1010,
                                0xFF981818
                        )
                );
            }

            if (stage == 5) {
                int centerX = this.width / 2;

                this.addRenderableWidget(
                        new SurveyButton(
                                centerX - 60,
                                this.height - 65,
                                120,
                                28,
                                Component.literal("CLOSE ARCHIVE"),
                                b -> Minecraft.getInstance().setScreen(null),
                                0xFF7A1313,
                                0xFFB31C1C
                        )
                );
            }
        }

        private void answerQuestion(String answer) {
            answers.add(answer);
            question++;
            noiseTimer = NOISE_TIME;

            if (question >= 3) {
                stage = 5;
                rebuildWidgets();
                playTransitionSound(0.9F, 0.9F);
                sendAnswersToServer();
            } else {
                stage++;
                rebuildWidgets();
                playTransitionSound(
                        0.6F,
                        0.95F + random.nextFloat() * 0.15F
                );
            }

            timer = 0;
        }

        private void sendAnswersToServer() {
            String a1 = answers.size() > 0 ? answers.get(0) : "UNKNOWN";
            String a2 = answers.size() > 1 ? answers.get(1) : "UNKNOWN";
            String a3 = answers.size() > 2 ? answers.get(2) : "UNKNOWN";
            PacketDistributor.sendToServer(new SubmitSurveyAnswersPayload(a1, a2, a3));
        }

        @Override
        public void tick() {
            super.tick();

            timer++;
            randomSoundTimer++;

            if (random.nextInt(5) == 0) {
                shakeX = random.nextInt(2) - 1;
                shakeY = random.nextInt(2) - 1;
            } else {
                shakeX = 0;
                shakeY = 0;
            }

            if (noiseTimer > 0) {
                noiseTimer--;
            }

            if (anomalyTimer > 0) {
                anomalyTimer--;
            }

            if (stage == 0) {
                if (timer >= INTRO_TIME) {
                    stage = 1;
                    timer = 0;
                    currentPhoto = 1;
                    noiseTimer = NOISE_TIME;

                    playTransitionSound(0.9F, 1.0F);
                }

                return;
            }

            if (stage == 1) {
                if (timer >= PHOTO_TIME) {
                    timer = 0;
                    currentPhoto++;
                    noiseTimer = NOISE_TIME;
                    playTransitionSound(0.5F, 1.0F);

                    if (currentPhoto > TOTAL_PHOTOS) {
                        currentPhoto = 1;
                        stage = 2;
                        question = 0;

                        rebuildWidgets();

                        playTransitionSound(0.75F, 0.85F);
                    }
                }

                if (randomSoundTimer >= nextRandomSound) {
                    playCaveSound(
                            0.35F + random.nextFloat() * 0.35F,
                            0.65F + random.nextFloat() * 0.45F
                    );

                    randomSoundTimer = 0;
                    nextRandomSound = 100 + random.nextInt(280);
                }

                if (timer >= nextAnomalyCheck && anomalyTimer <= 0) {
                    nextAnomalyCheck = timer + 60 + random.nextInt(140);

                    if (random.nextInt(6) == 0) {
                        anomalyTimer = 6;
                        playCaveSound(1.0F, 0.3F + random.nextFloat() * 0.2F);
                    }
                }
            }

            if (stage == 5 && !subliminalShown) {
                subliminalTimer++;
                if (subliminalTimer == 14) {
                    subliminalShown = true;
                }
            }
        }

        private void playCaveSound(float volume, float pitch) {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.player != null) {
                minecraft.getSoundManager().play(
                        SimpleSoundInstance.forLocalAmbience(
                                SoundEvents.AMBIENT_CAVE.value(),
                                pitch,
                                volume
                        )
                );
            }
        }

        private void playTransitionSound(float volume, float pitch) {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.player != null) {
                minecraft.getSoundManager().play(
                        SimpleSoundInstance.forLocalAmbience(
                                TRANSITION_SOUND,
                                pitch,
                                volume
                        )
                );
            }
        }

        private void drawCenteredWithShadow(
                GuiGraphics g,
                String text,
                int x,
                int y,
                int color
        ) {
            g.drawCenteredString(
                    Minecraft.getInstance().font,
                    text,
                    x + 1,
                    y + 1,
                    0xAA000000
            );

            g.drawCenteredString(
                    Minecraft.getInstance().font,
                    text,
                    x,
                    y,
                    color
            );
        }

        private void drawScanlines(GuiGraphics g) {
            for (int y = 0; y < this.height; y += 3) {
                g.fill(0, y, this.width, y + 1, 0x14000000);
            }
        }

        private void drawVignette(GuiGraphics g) {
            int edge = 60;
            g.fill(0, 0, this.width, edge, 0x66000000);
            g.fill(0, this.height - edge, this.width, this.height, 0x66000000);
            g.fill(0, 0, edge, this.height, 0x44000000);
            g.fill(this.width - edge, 0, this.width, this.height, 0x44000000);
        }

        private void drawNoise(GuiGraphics g) {
            for (int i = 0; i < 140; i++) {
                int nx = random.nextInt(this.width);
                int ny = random.nextInt(this.height);
                int nw = 2 + random.nextInt(6);
                int nh = 1 + random.nextInt(3);
                int shade = 0x22 + random.nextInt(0x40);
                int color = (0xFF << 24) | (shade << 16) | (shade << 8) | shade;
                g.fill(nx, ny, nx + nw, ny + nh, color);
            }
        }

        @Override
        public void renderBackground(
                GuiGraphics g,
                int mouseX,
                int mouseY,
                float partialTicks
        ) {
        }

        @Override
        public void render(
                GuiGraphics g,
                int mouseX,
                int mouseY,
                float partialTicks
        ) {
            g.pose().pushPose();
            g.pose().translate(shakeX, shakeY, 0);

            g.fill(
                    0,
                    0,
                    this.width,
                    this.height,
                    0xFF020202
            );

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            if (noiseTimer > 0) {
                drawNoise(g);
                drawScanlines(g);
                g.pose().popPose();
                return;
            }

            if (anomalyTimer > 0) {
                g.fill(0, 0, this.width, this.height, 0x33CC1111);
            }

            if (stage == 0) {
                g.fill(
                        centerX - 310,
                        centerY - 100,
                        centerX + 310,
                        centerY + 100,
                        0xFF070707
                );

                g.fill(
                        centerX - 310,
                        centerY - 100,
                        centerX + 310,
                        centerY - 98,
                        0xFF8C1717
                );

                g.fill(
                        centerX - 310,
                        centerY + 98,
                        centerX + 310,
                        centerY + 100,
                        0xFF8C1717
                );

                drawCenteredWithShadow(
                        g,
                        "ARCHIVE INTERFERENCE",
                        centerX,
                        centerY - 48,
                        0xFFE04343
                );

                drawCenteredWithShadow(
                        g,
                        "ATMOSPHERIC RECORDING DATABASE",
                        centerX,
                        centerY - 20,
                        0xFFAAAAAA
                );

                drawCenteredWithShadow(
                        g,
                        "......",
                        centerX,
                        centerY + 18,
                        0xFF666666
                );

                drawCenteredWithShadow(
                        g,
                        "DO NOT ADJUST THE IMAGE",
                        centerX,
                        centerY + 43,
                        0xFF444444
                );
            }

            if (stage == 1) {
                ResourceLocation photoRL =
                        ResourceLocation.fromNamespaceAndPath(
                                "lostinfog",
                                "textures/screens/" + currentPhoto + ".png"
                        );

                int size =
                        Math.min(
                                this.width - 120,
                                this.height - 160
                        );

                if (size < 220) {
                    size = 220;
                }

                int left = centerX - size / 2;
                int top = centerY - size / 2 - 10;

                g.fill(
                        left - 8,
                        top - 8,
                        left + size + 8,
                        top + size + 8,
                        0xFF090909
                );

                g.fill(
                        left - 8,
                        top - 8,
                        left + size + 8,
                        top - 6,
                        0xFF681515
                );

                g.fill(
                        left - 8,
                        top + size + 6,
                        left + size + 8,
                        top + size + 8,
                        0xFF681515
                );

                g.blit(
                        photoRL,
                        left,
                        top,
                        0,
                        0,
                        size,
                        size,
                        size,
                        size
                );

                drawCenteredWithShadow(
                        g,
                        "ARCHIVE RECORD  " + currentPhoto + " / " + TOTAL_PHOTOS,
                        centerX,
                        top - 28,
                        0xFFB0B0B0
                );

                drawCenteredWithShadow(
                        g,
                        ".......",
                        centerX,
                        top + size + 18,
                        0xFF5A5A5A
                );
            }

            if (stage == 2 || stage == 3 || stage == 4) {
                g.fill(
                        centerX - 360,
                        centerY - 115,
                        centerX + 360,
                        centerY + 115,
                        0xFF080808
                );

                g.fill(
                        centerX - 360,
                        centerY - 115,
                        centerX - 358,
                        centerY + 115,
                        0xFF7E1616
                );

                g.fill(
                        centerX + 358,
                        centerY - 115,
                        centerX + 360,
                        centerY + 115,
                        0xFF7E1616
                );

                int qIndex = selectedQuestions[Math.min(question, 2)];
                String title = QUESTION_TITLES[qIndex];
                String questionText = QUESTION_TEXTS[qIndex];

                drawCenteredWithShadow(
                        g,
                        title,
                        centerX,
                        centerY - 76,
                        0xFFE04343
                );

                drawCenteredWithShadow(
                        g,
                        questionText,
                        centerX,
                        centerY - 38,
                        0xFFF0F0F0
                );

                drawCenteredWithShadow(
                        g,
                        "RESPONSE REQUIRED",
                        centerX,
                        centerY + 35,
                        0xFF555555
                );
            }

            if (stage == 5) {
                if (!subliminalShown && subliminalTimer >= 10 && subliminalTimer < 14) {
                    drawCenteredWithShadow(
                            g,
                            "IT KNOWS YOU ANSWERED",
                            centerX,
                            centerY - 55,
                            0xFFFF2222
                    );
                } else {
                    drawCenteredWithShadow(
                            g,
                            "ARCHIVE COMPLETE",
                            centerX,
                            centerY - 55,
                            0xFFE04343
                    );

                    drawCenteredWithShadow(
                            g,
                            "RECORDING STORED",
                            centerX,
                            centerY - 20,
                            0xFFAAAAAA
                    );

                    drawCenteredWithShadow(
                            g,
                            "NO FURTHER DATA AVAILABLE",
                            centerX,
                            centerY + 10,
                            0xFF555555
                    );
                }
            }

            drawScanlines(g);
            drawVignette(g);

            g.pose().popPose();

            super.render(g, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
