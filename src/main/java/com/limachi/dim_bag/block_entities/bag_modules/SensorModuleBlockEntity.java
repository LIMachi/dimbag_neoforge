package com.limachi.dim_bag.block_entities.bag_modules;

import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

public class SensorModuleBlockEntity extends BlockEntity {

    //what directions relative to the bag can the event originate from
    ArrayList<Direction> activeDirections = new ArrayList<>(Arrays.stream(Direction.values()).toList());
    //what events are allowed to be processed (bit mask)
    int events = 0xFFFF;
    //should the analog redstone signal be produced using the event value instead of the distance (as if a comparator was used)
    boolean event = false;
    int rangeMin = 0;
    int rangeMax = 16;
    int cooldown = 0;

    @RegisterBlockEntity(blocks = "sensor_module")
    public static RegistryObject<BlockEntityType<SensorModuleBlockEntity>> R_TYPE;

    public SensorModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    public int power() {
        return getBlockState().getValue(BlockStateProperties.POWER);
    }

    public void setPower(int power) {
        BlockState bs = getBlockState();
        if (level instanceof ServerLevel && bs.getValue(BlockStateProperties.POWER) != power) {
            bs = bs.setValue(BlockStateProperties.POWER, power);
            level.setBlock(worldPosition, bs, 2);
            level.updateNeighborsAt(worldPosition, bs.getBlock());
        }
    }

    public boolean listenTo(Direction from, int distance, int event) {
        return (power() == 0 || cooldown - 1 <= 0) && distance >= rangeMin && distance <= rangeMax && activeDirections.contains(from) && (events & (1 << event)) != 0;
    }

    public void receiveEvent(int distance, int event) {
        int signal;
        if (this.event)
            signal = event;
        else
            signal = distance;
        signal = Mth.clamp(signal, 1, 15);
        setPower(signal);
        cooldown = 10;
    }

    public void tick() {
        if (power() > 0 && --cooldown <= 0)
            setPower(0);
    }

    public void install(CompoundTag tag) {
        loadFromTag(tag);
    }

    public CompoundTag uninstall() { return data(); }

    public CompoundTag data() {
        CompoundTag out = new CompoundTag();
        int dir = 0;
        for (Direction d: activeDirections)
            dir |= 1 << d.ordinal();
        out.putByte("directions", (byte)dir);
        out.putByte("range_min", (byte)rangeMin);
        out.putByte("range_max", (byte)rangeMax);
        out.putInt("events", events);
        out.putBoolean("event", event);
        return out;
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.merge(data());
    }

    public void loadFromTag(@Nonnull CompoundTag tag) {
        int dir = tag.getByte("directions");
        activeDirections.clear();
        Direction[] d = Direction.values();
        for (int i = 0; i < d.length; ++i)
            if (((1 << i) | dir) != 0)
                activeDirections.add(d[i]);
        events = tag.getInt("events");
        rangeMin = tag.getByte("range_min");
        rangeMax = tag.getByte("range_max");
        event = tag.getBoolean("event");
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        loadFromTag(tag);
    }
}
