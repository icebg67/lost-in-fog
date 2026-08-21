package net.mcreator.lostinfog;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.util.INBTSerializable;
import java.util.HashMap;
import java.util.Map;

public class CampfireData implements INBTSerializable<CompoundTag> {
    public final Map<BlockPos, Integer> timers = new HashMap<>();

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag list = new CompoundTag();
        int index = 0;
        for (Map.Entry<BlockPos, Integer> entry : timers.entrySet()) {
            CompoundTag element = new CompoundTag();
            element.putLong("pos", entry.getKey().asLong());
            element.putInt("time", entry.getValue());
            list.put("c_" + index, element);
            index++;
        }
        tag.put("campfires", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        timers.clear();
        CompoundTag list = tag.getCompound("campfires");
        for (String key : list.getAllKeys()) {
            CompoundTag element = list.getCompound(key);
            timers.put(BlockPos.of(element.getLong("pos")), element.getInt("time"));
        }
    }
}