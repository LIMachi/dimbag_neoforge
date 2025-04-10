package com.limachi.dim_bag.utils.annotations;

import com.limachi.dim_bag.utils.commands.CommandManager;

import java.lang.annotation.*;

@Repeatable(CommandManager.RegisterCommands.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterCommand {
    /**
     * command pattern, ex:<br>
     * {@code @RegisterCommand("root cmd <int>")}<br>
     * {@code public static int myCommand(CommandContext<CommandSourceStack> ctx, @CmdArg("int") Integer test){ return test; }}<br>
     * <br>
     * would result in the command: `/root cmd 1` returning 1, `/root cmd 2` returning 2 and so on<br>
     * (leave empty to generate a command with the form: {@code /mod_id method_name <param1> <param2> ...})
     */
    String value() default "";

    /**
     * what minimum permission level is required to access this command (default to 2, accessible to command blocks)<br>
     * reminder:<br>
     * 0 = anyone, 1 = spawn protection access, 2 = command blocks, 3 = admin (ban, op, kick, whitelist, tick, ...), 4 = owner (publish, save, stop, ...)
     */
    int permissionLevel() default 2;
}
