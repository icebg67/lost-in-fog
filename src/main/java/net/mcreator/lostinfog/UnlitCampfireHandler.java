package net.mcreator.lostinfog;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber
public class UnlitCampfireHandler {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockState newState = event.getPlacedBlock();
        
        if (newState.getBlock() instanceof CampfireBlock 
                && newState.hasProperty(BlockStateProperties.LIT) 
                && newState.getValue(BlockStateProperties.LIT)) {
            
            if (event.getEntity() instanceof Player player) {
                if (player.getMainHandItem().getItem() == newState.getBlock().asItem() || 
                    player.getOffhandItem().getItem() == newState.getBlock().asItem()) {
                    
                    event.getLevel().setBlock(event.getPos(), newState.setValue(BlockStateProperties.LIT, false), 3);
                }
            }
        }
    }
}