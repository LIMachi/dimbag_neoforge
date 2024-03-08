package com.limachi.dim_bag.entities.utils;

//player: simulate key inputs
//other: key input equivalents

import com.google.common.collect.ImmutableList;
import com.limachi.dim_bag.events.EntityEvents;
import com.limachi.lim_lib.Events;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.Log;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * walk (horizontal)
 * jump (vertical impulsion)
 * fly (vertical, use jump/crouch on player)
 * crouch (only player)
 * inventory (only player)
 * attack
 * interact (only player? could make it work on other entities)
 * chat (if start by command: converted to execute, will prevent commands that require a lvl > 2, if text, execute the say command with the text)
 * log (send private message to player that hold the bag, do not use standard chat)
 * look (horizontal/vertical rotation)
 */
public class Actuator {

    private BiFunction<Entity, Boolean, Boolean> run = null;
    private String original = "";

    public Actuator(String command) {
        Result t = parseAction(command);
        if (t.error != null)
            Log.warn(t.error); //FIXME: should be sent to player screen instead
        else {
            original = command;
            run = t.ok;
        }
    }

    public String getOriginal() { return original; }

    public boolean run(Entity entity, boolean powered) {
        if (run == null) return false;
        return run.apply(entity, powered);
    }

    public record Result(BiFunction<Entity, Boolean, Boolean> ok, String error) {}

    public static final ImmutableList<String> command_suggestions;
    static {
        ArrayList<String> t = new ArrayList<>();
        t.add("walk stop");
        t.add("walk forward");
        t.add("walk back");
        t.add("walk left");
        t.add("walk right");
        t.add("walk north");
        t.add("walk south");
        t.add("walk east");
        t.add("walk west");
        t.add("jump");
        t.add("fly stop");
        t.add("fly up");
        t.add("fly down");
        t.add("fly forward");
        t.add("fly back");
        t.add("fly right");
        t.add("fly left");
        t.add("sneak");
        t.add("sneak stop");
        t.add("inventory");
        t.add("attack");
        t.add("attack stop");
        t.add("interact");
        t.add("interact stop");
        t.add("chat Hello World!");
        t.add("log Hello World!");
        t.add("look 0 0");
        command_suggestions = ImmutableList.copyOf(t);
    }

    public static Result parseAction(String command) {
        String[] split = command.trim().replaceAll(" +", " ").split(" ");
        if (split.length < 1) return new Result(null, "Empty string");
        switch (split[0]) {
            case "walk" -> {
                if (split.length >= 2)
                    switch (split[1]) {
                        case "stop" -> { return new Result(Actuator::stopWalk, null); }
                        case "forward" -> { return new Result((e, p)->walk(e, WalkDirection.FORWARD, p), null); }
                        case "back" -> { return new Result((e, p)->walk(e, WalkDirection.BACK, p), null); }
                        case "left" -> { return new Result((e, p)->walk(e, WalkDirection.LEFT, p), null); }
                        case "right" -> { return new Result((e, p)->walk(e, WalkDirection.RIGHT, p), null); }
                        case "north" -> { return new Result((e, p)->walkAbsolute(e, Direction.NORTH, p), null); }
                        case "south" -> { return new Result((e, p)->walkAbsolute(e, Direction.SOUTH, p), null); }
                        case "east" -> { return new Result((e, p)->walkAbsolute(e, Direction.EAST, p), null); }
                        case "west" -> { return new Result((e, p)->walkAbsolute(e, Direction.WEST, p), null); }
                    }
                return new Result(null, "Invalid/missing qualifiers (stop|forward|back|left|right|north|east|south|east) after walk keyword");
            }
            case "jump" -> {
                return new Result(Actuator::jump, null);
            }
            case "fly" -> {
                if (split.length >= 2)
                    switch (split[1]) {
                        case "stop" -> { return new Result(Actuator::stopFly, null); }
                        case "up" -> { return new Result((e, p)->fly(e, Direction.UP, p), null); }
                        case "down" -> { return new Result((e, p)->fly(e, Direction.DOWN, p), null); }
                        case "forward" -> { return new Result((e, p)->fly(e, Direction.NORTH, p), null); }
                        case "back" -> { return new Result((e, p)->fly(e, Direction.SOUTH, p), null); }
                        case "right" -> { return new Result((e, p)->fly(e, Direction.EAST, p), null); }
                        case "left" -> { return new Result((e, p)->fly(e, Direction.WEST, p), null); }
                    }
                return new Result(null, "Invalid/missing qualifiers (stop|up|down|forward|back|left|right) after fly keyword");
            }
            case "sneak" -> {
                if (split.length >= 2 && split[1].equals("stop"))
                    return new Result(Actuator::stopSneak, null);
                else
                    return new Result(Actuator::sneak, null);
            }
            case "inventory" -> {
                return new Result(Actuator::inventory, null);
            }
            case "attack" -> {
                if (split.length >= 2 && split[1].equals("stop"))
                    return new Result(Actuator::attack, null);
                else
                    return new Result(Actuator::stopAttack, null);
            }
            case "interact" -> {
                if (split.length >= 2 && split[1].equals("stop"))
                    return new Result(Actuator::interact, null);
                else
                    return new Result(Actuator::stopInteract, null);
            }
            case "chat" -> {
                final String str = command.replaceAll(" *chat ", "");
                return new Result((e, p)->chat(e, str, p), null);
            }
            case "log" -> {
                final String str = command.replaceAll(" *log ", "");
                return new Result((e, p)->log(e, str, p), null);
            }
            case "look" -> {
                if (split.length < 3) return new Result(null, "Missing qualifiers (y x) after look keyword");
                try {
                    float y = Float.parseFloat(split[1]);
                    float x = Float.parseFloat(split[2]);
                    return new Result((e, p)->look(e, y, x, p), null);
                } catch (NumberFormatException ignore) {
                    return new Result(null, "Can't parse look argument (make sure they are real numbers)");
                }
            }
        }
        return new Result(null, "Unknown command: " + split[0]);
    }

    public static final double WALK_SPEED = 1.;

    public enum WalkDirection {
        FORWARD,
        BACK,
        LEFT,
        RIGHT
    }

    public static boolean walk(Entity target, WalkDirection direction, boolean powered) {
        if (!powered)
            return stopWalk(target, false);
        if (target instanceof Player player) {
            switch (direction) {
                case FORWARD -> KeyMapController.FORWARD.forceKeyState(player, true);
                case BACK -> KeyMapController.BACK.forceKeyState(player, true);
                case LEFT -> KeyMapController.LEFT.forceKeyState(player, true);
                case RIGHT -> KeyMapController.RIGHT.forceKeyState(player, true);
            }
            return true;
        }
        double rot = target.getYRot();
        switch (direction) {
            case BACK -> rot += Math.PI;
            case LEFT -> rot += Math.PI * 0.5;
            case RIGHT -> rot += Math.PI * 1.5;
        }
        Vec3 dir = new Vec3(Math.sin(-rot) * WALK_SPEED, 0., Math.cos(rot) * WALK_SPEED);
//        if (target instanceof Mob mob)
//            mob.getNavigation().moveTo(mob.getX() + dir.x, mob.getY(), mob.getZ() + dir.z, 1.);
//        else
            target.move(MoverType.PLAYER, dir);
        return true;
    }

    public static boolean walkAbsolute(Entity target, Direction direction, boolean powered) {
        if (!powered) return true;
        Vec3 to = new Vec3(direction.getStepX() * WALK_SPEED, 0., direction.getStepZ() * WALK_SPEED);
//        if (target instanceof Mob mob)
//            mob.getNavigation().moveTo(mob.getX() + to.x, mob.getY(), mob.getZ() + to.z, 1.);
//        else
            target.move(MoverType.PLAYER, to);
        return true;
    }

    public static boolean stopWalk(Entity target, boolean powered) {
        if (target instanceof Player player) {
            KeyMapController.FORWARD.forceKeyState(player, false);
            KeyMapController.BACK.forceKeyState(player, false);
            KeyMapController.LEFT.forceKeyState(player, false);
            KeyMapController.RIGHT.forceKeyState(player, false);
            return true;
        }
        target.setDeltaMovement(new Vec3(0., target.getDeltaMovement().y, 0.));
        return true;
    }

    public static boolean jump(Entity target, boolean powered) {
        if (!powered || !target.onGround()) return false;
        if (target instanceof Player player) {
            KeyMapController.JUMP.forceKeyState(player, true);
            Events.delayedTask(3, ()->KeyMapController.JUMP.forceKeyState(player, false));
            return true;
        }
        if (target instanceof LivingEntity living) {
            living.jumpFromGround();
            return true;
        }
        return false;
    }

    public static boolean fly(Entity target, Direction direction, boolean powered) {
        if (target instanceof Player player) {
            switch (direction) {
                case NORTH -> KeyMapController.FORWARD.forceKeyState(player, powered);
                case SOUTH -> KeyMapController.BACK.forceKeyState(player, powered);
                case WEST -> KeyMapController.LEFT.forceKeyState(player, powered);
                case EAST -> KeyMapController.RIGHT.forceKeyState(player, powered);
                case UP -> KeyMapController.JUMP.forceKeyState(player, powered);
                case DOWN -> KeyMapController.SNEAK.forceKeyState(player, powered);
            }
            return true;
        }
        if (!powered) return false;
        double rot = target.getYRot();
        switch (direction) {
            case SOUTH -> rot += Math.PI;
            case WEST -> rot += Math.PI * 0.5;
            case EAST -> rot += Math.PI * 1.5;
        }
        Vec3 dir = new Vec3(Math.sin(-rot) * WALK_SPEED, direction == Direction.UP ? WALK_SPEED : direction == Direction.DOWN ? -WALK_SPEED : 0., Math.cos(rot) * WALK_SPEED);
        if (target instanceof Mob mob)
            mob.getNavigation().moveTo(mob.getX() + dir.x, mob.getY() + dir.z, mob.getZ() + dir.z, 1.);
        else
            target.move(MoverType.PLAYER, dir);
        return true;
    }

    public static boolean stopFly(Entity target, boolean powered) {
        if (!powered) return false;
        if (target instanceof Player player) {
            KeyMapController.FORWARD.forceKeyState(player, false);
            KeyMapController.BACK.forceKeyState(player, false);
            KeyMapController.LEFT.forceKeyState(player, false);
            KeyMapController.RIGHT.forceKeyState(player, false);
            KeyMapController.JUMP.forceKeyState(player, false);
            KeyMapController.SNEAK.forceKeyState(player, false);
            return true;
        }
        target.setDeltaMovement(new Vec3(0., 0., 0.));
        return true;
    }

    public static boolean sneak(Entity target, boolean powered) {
        if (target instanceof Player player) {
            KeyMapController.SNEAK.forceKeyState(player, powered);
            return true;
        }
        return false;
    }

    public static boolean stopSneak(Entity target, boolean powered) {
        if (!powered) return false;
        if (target instanceof Player player) {
            KeyMapController.SNEAK.forceKeyState(player, false);
            return true;
        }
        return false;
    }


    public static boolean inventory(Entity target, boolean powered) {
        if (target instanceof Player player) {
            EntityEvents.INVENTORY.forceKeyState(player, powered);
            return true;
        }
        return false;
    }

    public static boolean attack(Entity target, boolean powered) {
        if (target instanceof Player player) {
            KeyMapController.ATTACK.forceKeyState(player, powered);
            return true;
        }
        //FIXME: missing attack code for generic entity
        return false;
    }

    public static boolean stopAttack(Entity target, boolean powered) {
        if (!powered) return false;
        if (target instanceof Player player) {
            KeyMapController.ATTACK.forceKeyState(player, false);
            return true;
        }
        return false;
    }

    public static boolean interact(Entity target, boolean powered) {
        if (target instanceof Player player) {
            KeyMapController.USE.forceKeyState(player, powered);
            return true;
        }
        //FIXME: missing interact code for generic entity
        return false;
    }

    public static boolean stopInteract(Entity target, boolean powered) {
        if (!powered) return false;
        if (target instanceof Player player) {
            KeyMapController.USE.forceKeyState(player, false);
            return true;
        }
        return false;
    }

    public static boolean chat(Entity target, String text, boolean powered) {
        if (!powered) return false;
        if (target.level() instanceof ServerLevel level) {
            String trimmed = text.trim();
            boolean isCommand = trimmed.startsWith("/");
            MinecraftServer server = level.getServer();
            CommandSourceStack cmd = new CommandSourceStack(target, target.position(), target.getRotationVector(), level, isCommand ? 0 : 4, target.getName().getString(), target.getDisplayName(), server, target);
            if (isCommand)
                return server.getCommands().performPrefixedCommand(cmd, trimmed) > 0;
            else
                return server.getCommands().performPrefixedCommand(cmd, "/say " + text) > 0;
        }
        return false;
    }

    public static boolean log(Entity target, String text, boolean powered) {
        if (!powered) return false;
        if (target instanceof Player player) {
            player.displayClientMessage(Component.literal(text), true);
            return true;
        }
        return false;
    }

    public static boolean look(Entity target, float y, float x, boolean powered) {
        if (!powered) return false;
        y = y * (float)Math.PI / 180f;
        x = x * (float)Math.PI / 180f;
        target.setYHeadRot(y);
        target.setYRot(y);
        target.setXRot(x);
        return true;
    }
}
