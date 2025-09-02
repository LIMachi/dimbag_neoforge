package com.limachi.dim_bag;

import com.limachi.lim_lib.common.scrollSystem.IScrollItem;
import net.minecraft.world.entity.player.Player;

public class BagMode implements IScrollItem {
    @Override
    public void scroll(Player player, int i, int i1) {}
    @Override
    public void scrollFeedBack(Player player, int i, int i1) {}
    @Override
    public boolean canScroll(Player player, int i) { return false; }
}
