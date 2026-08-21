package net.mcreator.lostinfog;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.minecraft.advancements.AdvancementHolder;

@EventBusSubscriber(modid = "lostinfog")
public class IntroEventHandler {

    private static CompoundTag getPlayerData(Player player) {
        CompoundTag rootData = player.getPersistentData();
        if (!rootData.contains("PlayerPersisted")) {
            rootData.put("PlayerPersisted", new CompoundTag());
        }
        return rootData.getCompound("PlayerPersisted");
    }

    @EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("lostinfog");
            registrar.playToClient(StartGamePayload.TYPE, StartGamePayload.STREAM_CODEC, StartGamePayload::handleClient);
            registrar.playToClient(IntroLockPayload.TYPE, IntroLockPayload.STREAM_CODEC, IntroLockPayload::handleClient);
            registrar.playToServer(SkipIntroPayload.TYPE, SkipIntroPayload.STREAM_CODEC, SkipIntroPayload::handleServer);
        }
    }

    public record StartGamePayload() implements CustomPacketPayload {
        public static final Type<StartGamePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "start_game"));
        public static final StreamCodec<FriendlyByteBuf, StartGamePayload> STREAM_CODEC = StreamCodec.unit(new StartGamePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(final StartGamePayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                ClientProxy.introLocked = true;
                ClientProxy.openStartScreen();
            });
        }
    }

    public record IntroLockPayload(boolean locked) implements CustomPacketPayload {
        public static final Type<IntroLockPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "intro_lock"));
        public static final StreamCodec<FriendlyByteBuf, IntroLockPayload> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public IntroLockPayload decode(FriendlyByteBuf buf) {
                return new IntroLockPayload(buf.readBoolean());
            }

            @Override
            public void encode(FriendlyByteBuf buf, IntroLockPayload payload) {
                buf.writeBoolean(payload.locked());
            }
        };

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleClient(final IntroLockPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                ClientProxy.introLocked = payload.locked();

                if (!payload.locked()) {
                    ClientProxy.releaseMovementKeys();
                }
            });
        }
    }

    public record SkipIntroPayload() implements CustomPacketPayload {
        public static final Type<SkipIntroPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lostinfog", "skip_intro"));
        public static final StreamCodec<FriendlyByteBuf, SkipIntroPayload> STREAM_CODEC = StreamCodec.unit(new SkipIntroPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleServer(final SkipIntroPayload payload, final IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                CompoundTag data = getPlayerData(player);
                if (data.getInt("lostinfog_state") == 1) {
                    data.putInt("lostinfog_state", 2);
                    data.putInt("lostinfog_timer", 40);
                    PacketDistributor.sendToPlayer((ServerPlayer) player, new IntroLockPayload(true));
                }
            });
        }
    }

    @EventBusSubscriber(modid = "lostinfog", value = Dist.CLIENT)
    public static class ClientProxy {
        private static SimpleSoundInstance currentSound;
        private static int soundStopTimer = -1;
        private static boolean introLocked = false;

        public static void openStartScreen() {
            Minecraft.getInstance().setScreen(new StartGameScreen());
        }

        private static void releaseMovementKeys() {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.options == null) {
                return;
            }

            minecraft.options.keyUp.setDown(false);
            minecraft.options.keyDown.setDown(false);
            minecraft.options.keyLeft.setDown(false);
            minecraft.options.keyRight.setDown(false);
            minecraft.options.keyJump.setDown(false);
            minecraft.options.keyShift.setDown(false);
            minecraft.options.keySprint.setDown(false);

            KeyMapping.setAll();
        }

        private static void lockMovementKeys() {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.options == null) {
                return;
            }

            minecraft.options.keyUp.setDown(false);
            minecraft.options.keyDown.setDown(false);
            minecraft.options.keyLeft.setDown(false);
            minecraft.options.keyRight.setDown(false);
            minecraft.options.keyJump.setDown(false);
            minecraft.options.keyShift.setDown(false);
            minecraft.options.keySprint.setDown(false);
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (introLocked) {
                lockMovementKeys();
            }

            if (soundStopTimer > 0) {
                soundStopTimer--;

                if (soundStopTimer == 0) {
                    if (currentSound != null) {
                        Minecraft.getInstance().getSoundManager().stop(currentSound);
                        currentSound = null;
                    }

                    soundStopTimer = -1;
                }
            }
        }
    }

    public static class StartGameScreen extends Screen {
        private static final ResourceLocation TEXTURE1 = ResourceLocation.fromNamespaceAndPath("lostinfog", "textures/screens/startgame1.png");
        private static final ResourceLocation TEXTURE2 = ResourceLocation.fromNamespaceAndPath("lostinfog", "textures/screens/startgame2.png");

        private int subState = 0;
        private int blackScreenTimer = 0;

        public StartGameScreen() {
            super(Component.literal("Start"));
        }

        @Override
        protected void init() {
            super.init();
            this.playIntroSound();
        }

        private void playIntroSound() {
            if (subState != 1 && (ClientProxy.currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(ClientProxy.currentSound))) {
                ClientProxy.currentSound = SimpleSoundInstance.forUI(
                    BuiltInRegistries.SOUND_EVENT.get(
                        ResourceLocation.fromNamespaceAndPath("lostinfog", "startgame")
                    ),
                    1.0F
                );

                Minecraft.getInstance().getSoundManager().play(ClientProxy.currentSound);
            }
        }

        @Override
        public void tick() {
            super.tick();

            if (subState == 1) {
                blackScreenTimer--;

                if (blackScreenTimer <= 0) {
                    subState = 2;
                }
            } else {
                this.playIntroSound();
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (subState == 0) {
                guiGraphics.blit(
                    TEXTURE1,
                    0,
                    0,
                    0,
                    0,
                    this.width,
                    this.height,
                    this.width,
                    this.height
                );
            } else if (subState == 1) {
                guiGraphics.fill(
                    0,
                    0,
                    this.width,
                    this.height,
                    0xFF000000
                );
            } else if (subState == 2) {
                guiGraphics.blit(
                    TEXTURE2,
                    0,
                    0,
                    0,
                    0,
                    this.width,
                    this.height,
                    this.width,
                    this.height
                );
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == InputConstants.KEY_SPACE) {
                if (subState == 0) {
                    subState = 1;
                    blackScreenTimer = 20;

                    SimpleSoundInstance shelkSound = SimpleSoundInstance.forUI(
                        BuiltInRegistries.SOUND_EVENT.get(
                            ResourceLocation.fromNamespaceAndPath("lostinfog", "startgameshelk")
                        ),
                        1.0F
                    );

                    Minecraft.getInstance().getSoundManager().play(shelkSound);
                    return true;
                } else if (subState == 2) {
                    PacketDistributor.sendToServer(new SkipIntroPayload());
                    ClientProxy.soundStopTimer = 40;
                    this.onClose();
                    return true;
                }
            }

            return false;
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

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            CompoundTag data = getPlayerData(player);

            if (!data.getBoolean("lostinfog_intro_done")) {
                data.putBoolean("lostinfog_intro_done", true);
                data.putInt("lostinfog_state", 1);
                data.putInt("lostinfog_timer", 0);

                ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);

                if (overworld != null) {
                    overworld.setWeatherParameters(0, 12000, true, true);
                    overworld.setDayTime(18000);
                }

                serverLevel.getServer().setDifficulty(Difficulty.PEACEFUL, true);

                PacketDistributor.sendToPlayer(
                    (ServerPlayer) player,
                    new StartGamePayload()
                );

                PacketDistributor.sendToPlayer(
                    (ServerPlayer) player,
                    new IntroLockPayload(true)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CompoundTag data = getPlayerData(player);
            int state = data.getInt("lostinfog_state");
            int timer = data.getInt("lostinfog_timer");

            if (state == 8 && player.level().dimension() == Level.OVERWORLD) {
                if (!player.isSleeping() && player.level() instanceof ServerLevel serverLevel) {
                    long dayTime = serverLevel.getDayTime() % 24000;

                    if (dayTime >= 13000 && dayTime <= 23000) {
                        serverLevel.setDayTime(18000);
                    }
                }
            }

            boolean introLocked = (state >= 1 && state <= 6) || (state == 7 && timer > 60);

            if (introLocked) {
                player.addEffect(
                    new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        10,
                        255,
                        false,
                        false
                    )
                );

                player.setDeltaMovement(0, 0, 0);
                player.setSprinting(false);
                player.setJumping(false);
            }

            if (state == 2) {
                if (timer > 0) {
                    data.putInt("lostinfog_timer", timer - 1);
                } else {
                    data.putInt("lostinfog_state", 3);
                    data.putInt("lostinfog_timer", 100);
                }
            } else if (state == 3) {
                player.displayClientMessage(
                    Component.literal("By the time I got here, the fog had become very thick"),
                    true
                );

                if (timer > 0) {
                    data.putInt("lostinfog_timer", timer - 1);
                } else {
                    player.displayClientMessage(Component.literal(""), true);
                    data.putInt("lostinfog_state", 4);
                    data.putInt("lostinfog_timer", 40);
                }
            } else if (state == 4) {
                if (timer > 0) {
                    data.putInt("lostinfog_timer", timer - 1);
                } else {
                    data.putInt("lostinfog_state", 5);
                    data.putInt("lostinfog_timer", 80);
                }
            } else if (state == 5) {
                player.displayClientMessage(
                    Component.literal("It's very dangerous to go outside right now"),
                    true
                );

                if (timer > 0) {
                    data.putInt("lostinfog_timer", timer - 1);
                } else {
                    player.displayClientMessage(Component.literal(""), true);
                    data.putInt("lostinfog_state", 6);
                    data.putInt("lostinfog_timer", 40);
                }
            } else if (state == 6) {
                if (timer > 0) {
                    data.putInt("lostinfog_timer", timer - 1);
                } else {
                    data.putInt("lostinfog_state", 7);
                    data.putInt("lostinfog_timer", 120);
                }
            } else if (state == 7) {
                player.displayClientMessage(
                    Component.literal("I should watch some TV and go to bed, I'll look everything over in the morning"),
                    true
                );

                if (timer > 0) {
                    data.putInt("lostinfog_timer", timer - 1);
                } else {
                    player.displayClientMessage(Component.literal(""), true);
                    data.putInt("lostinfog_state", 8);
                    data.putInt("lostinfog_timer", 0);
                    data.putBoolean("lostinfog_debuffs_active", true);

                    PacketDistributor.sendToPlayer(
                        serverPlayer,
                        new IntroLockPayload(false)
                    );
                }
            } else if (state == 8) {
                if (hasServerAdvancement(serverPlayer, "lostinfog:day_1")
                    && !hasServerAdvancement(serverPlayer, "lostinfog:day_2")) {

                    if (data.getBoolean("lostinfog_watched_1")) {
                        int tvTimer = data.getInt("lostinfog_tv_msg_timer");

                        if (tvTimer == 120) {
                            player.displayClientMessage(
                                Component.literal("Well, now I can go to sleep"),
                                true
                            );
                        } else if (tvTimer == 200) {
                            player.displayClientMessage(
                                Component.literal(""),
                                true
                            );
                        }

                        data.putInt("lostinfog_tv_msg_timer", tvTimer + 1);
                    }
                }
            } else if (state == 9) {
                if (timer == 0) {
                    player.displayClientMessage(
                        Component.literal("Press R to turn on the flashlight"),
                        true
                    );
                } else if (timer == 60) {
                    player.displayClientMessage(
                        Component.literal(""),
                        true
                    );
                } else if (timer == 300) {
                    player.displayClientMessage(
                        Component.literal("Well, time to check mail..."),
                        true
                    );
                } else if (timer == 400) {
                    player.displayClientMessage(
                        Component.literal(""),
                        true
                    );
                } else if (timer == 860) {
                    player.sendSystemMessage(
                        Component.literal("Lost in fog 1.0.1 - Made by FLYover67.")
                    );

                    player.displayClientMessage(
                        Component.literal("Lost in fog 1.0.1 - Made by FLYover67."),
                        true
                    );

                    Component patreonComponent = Component.literal("Patreon")
                        .withStyle(style -> style
                            .withClickEvent(
                                new ClickEvent(
                                    ClickEvent.Action.OPEN_URL,
                                    "https://www.patreon.com/c/flyover67"
                                )
                            )
                            .withUnderlined(true)
                            .withColor(ChatFormatting.BLUE)
                        );

                    Component discordComponent = Component.literal("Discord")
                        .withStyle(style -> style
                            .withClickEvent(
                                new ClickEvent(
                                    ClickEvent.Action.OPEN_URL,
                                    "https://discord.gg/BpshSf8jhP"
                                )
                            )
                            .withUnderlined(true)
                            .withColor(ChatFormatting.BLUE)
                        );

                    Component linksMessage = Component.literal("")
                        .append(patreonComponent)
                        .append(Component.literal(" , "))
                        .append(discordComponent);

                    player.sendSystemMessage(linksMessage);
                } else if (timer == 960) {
                    player.displayClientMessage(
                        Component.literal(""),
                        true
                    );

                    player.removeEffect(MobEffects.WEAKNESS);
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

                    data.putInt("lostinfog_state", 11);
                    data.putInt("lostinfog_timer", 0);

                    return;
                }

                data.putInt("lostinfog_timer", timer + 1);
            }

            if (data.getBoolean("lostinfog_debuffs_active") && state == 8) {
                if (player.tickCount % 40 == 0) {
                    player.addEffect(
                        new MobEffectInstance(
                            MobEffects.WEAKNESS,
                            100,
                            199,
                            false,
                            false
                        )
                    );

                    player.addEffect(
                        new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            100,
                            0,
                            false,
                            false
                        )
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide()) {
            CompoundTag data = getPlayerData(player);

            if (data.getInt("lostinfog_state") == 8) {
                if (event.getAdvancement().id().toString().equalsIgnoreCase("lostinfog:day_2")) {
                    data.putInt("lostinfog_state", 9);
                    data.putInt("lostinfog_timer", 0);
                    data.putBoolean("lostinfog_debuffs_active", false);

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.getServer().setDifficulty(Difficulty.NORMAL, true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player != null && !player.level().isClientSide()) {
            CompoundTag data = getPlayerData(player);
            int state = data.getInt("lostinfog_state");
            int timer = data.getInt("lostinfog_timer");

            boolean introLocked =
                (state >= 1 && state <= 6)
                || (state == 7 && timer > 60);

            if (data.getBoolean("lostinfog_debuffs_active") || introLocked) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide()) {
            CompoundTag data = getPlayerData(player);
            int state = data.getInt("lostinfog_state");
            int timer = data.getInt("lostinfog_timer");

            boolean introLocked =
                (state >= 1 && state <= 6)
                || (state == 7 && timer > 60);

            if (data.getBoolean("lostinfog_debuffs_active") || introLocked) {
                BlockState stateBlock = player.level().getBlockState(event.getPos());

                if (stateBlock.getBlock() instanceof DoorBlock) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public static boolean hasServerAdvancement(ServerPlayer player, String id) {
        AdvancementHolder holder =
            player.getServer().getAdvancements().get(ResourceLocation.parse(id));

        if (holder != null) {
            return player.getAdvancements()
                .getOrStartProgress(holder)
                .isDone();
        }

        return false;
    }
}