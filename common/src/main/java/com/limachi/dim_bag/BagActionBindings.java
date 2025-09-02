package com.limachi.dim_bag;

import net.minecraft.network.chat.Component;

import java.util.HashMap;

public class BagActionBindings {
    public static final HashMap<String, BagActionBinding> bindings = new HashMap<>();

    public enum Target {
        Air,
        Block,
        Fluid,
        Entity
    }

    public enum KeyCombo {
        RightClick,
        ShiftRightClick,
        LeftClick
    }

    public interface BagActionBinding {
        boolean canBind(boolean shift, boolean leftClick, Target target, boolean bagItself, boolean ghost);
        boolean run(boolean shift, boolean leftClick, Target target, boolean bagItself, boolean ghost);
        Component name();
    }

    static {
        bindings.put("open_menu", new BagActionBinding() {
            @Override
            public boolean canBind(boolean shift, boolean leftClick, Target target, boolean bagItself) { return true; }

            @Override
            public boolean run(boolean shift, boolean leftClick, Target target, boolean bagItself) {
                return false;
            }

            @Override
            public Component name() {
                return null;
            }
        })
    }

    public enum BagItemAction {
        OpenMenu, //open the bag menu (multi screen menu, start in inventory screen but has access to settings and other screens)
        EnterLeave, //if in matching bag, leave, otherwise enter it

        PlaceFluid, //raycast and try to place fluid
        PumpFluid, //raycast and try to pump fluid
        Bucket, //do both of place and pump by trying to pump first, and if it can't, it will try to place the fluid instead

        Capture, //raycast and try to capture entity
        Release, //raycast position and try to release entity
        CaptureRelease, //do both capture and place, trying to capture first, and otherwise try to release

        ArmorStandHandRightClick, //does the right click action of the item in the armor stand module
        ArmorStandHandLeftClick, //does the left click action of the item in the armor stand module
    }

    public enum BagEntityAction {
        OpenMenu, //same as item actions
        EnterLeave, //same as item actions (note: the leave should only occur in paradox mode)


    }
}
