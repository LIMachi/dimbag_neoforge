package com.limachi.dim_bag;

import com.limachi.dim_bag.utils.annotations.CmdArg;
import com.limachi.dim_bag.utils.annotations.RegisterCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class Commands {
    @RegisterCommand("dim_bag enter <id>")
    @RegisterCommand("dim_bag enter <id> <proxy>")
    @RegisterCommand("dim_bag enter <id> <entities>")
    @RegisterCommand
    public static int enter(CommandContext<CommandSourceStack> ctx, @CmdArg("id") int id, @CmdArg("entities") Entity[] entities, @CmdArg("proxy") boolean proxy) {
//        int res = BagsData.runOnBag(id, b->{
//            if (entities == null || entities.isEmpty())
//                return source.getPlayer() != null ? b.enter(source.getPlayer(), proxy) != null ? 1 : 0 : 0;
//            else
//                return (int)entities.stream().filter(e->b.enter(e, proxy) != null).count();
//        }, -1);
//        if (res == -1) {
//            source.sendFailure(Component.translatable("command.error.bag_not_exist"));
//            return 0;
//        }
//        return res;
        return 0;
    }
}
