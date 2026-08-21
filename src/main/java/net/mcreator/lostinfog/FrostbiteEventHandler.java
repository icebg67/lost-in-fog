package net.mcreator.lostinfog;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = "lostinfog")
public class FrostbiteEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        
        if (level.isClientSide() || !level.dimension().location().toString().equals("lostinfog:the_fog_forest")) return;

        FrostbiteData data = player.getData(LostinfogModAttachments.FROSTBITE);
        boolean dataChanged = false;

        if (player.tickCount % 10 == 0) {
            BlockPos playerPos = player.blockPosition();
            boolean nearCampfire = false;
            CampfireData campfireData = level.getData(LostinfogModAttachments.CAMPFIRES);

            for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-5, -5, -5), playerPos.offset(5, 5, 5))) {
                BlockState state = level.getBlockState(pos);
                if ((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) && state.getValue(CampfireBlock.LIT)) {
                    nearCampfire = true;
                    if (!campfireData.timers.containsKey(pos.immutable())) {
                        campfireData.timers.put(pos.immutable(), level.random.nextInt(1200) + 1200);
                    }
                    break;
                }
            }

            if (nearCampfire) {
                data.freezeTimer = 0;
                data.warmthTimer += 10;
                if (data.warmthTimer >= 200) {
                    if (data.frostbite > 0) data.frostbite--;
                    data.warmthTimer = 0;
                }
            } else {
                data.warmthTimer = 0;
                data.freezeTimer += 10;
                if (data.freezeTimer >= 2400) {
                    if (data.frostbite < 10) data.frostbite++;
                    data.freezeTimer = 0;
                }
            }
            dataChanged = true;
        }

        if (player.tickCount % 40 == 0) {
            if (data.frostbite >= 7 && data.frostbite < 9) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0, false, false, true));
            } else if (data.frostbite == 9) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, false, true));
            }
        }

        if (player.tickCount % 300 == 0) {
            if (data.frostbite >= 3 && data.frostbite < 7) {
                player.displayClientMessage(Component.literal("It's getting cold, and the nights here are very cold"), true);
            } else if (data.frostbite >= 7 && data.frostbite < 9) {
                player.displayClientMessage(Component.literal("I'm freezing"), true);
            } else if (data.frostbite == 9) {
                player.displayClientMessage(Component.literal("......."), true);
            }
        }

        if (data.frostbite == 10 && player.tickCount % 20 == 0) {
            player.hurt(level.damageSources().freeze(), 1.0F);
            player.setTicksFrozen(player.getTicksFrozen() + 40);
        }

        if (dataChanged && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new NetworkInit.SyncFrostbitePacket(data.frostbite, data.freezeTimer, data.warmthTimer));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide() || !level.dimension().location().toString().equals("lostinfog:the_fog_forest")) return;

        CampfireData campfireData = level.getData(LostinfogModAttachments.CAMPFIRES);
        List<BlockPos> toRemove = new ArrayList<>();

        for (Map.Entry<BlockPos, Integer> entry : campfireData.timers.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = level.getBlockState(pos);
            if ((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) && state.getValue(CampfireBlock.LIT)) {
                int timeLeft = entry.getValue() - 1;
                if (timeLeft <= 0) {
                    level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
                    toRemove.add(pos);
                } else {
                    entry.setValue(timeLeft);
                }
            } else {
                toRemove.add(pos);
            }
        }

        for (BlockPos pos : toRemove) {
            campfireData.timers.remove(pos);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || !level.dimension().location().toString().equals("lostinfog:the_fog_forest")) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
            ItemStack stack = event.getItemStack();
            if (stack.is(Items.FLINT_AND_STEEL) && !state.getValue(CampfireBlock.LIT)) {
                CampfireData campfireData = level.getData(LostinfogModAttachments.CAMPFIRES);
                campfireData.timers.put(pos.immutable(), level.random.nextInt(1200) + 1200);
            }
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo().location().toString().equals("lostinfog:the_fog_forest")) {
            Player player = event.getEntity();
            ItemStack listStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("lostinfog", "list")));
            ItemStack flintStack = new ItemStack(Items.FLINT_AND_STEEL);
            flintStack.setDamageValue(flintStack.getMaxDamage() / 2);
            
            if (!player.getInventory().add(listStack)) player.drop(listStack, false);
            if (!player.getInventory().add(flintStack)) player.drop(flintStack, false);
        }
    }
}