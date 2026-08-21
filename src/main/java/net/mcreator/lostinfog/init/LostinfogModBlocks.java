
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.lostinfog.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import net.mcreator.lostinfog.block.VhoddBlock;
import net.mcreator.lostinfog.block.TvonBlock;
import net.mcreator.lostinfog.block.TvoffBlock;
import net.mcreator.lostinfog.block.RadioBlock;
import net.mcreator.lostinfog.block.Radio2Block;
import net.mcreator.lostinfog.block.PalkkaBlock;
import net.mcreator.lostinfog.block.Palkka4Block;
import net.mcreator.lostinfog.block.Palkka3Block;
import net.mcreator.lostinfog.block.PalkaBlock;
import net.mcreator.lostinfog.block.MailBlock;
import net.mcreator.lostinfog.block.JoeBlock;
import net.mcreator.lostinfog.block.GOATBlock;
import net.mcreator.lostinfog.block.EpsBlock;
import net.mcreator.lostinfog.LostinfogMod;

public class LostinfogModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(LostinfogMod.MODID);
	public static final DeferredBlock<Block> TVOFF = REGISTRY.register("tvoff", TvoffBlock::new);
	public static final DeferredBlock<Block> TVON = REGISTRY.register("tvon", TvonBlock::new);
	public static final DeferredBlock<Block> MAIL = REGISTRY.register("mail", MailBlock::new);
	public static final DeferredBlock<Block> PALKA = REGISTRY.register("palka", PalkaBlock::new);
	public static final DeferredBlock<Block> PALKKA = REGISTRY.register("palkka", PalkkaBlock::new);
	public static final DeferredBlock<Block> PALKKA_3 = REGISTRY.register("palkka_3", Palkka3Block::new);
	public static final DeferredBlock<Block> PALKKA_4 = REGISTRY.register("palkka_4", Palkka4Block::new);
	public static final DeferredBlock<Block> RADIO = REGISTRY.register("radio", RadioBlock::new);
	public static final DeferredBlock<Block> RADIO_2 = REGISTRY.register("radio_2", Radio2Block::new);
	public static final DeferredBlock<Block> VHODD = REGISTRY.register("vhodd", VhoddBlock::new);
	public static final DeferredBlock<Block> JOE = REGISTRY.register("joe", JoeBlock::new);
	public static final DeferredBlock<Block> EPS = REGISTRY.register("eps", EpsBlock::new);
	public static final DeferredBlock<Block> GOAT = REGISTRY.register("goat", GOATBlock::new);
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
