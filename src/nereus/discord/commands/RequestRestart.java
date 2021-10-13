package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.tasks.Restart;
import nereus.world.World;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.concurrent.TimeUnit;

public class RequestRestart implements IDiscord {
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        if (command.equalsIgnoreCase("$restart")) {
            event.getChannel().sendMessage("Restarting the Server " + ConfigData.SERVER_NAME + ". Please logout to prevent data loss.").exceptionally(ExceptionLogger.get((Class[])new Class[]{MissingPermissionsException.class}));
            Restart restart = new Restart(world, null);
            restart.setRunning(world.scheduleTask(restart, 0L, TimeUnit.SECONDS));
        }
    }
}
