package net.mcreator.lostinfog;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class DoorLockManager extends SavedData {

    private final Map<BlockPos, String> lockedDoors = new HashMap<>();

    public static final SavedData.Factory<DoorLockManager> FACTORY = new SavedData.Factory<>(
            DoorLockManager::new, DoorLockManager::load, DataFixTypes.LEVEL
    );

    public DoorLockManager() {}

    public static DoorLockManager load(CompoundTag tag, HolderLookup.Provider provider) {
        DoorLockManager data = new DoorLockManager();
        ListTag list = tag.getList("LockedDoors", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            data.lockedDoors.put(BlockPos.of(entry.getLong("Pos")), entry.getString("Face"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, String> entry : lockedDoors.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putLong("Pos", entry.getKey().asLong());
            e.putString("Face", entry.getValue());
            list.add(e);
        }
        tag.put("LockedDoors", list);
        return tag;
    }

    public static DoorLockManager get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(FACTORY, "lostinfog_door_locks");
        }
        return null;
    }

    public boolean isLocked(BlockPos pos) {
        return lockedDoors.containsKey(pos);
    }

    public String getLockedFace(BlockPos pos) {
        return lockedDoors.getOrDefault(pos, "unknown");
    }

    public void lock(BlockPos pos, String face) {
        lockedDoors.put(pos, face);
        setDirty();
    }

    public void unlock(BlockPos pos) {
        if (lockedDoors.remove(pos) != null) {
            setDirty();
        }
    }
}