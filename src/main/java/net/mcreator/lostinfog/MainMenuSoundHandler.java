package net.mcreator.lostinfog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class MainMenuSoundHandler {

    private static boolean hasPlayedSound = false;

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen && !hasPlayedSound) {
            Minecraft mc = Minecraft.getInstance();

            mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(0.0);
            mc.options.save();

            ResourceLocation soundLocation = ResourceLocation.parse("lostinfog:mainmenu");
            SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(soundLocation);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0F, 1.0F));

            hasPlayedSound = true;
        }
    }
}