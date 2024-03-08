package com.limachi.dim_bag.block_entities.bag_modules;

import com.limachi.dim_bag.blocks.bag_modules.ParasiteModule;
import com.limachi.dim_bag.entities.utils.Actuator;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.dim_bag.utils.Tags;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class ParasiteModuleBlockEntity extends BlockEntity {

    @RegisterBlockEntity(blocks = "parasite_module")
    public static RegistryObject<BlockEntityType<ParasiteModuleBlockEntity>> R_TYPE;

    protected Actuator command = new Actuator("");
    protected int bagId;
    private BagInstance bag = null; //only present server side!!

    public ParasiteModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    public void install(BagInstance bag, CompoundTag command) {
        this.command = new Actuator(command.getString("command"));
        this.bagId = bag.bagId();
    }

    public void replaceCommand(CompoundTag command) {
        this.command = new Actuator(command.getString("command"));
    }

    public CompoundTag uninstall() { return Tags.singleton("command", command.getOriginal()); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(ParasiteModule.NAME, command.getOriginal());
        if (level != null && !level.isClientSide)
            tag.putInt("bag", bagId);
    }

    public BagInstance getBag() {
        if (bag == null)
            bag = BagsData.getBagHandle(bagId, ()->bag = null);
        return bag;
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        bag = null;
        String cmd = tag.getString(ParasiteModule.NAME);
        if (!command.getOriginal().equals(cmd))
            command = new Actuator(cmd);
        bagId = tag.getInt("bag");
    }

    public void run(boolean powered) {
        if (getBag() != null)
            bag.getHolder(false).map(e->command.run(e, powered));
    }
}
