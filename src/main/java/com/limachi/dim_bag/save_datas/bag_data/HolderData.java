package com.limachi.dim_bag.save_datas.bag_data;

import com.limachi.lim_lib.World;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

class HolderData {
    protected Entity entity = null;
    protected BlockPos position = null;
    protected Level level = null;
    protected boolean paradox = false;

    protected HolderData(CompoundTag data, BlockPos minWalls, BlockPos maxWalls, ServerLevel bagLevel) {
        if (data.contains("dimension") && data.contains("position")) {
            position = BlockPos.of(data.getLong("position"));
            level = World.getLevel(data.getString("dimension"));
        }
        if (data.contains("entity")) {
            UUID searchEntity = data.getUUID("entity");
            if (data.contains("paradox_position")) {
                BlockPos searchPos = BlockPos.of(data.getLong("paradox_position"));
                List<Entity> found = bagLevel.getEntities((Entity) null, new AABB(searchPos.offset(-1, -1, -1), searchPos.offset(1, 1, 1)), e -> e.getUUID().equals(searchEntity));
                if (found.size() > 0) {
                    entity = found.get(0);
                    paradox = true;
                }
            } else if (position != null && level != null) {
                List<Entity> found = level.getEntities((Entity) null, new AABB(position.offset(-1, -1, -1), position.offset(1, 1, 1)), e -> e.getUUID().equals(searchEntity));
                if (found.size() > 0)
                    entity = found.get(0);
            }
            if (entity == null && paradox)
                bagLevel.getEntities((Entity)null, new AABB(minWalls, maxWalls), e -> e.getUUID().equals(searchEntity));
        }
    }

    protected CompoundTag serialize() {
        CompoundTag out = new CompoundTag();
        if (position != null && level != null) {
            out.putLong("position", position.asLong());
            out.putString("dimension", level.dimension().location().toString());
        }
        if (entity != null) {
            if (paradox)
                out.putLong("paradox_position", entity.blockPosition().asLong());
            out.putUUID("entity", entity.getUUID());
        }
        return out;
    }
}