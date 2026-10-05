package net.mcreator.lostinfog;

import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = "lostinfog")
public class DaySkipMechanic {

    @SubscribeEvent
    public static void onBedRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        if (level.getBlockState(event.getPos()).is(BlockTags.BEDS)) {
            long time = level.getDayTime() % 24000;
            Player p = event.getEntity();

            if (time >= 0 && time < 13000) {
                long timeToSkip = 13000 - time;
                
                if (level instanceof ServerLevel sLevel) {
                    sLevel.setDayTime(level.getDayTime() + timeToSkip);
                    p.displayClientMessage(Component.literal("The day passes..."), true);
                    
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
    }
}