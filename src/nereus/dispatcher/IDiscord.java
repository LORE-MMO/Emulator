package nereus.dispatcher;

import nereus.world.World;
import org.javacord.api.event.message.MessageCreateEvent;

public interface IDiscord {
    public void process(World var1, MessageCreateEvent var2) throws CommandException;
}

