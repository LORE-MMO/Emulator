package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.discord.Bot;
import nereus.discord.events.ReloadUsers;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;

public class RequestPromote implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$promote")) {
            embed.setAuthor("Promote", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$promote <access> <@someone>`");
            embed.setFooter("This command is used to promote someone.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
        } else {
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[1])) {
                throw new CommandException("Your first key is not a numeric", event);
            }

            int access = Integer.parseInt(event.getMessageContent().split(" ")[1]);
            if (!Bot.accesses.containsKey(access)) {
                throw new CommandException("There is no such thing as access level: " + access, event);
            }

            for (User user : event.getMessage().getMentionedUsers()) {
                if (!Bot.users.containsKey(user.getIdAsString())) {
                    world.db.jdbc.run("INSERT INTO discord_users (id, AccessID, Name) VALUES (?, ?, ?)", user.getIdAsString(), access, user.getName());
                    event.getChannel().sendMessage("Promoted " + user.getName() + " to " + Bot.accesses.get(access) + ".").exceptionally(ExceptionLogger.get((Class[])new Class[]{MissingPermissionsException.class}));
                    continue;
                }
                event.getChannel().sendMessage("User " + user.getName() + " is already on a role, demote first and promote again!").exceptionally(ExceptionLogger.get((Class[])new Class[]{MissingPermissionsException.class}));
            }
            new ReloadUsers(world);
        }
        event.getChannel().sendMessage(embed);
    }
}
