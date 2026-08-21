package net.mcreator.lostinfog;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class FrostbiteData implements INBTSerializable<CompoundTag> {
    public int frostbite = 0;
    public int freezeTimer = 0;
    public int warmthTimer = 0;

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("frostbite", frostbite);
        tag.putInt("freezeTimer", freezeTimer);
        tag.putInt("warmthTimer", warmthTimer);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        frostbite = tag.getInt("frostbite");
        freezeTimer = tag.getInt("freezeTimer");
        warmthTimer = tag.getInt("warmthTimer");
    }
}