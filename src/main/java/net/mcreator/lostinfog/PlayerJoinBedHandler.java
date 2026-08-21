package net.mcreator.lostinfog;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "lostinfog")
public class PlayerJoinBedHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            if (!player.getTags().contains("lostinfog_bed_checked")) {
                player.addTag("lostinfog_bed_checked");

                MinecraftServer server = player.getServer();
                boolean isHost = server != null && server.isSingleplayerOwner(player.getGameProfile());

                if (!isHost) {
                    ItemStack bed = new ItemStack(Items.RED_BED);
                    if (!player.getInventory().add(bed)) {
                        player.drop(bed, false);
                    }
                }
            }
        }
    }
}