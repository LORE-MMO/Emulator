package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.discord.Bot;
import nereus.discord.events.ReloadUsers;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;

public class RequestDemote implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$demote")) {
            embed.setAuthor("Demote", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$demote <@someone>`");
            embed.setFooter("This command is used to demote someone.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
        } else {
            for (User user : event.getMessage().getMentionedUsers()) {
                if (user.getIdAsString().equals("514722220090851328")) {
                    event.getChannel().sendMessage("You cannot demote my creator you dummy little shit.").exceptionally(ExceptionLogger.get((Class[])new Class[]{MissingPermissionsException.class}));
                    continue;
                }

                if (Bot.users.containsKey(user.getIdAsString())) {
                    world.db.jdbc.run("DELETE FROM discord_users WHERE id = ?", new Object[]{user.getIdAsString()});
                    event.getChannel().sendMessage("Demoted the user " + user.getName() + ".").exceptionally(ExceptionLogger.get((Class[])new Class[]{MissingPermissionsException.class}));
                    continue;
                }
                event.getChannel().sendMessage("User " + user.getName() + " doesn't have a role in this server!").exceptionally(ExceptionLogger.get((Class[])new Class[]{MissingPermissionsException.class}));
            }
            new ReloadUsers(world);
        }
        event.getChannel().sendMessage(embed);
    }
}
