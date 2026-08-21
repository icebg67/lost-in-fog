package net.mcreator.lostinfog.client;

import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openLetterScreen() {
        Minecraft.getInstance().setScreen(new LetterScreen());
    }
    public static void openPaperScreem() {
        Minecraft.getInstance().setScreen(new PaperScreem());
    }
}	