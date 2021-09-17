package nereus.discord.commands;

import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.javacord.api.event.message.MessageCreateEvent;

public class DaeTest implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
//        String command = event.getMessageContent();
        if (event.getMessageContent().split(" ")[1].isEmpty()) event.getChannel().sendMessage("all args: NOW()");

    }
}
