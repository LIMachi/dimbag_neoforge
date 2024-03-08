package com.limachi.dim_bag.block_entities.bag_modules;

import com.limachi.dim_bag.menus.SculkMenu;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.stream.IntStream;

public class SculkModuleBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

    @RegisterBlockEntity(blocks = "sculk_module")
    public static RegistryObject<BlockEntityType<SculkModuleBlockEntity>> R_TYPE;

    private static final int[] IN_SLOTS = IntStream.range(0, 9).toArray();
    private static final int[] OUT_SLOTS = IntStream.range(9, 18).toArray();
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(18, ItemStack.EMPTY);

    public SculkModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    public void install(CompoundTag tag) {
        loadFromTag(tag);
    }

    public int insertXp(int amount) {
        boolean changed = false;
        if (amount >= 5 && level != null) {
            int canUpgrade = 0;
            boolean canShrieker = false;
            boolean canSensor = false;
            for (int i = 9; i < 18; ++i) {
                ItemStack stack = itemStacks.get(i);
                if (stack.getItem() == Items.SCULK)
                    canUpgrade += stack.getCount();
                else if (stack.isEmpty())
                    canShrieker = canSensor = true;
                else if (stack.getItem() == Items.SCULK_SENSOR)
                    canSensor = true;
                else if (stack.getItem() == Items.SCULK_SHRIEKER)
                    canShrieker = true;
            }
            if (canShrieker || canSensor) {
                int upgrade = 0;
                for (int i = 0; i < canUpgrade && i * 5 < amount; ++i)
                    if (level.random.nextInt(5) == 0)
                        ++upgrade;
                upgrade = Math.min(upgrade, amount / 5);
                if (upgrade > 0) {
                    int shriekers = 0;
                    int sensors = 0;
                    if (canSensor && canShrieker) {
                        for (int i = 0; i < upgrade; ++i)
                            if (level.random.nextInt(2) == 0)
                                ++shriekers;
                            else
                                ++sensors;
                    } else if (canSensor)
                        sensors = upgrade;
                    else
                        shriekers = upgrade;
                    for (int i = 9; i < 18 && (shriekers > 0 || sensors > 0); ++i) {
                        ItemStack stack = itemStacks.get(i);
                        if (stack.getCount() >= 64) continue;
                        if (shriekers > 0 && stack.getItem() == Items.SCULK_SHRIEKER) {
                            int insert = Math.min(shriekers, 64 - stack.getCount());
                            stack.grow(insert);
                            shriekers -= insert;
                        } else if (sensors > 0 && stack.getItem() == Items.SCULK_SENSOR) {
                            int insert = Math.min(sensors, 64 - stack.getCount());
                            stack.grow(insert);
                            sensors -= insert;
                        } else if (stack.isEmpty()) {
                            if (sensors > 0) {
                                int insert = Math.min(sensors, 64);
                                itemStacks.set(i, new ItemStack(Items.SCULK_SENSOR, insert));
                                sensors -= insert;
                                changed = true;
                            } else {
                                int insert = Math.min(shriekers, 64);
                                itemStacks.set(i, new ItemStack(Items.SCULK_SHRIEKER, insert));
                                shriekers -= insert;
                                changed = true;
                            }
                        }
                    }
                    upgrade -= sensors + shriekers;
                    amount -= upgrade * 5;
                    for (int i = 9; i < 18 && upgrade > 0; ++i) {
                        ItemStack stack = itemStacks.get(i);
                        if (stack.getItem() == Items.SCULK) {
                            if (upgrade >= stack.getCount()) {
                                upgrade -= stack.getCount();
                                itemStacks.set(i, ItemStack.EMPTY);
                                changed = true;
                            } else {
                                itemStacks.get(i).shrink(upgrade);
                                changed = true;
                                upgrade = 0;
                            }
                        }
                    }
                }
            }
        }
        if (amount > 0) {
            int maxConvert = 0;
            for (int i = 0; i < 9; ++i)
                if (!itemStacks.get(i).isEmpty())
                    maxConvert += itemStacks.get(i).getCount();
            if (maxConvert > 0) {
                int maxInsert = 0;
                for (int i = 9; i < 18; ++i)
                    if (itemStacks.get(i).isEmpty())
                        maxInsert += 64;
                    else if (itemStacks.get(i).getItem() == Items.SCULK)
                        maxInsert += Math.max(0, 64 - itemStacks.get(i).getCount());
                maxConvert = maxInsert = Math.min(amount, Math.min(maxInsert, maxConvert));
                if (maxConvert > 0) {
                    amount -= maxConvert;
                    for (int i = 0; i < 9 && maxConvert > 0; ++i) {
                        ItemStack stack = itemStacks.get(i);
                        if (stack.isEmpty()) continue;
                        if (maxConvert >= stack.getCount()) {
                            maxConvert -= stack.getCount();
                            itemStacks.set(i, ItemStack.EMPTY);
                            changed = true;
                        } else {
                            itemStacks.get(i).shrink(maxConvert);
                            changed = true;
                            maxConvert = 0;
                        }
                    }
                    for (int i = 9; i < 18 && maxInsert > 0; ++i) {
                        ItemStack stack = itemStacks.get(i);
                        if (stack.isEmpty()) {
                            ItemStack newSculk = new ItemStack(Items.SCULK, Math.min(maxInsert, 64));
                            maxInsert -= newSculk.getCount();
                            itemStacks.set(i, newSculk);
                            changed = true;
                        } else if (stack.getItem() == Items.SCULK) {
                            int insert = Math.min(maxInsert, Math.max(0, 64 - stack.getCount()));
                            stack.grow(insert);
                            maxInsert -= insert;
                            changed = true;
                        }
                    }
                }
            }
        }
        if (changed)
            setChanged();
        return amount;
    }

    public boolean validInsert(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem bi && bi.getBlock().builtInRegistryHolder().is(BlockTags.SCULK_REPLACEABLE);
    }

    public CompoundTag uninstall() { return data(); }

    public CompoundTag data() { return ContainerHelper.saveAllItems(new CompoundTag(), itemStacks, false); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.merge(data());
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.sculk_module");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new SculkMenu(id, playerInventory, this);
    }

    public void loadFromTag(@Nonnull CompoundTag tag) {
        itemStacks.clear();
        if (tag.contains("Items", Tag.TAG_LIST))
            ContainerHelper.loadAllItems(tag, itemStacks);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        loadFromTag(tag);
    }

    @Override
    public int getContainerSize() {
        return 18;
    }

    @Override
    public boolean isEmpty() {
        return itemStacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot < 0 || slot >= 18 ? ItemStack.EMPTY : itemStacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack out = ContainerHelper.removeItem(itemStacks, slot, amount);
        if (!out.isEmpty())
            setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(itemStacks, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 18) {
            itemStacks.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        itemStacks.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction dir) {
        return dir == Direction.DOWN ? OUT_SLOTS : IN_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return direction != Direction.DOWN && slot < 9 && slot >= 0 && validInsert(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && slot >= 9 && slot < 18;
    }
}
