
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.lostinfog.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.registries.Registries;

import net.mcreator.lostinfog.world.inventory.ChestMenu;
import net.mcreator.lostinfog.LostinfogMod;

public class LostinfogModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, LostinfogMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<ChestMenu>> CHEST = REGISTRY.register("chest", () -> IMenuTypeExtension.create(ChestMenu::new));
}
