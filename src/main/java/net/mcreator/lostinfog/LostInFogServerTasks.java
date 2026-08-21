package net.mcreator.lostinfog;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = "lostinfog")
public class LostInFogServerTasks {
    public static int serverDay = 1;
    public static int serverTicksActive = 0;
    public static int serverCount1 = 0;
    public static int serverCount2 = 0;
    public static boolean serverCompleted = false;
    public static boolean serverPhraseShown = false;
    public static boolean serverHudActive = false;

    public static int phraseDisplayTicks = 0;
    public static String currentPhrase = "";

    private static final int PHRASE_DELAY_TICKS = 1200;
    private static final int HUD_DELAY_TICKS = 1280;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        int detectedDay = 1;
        for (int d = 8; d >= 1; d--) {
            AdvancementHolder adv = server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("lostinfog", "day_" + d));
            if (adv != null) {
                boolean hasAdv = false;
                for (ServerPlayer sp : players) {
                    if (sp.getAdvancements().getOrStartProgress(adv).isDone()) {
                        hasAdv = true;
                        break;
                    }
                }
                if (hasAdv) {
                    detectedDay = d;
                    break;
                }
            }
        }

        if (detectedDay != serverDay) {
            if (detectedDay > serverDay && !serverCompleted && serverDay > 1) {
                for (ServerPlayer sp : players) {
                    if (sp.level().dimension() == Level.OVERWORLD) {
                        sp.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 1, false, false));
                        sp.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
                        sp.hurt(sp.damageSources().generic(), 6.0F);
                    }
                }
            }
            serverDay = detectedDay;
            serverTicksActive = 0;
            serverCount1 = 0;
            serverCount2 = 0;
            serverCompleted = false;
            serverPhraseShown = false;
            serverHudActive = false;
            currentPhrase = "";
            phraseDisplayTicks = 0;
        }

        boolean inOverworld = false;
        for (ServerPlayer sp : players) {
            if (sp.level().dimension() == Level.OVERWORLD) {
                inOverworld = true;
                break;
            }
        }

        if (!inOverworld) {
            serverHudActive = false;
            if (serverTicksActive % 20 == 0) syncToAll(server);
            return;
        }

        boolean tvActive = false;
        for (ServerPlayer sp : players) {
            if (sp.level().dimension() == Level.OVERWORLD && VideoMenu.ServerTracker.isTvActiveNear(sp)) {
                tvActive = true;
                break;
            }
        }

        if (!tvActive) {
            serverTicksActive++;
        }

        if (phraseDisplayTicks > 0) {
            phraseDisplayTicks--;
            for (ServerPlayer sp : players) {
                if (sp.level().dimension() == Level.OVERWORLD) {
                    sp.displayClientMessage(Component.literal(currentPhrase).withStyle(ChatFormatting.YELLOW), true);
                }
            }
        }

        if (serverDay == 1) {
            if (serverTicksActive % 20 == 0) syncToAll(server);
            return;
        }

        if (serverTicksActive == PHRASE_DELAY_TICKS && !serverPhraseShown) {
            String[] phrases = getPhrasesForDay(serverDay);
            currentPhrase = phrases[new Random().nextInt(phrases.length)];
            phraseDisplayTicks = 120;
            serverPhraseShown = true;
        }

        if (serverTicksActive >= HUD_DELAY_TICKS) {
            serverHudActive = true;
        }

        if (serverHudActive && !serverCompleted) {
            if (serverDay == 4) {
                if (serverTicksActive >= HUD_DELAY_TICKS + 24000) {
                    serverCompleted = true;
                    syncToAll(server);
                }
            } else if (serverDay == 6) {
                boolean radioFound = false;
                for (ServerPlayer sp : players) {
                    if (sp.level().dimension() == Level.OVERWORLD) {
                        for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                            ItemStack stack = sp.getInventory().getItem(i);
                            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                            if (itemId.equals(ResourceLocation.fromNamespaceAndPath("lostinfog", "radio"))) {
                                radioFound = true;
                                break;
                            }
                        }
                        if (radioFound) break;
                    }
                }
                if (radioFound) {
                    serverCompleted = true;
                    syncToAll(server);
                }
            } else if (serverDay == 7) {
                boolean radioAdvDone = false;
                for (ServerPlayer sp : players) {
                    if (sp.level().dimension() == Level.OVERWORLD) {
                        AdvancementHolder adv = server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("lostinfog", "radiotask"));
                        if (adv != null && sp.getAdvancements().getOrStartProgress(adv).isDone()) {
                            radioAdvDone = true;
                            break;
                        }
                    }
                }
                if (radioAdvDone) {
                    serverCompleted = true;
                    syncToAll(server);
                }
            } else if (serverDay == 8) {
                boolean theEndDone = false;
                for (ServerPlayer sp : players) {
                    if (sp.level().dimension() == Level.OVERWORLD) {
                        AdvancementHolder adv = server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("lostinfog", "theend"));
                        if (adv != null && sp.getAdvancements().getOrStartProgress(adv).isDone()) {
                            theEndDone = true;
                            break;
                        }
                    }
                }
                if (theEndDone) {
                    serverCompleted = true;
                    syncToAll(server);
                }
            } else if (checkTaskCompletion(players.size())) {
                serverCompleted = true;
                syncToAll(server);
            }
        }

        if (serverTicksActive % 20 == 0) {
            syncToAll(server);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        if (!player.level().dimension().equals(Level.OVERWORLD)) return;
        if (serverCompleted || !serverHudActive) return;

        int playerCount = player.getServer().getPlayerCount();
        BlockState bs = event.getState();
        boolean changed = false;

        if (serverDay == 2) {
            if (bs.is(BlockTags.LOGS)) {
                if (serverCount1 < 5 * playerCount) { serverCount1++; changed = true; }
            } else if (bs.is(Blocks.STONE) || bs.is(Blocks.COBBLESTONE) || bs.is(Blocks.DEEPSLATE)) {
                if (serverCount2 < 15 * playerCount) { serverCount2++; changed = true; }
            }
            if (checkTaskCompletion(playerCount)) {
                serverCompleted = true;
                syncToAll(player.getServer());
            } else if (changed) {
                syncToAll(player.getServer());
            }
        } else if (serverDay == 3) {
            if (bs.is(Blocks.IRON_ORE) || bs.is(Blocks.DEEPSLATE_IRON_ORE)) {
                if (serverCount1 < 5 * playerCount) {
                    serverCount1++;
                    changed = true;
                }
            }
            if (checkTaskCompletion(playerCount)) {
                serverCompleted = true;
                syncToAll(player.getServer());
            } else if (changed) {
                syncToAll(player.getServer());
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!player.level().dimension().equals(Level.OVERWORLD)) return;
        if (serverCompleted || !serverHudActive) return;

        int playerCount = player.getServer().getPlayerCount();
        if (serverDay == 5) {
            if (serverCount1 < 10 * playerCount) {
                serverCount1++;
                if (checkTaskCompletion(playerCount)) {
                    serverCompleted = true;
                    syncToAll(player.getServer());
                } else {
                    syncToAll(player.getServer());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!player.level().dimension().equals(Level.OVERWORLD)) return;
        if (serverCompleted || !serverHudActive) return;

        int playerCount = player.getServer().getPlayerCount();
        EntityType<?> t = event.getEntity().getType();

        if (serverDay == 3) {
            if (t == EntityType.COW || t == EntityType.PIG || t == EntityType.SHEEP || t == EntityType.CHICKEN || t == EntityType.RABBIT || t == EntityType.SALMON || t == EntityType.COD) {
                if (serverCount2 < 10 * playerCount) {
                    serverCount2++;
                    if (checkTaskCompletion(playerCount)) {
                        serverCompleted = true;
                        syncToAll(player.getServer());
                    } else {
                        syncToAll(player.getServer());
                    }
                }
            }
        }
    }

    private static void syncToAll(MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(new LostInFogClientTasks.SyncPacket(serverDay, serverTicksActive, serverCount1, serverCount2, serverCompleted, server.getPlayerCount(), serverHudActive));
    }

    private static String[] getPhrasesForDay(int day) {
        return switch (day) {
            case 2 -> new String[]{
                "Today I want to chop down a tree and mine some stone, 5 of each and 15 of each",
                "Today it's worth chopping a tree and getting stone",
                "I want to chop some wood and mine some stone",
                "It would be nice to chop a tree and get stone"
            };
            case 3 -> new String[]{"Today I need to get 5 iron ore and 10 food"};
            case 4 -> new String[]{"Today it's dangerous to go outside"};
            case 5 -> new String[]{
                "I think today is worth preparing for something?",
                "We need to prepare today, maybe place some blocks",
                "I should get ready for whatever is coming",
                "Time to set up defenses and place blocks"
            };
            case 6 -> new String[]{"Find the radio in the house"};
            case 7 -> new String[]{"Listen to the instructions on the radio"};
            case 8 -> new String[]{"Survive the Fog"};
            default -> new String[]{"Survive the day"};
        };
    }

    private static boolean checkTaskCompletion(int playerCount) {
        return switch (serverDay) {
            case 2 -> serverCount1 >= 5 * playerCount && serverCount2 >= 15 * playerCount;
            case 3 -> serverCount1 >= 5 * playerCount && serverCount2 >= 10 * playerCount;
            case 4 -> serverTicksActive >= HUD_DELAY_TICKS + 24000;
            case 5 -> serverCount1 >= 10 * playerCount;
            case 6 -> serverCompleted;
            case 7 -> serverCompleted;
            case 8 -> false;
            default -> false;
        };
    }
}