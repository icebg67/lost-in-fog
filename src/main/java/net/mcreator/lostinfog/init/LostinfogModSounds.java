
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.lostinfog.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.mcreator.lostinfog.LostinfogMod;

public class LostinfogModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, LostinfogMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> FLASHLIGHT_OFF = REGISTRY.register("flashlight_off", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "flashlight_off")));
	public static final DeferredHolder<SoundEvent, SoundEvent> FLASHLIGHT_ON = REGISTRY.register("flashlight_on", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "flashlight_on")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STUKDOOR = REGISTRY.register("stukdoor", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "stukdoor")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STUKOKNO = REGISTRY.register("stukokno", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "stukokno")));
	public static final DeferredHolder<SoundEvent, SoundEvent> MAINMENU = REGISTRY.register("mainmenu", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "mainmenu")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STARTGAME = REGISTRY.register("startgame", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "startgame")));
	public static final DeferredHolder<SoundEvent, SoundEvent> TVVIKL = REGISTRY.register("tvvikl", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "tvvikl")));
	public static final DeferredHolder<SoundEvent, SoundEvent> TVVKL = REGISTRY.register("tvvkl", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "tvvkl")));
	public static final DeferredHolder<SoundEvent, SoundEvent> VHS = REGISTRY.register("vhs", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "vhs")));
	public static final DeferredHolder<SoundEvent, SoundEvent> FLASHLIGHTON = REGISTRY.register("flashlighton", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "flashlighton")));
	public static final DeferredHolder<SoundEvent, SoundEvent> FLASHLIGHTOFF = REGISTRY.register("flashlightoff", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "flashlightoff")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STARTGAMESHELK = REGISTRY.register("startgameshelk", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "startgameshelk")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SCREAM = REGISTRY.register("scream", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "scream")));
	public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT = REGISTRY.register("ambient", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "ambient")));
	public static final DeferredHolder<SoundEvent, SoundEvent> EVENT3 = REGISTRY.register("event3", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "event3")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ITSME = REGISTRY.register("itsme", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "itsme")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SARAH = REGISTRY.register("sarah", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "sarah")));
	public static final DeferredHolder<SoundEvent, SoundEvent> TABLETKA = REGISTRY.register("tabletka", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "tabletka")));
	public static final DeferredHolder<SoundEvent, SoundEvent> CORNER = REGISTRY.register("corner", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "corner")));
	public static final DeferredHolder<SoundEvent, SoundEvent> THEFOG = REGISTRY.register("thefog", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "thefog")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ATTACK = REGISTRY.register("attack", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "attack")));
	public static final DeferredHolder<SoundEvent, SoundEvent> KHEKHE = REGISTRY.register("khekhe", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "khekhe")));
	public static final DeferredHolder<SoundEvent, SoundEvent> FOREST = REGISTRY.register("forest", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "forest")));
	public static final DeferredHolder<SoundEvent, SoundEvent> RAD = REGISTRY.register("rad", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "rad")));
	public static final DeferredHolder<SoundEvent, SoundEvent> RADIOSOUND = REGISTRY.register("radiosound", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "radiosound")));
	public static final DeferredHolder<SoundEvent, SoundEvent> RADIOVHS = REGISTRY.register("radiovhs", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "radiovhs")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STARTSOUNDVIDEO = REGISTRY.register("startsoundvideo", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "startsoundvideo")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DAY1 = REGISTRY.register("day1", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "day1")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DAY2 = REGISTRY.register("day2", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "day2")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DAY3 = REGISTRY.register("day3", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "day3")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DAY4 = REGISTRY.register("day4", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "day4")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DAY5 = REGISTRY.register("day5", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "day5")));
	public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENTBUNKER = REGISTRY.register("ambientbunker", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "ambientbunker")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DEATH1 = REGISTRY.register("death1", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "death1")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DEATH2 = REGISTRY.register("death2", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "death2")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DAY6 = REGISTRY.register("day6", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("lostinfog", "day6")));
}
