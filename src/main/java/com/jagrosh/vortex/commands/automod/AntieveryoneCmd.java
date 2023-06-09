/*
 * Copyright 2018 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.vortex.commands.automod;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import com.jagrosh.vortex.Vortex;
import com.jagrosh.vortex.commands.CommandExceptionListener;
import com.jagrosh.vortex.database.managers.AutomodManager;
import com.jagrosh.vortex.database.managers.PunishmentManager;
import com.jagrosh.vortex.utils.FormatUtil;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class AntieveryoneCmd extends Command
{
    private final Vortex vortex;
    
    public AntieveryoneCmd(Vortex vortex)
    {
        this.vortex = vortex;
        this.name = "antieveryone";
        this.guildOnly = true;
        this.aliases = new String[]{"antiateveryone", "anti-everyone", "anti-ateveryone"};
        this.category = new Category("AutoMod");
        this.arguments = "<strikes>";
        this.help = "sets strikes for failed @\u0435veryone/here attempts"; // cyrillic e
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Please provide a number of strikes!");
            return;
        }
        int numstrikes;
        try
        {
            numstrikes = Integer.parseInt(event.getArgs());
        }
        catch(NumberFormatException ex)
        {
            if(event.getArgs().equalsIgnoreCase("none") || event.getArgs().equalsIgnoreCase("off"))
                numstrikes = 0;
            else
            {
                event.replyError(FormatUtil.filterEveryone("`"+event.getArgs()+"` is not a valid integer!"));
                return;
            }
        }
        if(numstrikes<0 || numstrikes>AutomodManager.MAX_STRIKES)
            throw new CommandExceptionListener.CommandErrorException("The number of strikes must be between 0 and " + AutomodManager.MAX_STRIKES);
        if(numstrikes > 0 && !vortex.getDatabase().actions.hasPunishments(event.getGuild()))
            throw new CommandExceptionListener.CommandErrorException("Anti-Everyone cannot be enabled without first setting at least one punishment.");
        
        vortex.getDatabase().automod.setEveryoneStrikes(event.getGuild(), numstrikes);
        event.replySuccess("Users will now receive `"+numstrikes+"` strikes for attempting to ping @\u0435veryone/here. " // cyrillic e
                + "This also considers pingable roles called 'everyone' and 'here'. This will not affect users that actually "
                + "have permission to ping everyone. Pinging oneself or bots do not count towards this total. Duplicate "
                + "pings in the same message are also not counted towards this total (pinging the same person 3 times only "
                + "counts as 1 ping).");
    }
}
