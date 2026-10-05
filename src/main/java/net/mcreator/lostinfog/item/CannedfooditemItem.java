
package net.mcreator.lostinfog.item;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CannedfooditemItem extends Item {
	public CannedfooditemItem() {
		super(new Item.Properties().stacksTo(3).rarity(Rarity.COMMON).food((new FoodProperties.Builder()).nutrition(5).saturationModifier(0.3f).alwaysEdible().build()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, list, flag);
		list.add(Component.translatable("item.lostinfog.cannedfooditem.description_0"));
	}
}
