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
        String command = event.getMessageContent();
        String args = command.split(" ")[0] + command.split(" ")[1] +command.split(" ")[2];
        event.getChannel().sendMessage("args 0: " + command.split(" ")[0] + ", args 1: " + command.split(" ")[1] + ", args 2: " + command.split(" ")[2]);
        event.getChannel().sendMessage("all args: " + args);
    }
}
