package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;

public class RequestSetStatus implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        if (command.equalsIgnoreCase("$status")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Set Status", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$status <playing|listening|watching|competing|streaming> <name>`");
            embed.setFooter("Sets the bot status.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
            event.getChannel().sendMessage(embed);
        } else {
            String Type = command.split(" ")[1];
            String status = command.replaceFirst("\\$" + "status " + Type, "");
            switch (Type) {
                case "playing":
                    world.bot.api.updateActivity(ActivityType.PLAYING, status);
                    break;
                case "listening":
                    world.bot.api.updateActivity(ActivityType.LISTENING, status);
                    break;
                case "watching":
                    world.bot.api.updateActivity(ActivityType.WATCHING, status);
                    break;
                case "competing":
                    world.bot.api.updateActivity(ActivityType.COMPETING, status);
                    break;
                case "streaming":
                    world.bot.api.updateActivity(ActivityType.STREAMING, status);
                    break;
            }
            event.getChannel().sendMessage("Successfully set my status to " + Type + " " + status).exceptionally(ExceptionLogger.get((Class[])new Class[] {MissingPermissionsException.class}));
        }
    }
}
