
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.lostinfog.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.lostinfog.item.PillsItem;
import net.mcreator.lostinfog.item.ListItem;
import net.mcreator.lostinfog.item.LetterItem;
import net.mcreator.lostinfog.item.CassetteItem;
import net.mcreator.lostinfog.LostinfogMod;

public class LostinfogModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(LostinfogMod.MODID);
	public static final DeferredItem<Item> TVOFF = block(LostinfogModBlocks.TVOFF);
	public static final DeferredItem<Item> TVON = block(LostinfogModBlocks.TVON);
	public static final DeferredItem<Item> THEFOG_SPAWN_EGG = REGISTRY.register("thefog_spawn_egg", () -> new DeferredSpawnEggItem(LostinfogModEntities.THEFOG, -1, -1, new Item.Properties()));
	public static final DeferredItem<Item> MAIL = block(LostinfogModBlocks.MAIL);
	public static final DeferredItem<Item> LETTER = REGISTRY.register("letter", LetterItem::new);
	public static final DeferredItem<Item> PILLS = REGISTRY.register("pills", PillsItem::new);
	public static final DeferredItem<Item> DAVID_SPAWN_EGG = REGISTRY.register("david_spawn_egg", () -> new DeferredSpawnEggItem(LostinfogModEntities.DAVID, -1, -1, new Item.Properties()));
	public static final DeferredItem<Item> GLITCH_SPAWN_EGG = REGISTRY.register("glitch_spawn_egg", () -> new DeferredSpawnEggItem(LostinfogModEntities.GLITCH, -1, -1, new Item.Properties()));
	public static final DeferredItem<Item> CASSETTE = REGISTRY.register("cassette", CassetteItem::new);
	public static final DeferredItem<Item> WATCHER_SPAWN_EGG = REGISTRY.register("watcher_spawn_egg", () -> new DeferredSpawnEggItem(LostinfogModEntities.WATCHER, -1, -1, new Item.Properties()));
	public static final DeferredItem<Item> CORNER_SPAWN_EGG = REGISTRY.register("corner_spawn_egg", () -> new DeferredSpawnEggItem(LostinfogModEntities.CORNER, -1, -1, new Item.Properties()));
	public static final DeferredItem<Item> FRIEND_SPAWN_EGG = REGISTRY.register("friend_spawn_egg", () -> new DeferredSpawnEggItem(LostinfogModEntities.FRIEND, -1, -1, new Item.Properties()));
	public static final DeferredItem<Item> LIST = REGISTRY.register("list", ListItem::new);
	public static final DeferredItem<Item> PALKA = block(LostinfogModBlocks.PALKA);
	public static final DeferredItem<Item> PALKKA = block(LostinfogModBlocks.PALKKA);
	public static final DeferredItem<Item> PALKKA_3 = block(LostinfogModBlocks.PALKKA_3);
	public static final DeferredItem<Item> PALKKA_4 = block(LostinfogModBlocks.PALKKA_4);
	public static final DeferredItem<Item> RADIO = block(LostinfogModBlocks.RADIO);
	public static final DeferredItem<Item> RADIO_2 = block(LostinfogModBlocks.RADIO_2);
	public static final DeferredItem<Item> VHODD = block(LostinfogModBlocks.VHODD);
	public static final DeferredItem<Item> JOE = block(LostinfogModBlocks.JOE);
	public static final DeferredItem<Item> EPS = block(LostinfogModBlocks.EPS);
	public static final DeferredItem<Item> GOAT = block(LostinfogModBlocks.GOAT);
	public static final DeferredItem<Item> WINDOWENTITY_SPAWN_EGG = REGISTRY.register("windowentity_spawn_egg", () -> new DeferredSpawnEggItem(LostinfogModEntities.WINDOWENTITY, -1, -1, new Item.Properties()));
	public static final DeferredItem<Item> WHOISTHAT_SPAWN_EGG = REGISTRY.register("whoisthat_spawn_egg", () -> new DeferredSpawnEggItem(LostinfogModEntities.WHOISTHAT, -1, -1, new Item.Properties()));

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
