package net.mcreator.lostinfog.client;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.GraphicsStatus;

@Mod("lostinfog")
public class LostInFogClientMod {

    public LostInFogClientMod() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();
        Options options = mc.options;

        options.gamma().set(0.0D);
        options.graphicsMode().set(GraphicsStatus.FAST);
        options.ambientOcclusion().set(true);

        options.save();
    }
}