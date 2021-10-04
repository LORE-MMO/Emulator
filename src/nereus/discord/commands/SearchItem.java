package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;

import jdbchelper.QueryResult;

import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;

public class SearchItem implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$item")) {
            embed.setAuthor("Item Search", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$item <name>`");
            embed.setFooter("This command is used to search items by name.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
        } else {
            int count = 0;
            String name = event.getMessageContent().split(" ")[1];

            QueryResult rs = world.db.jdbc.query("SELECT * FROM items WHERE Name LIKE ? LIMIT 10", "%" + name.toLowerCase() + "%");
            while (rs.next()) {
                int id = rs.getInt("id");
                String iName = rs.getString("Name");
                String iType = rs.getString("Type");

                embed.addInlineField("ID", " " + id);
                embed.addInlineField("Name", "[" + world.clearHTMLTags(iName) + "](" + ConfigData.SERVER_PROFILE_LINK + id + ")");
                embed.addInlineField("Type", " " + iType);

//                embed.addField(world.clearHTMLTags(iName), "[" + iType + "](" + ConfigData.SERVER_PROFILE_LINK + id + ")", true);
                ++count;
            }
            rs.close();

            embed.setColor(Color.BLACK);
            embed.setThumbnail(ConfigData.DISCORD_BOT_AVATAR);
            embed.setAuthor("Search Item", null, event.getMessageAuthor().getAvatar());
            embed.setDescription("Here is an accurate list of the searched keyword: **'" + name + "'**.\nThere are **" + count + "** results for your keyword: " + name + ".");
//            embed.setDescription("About " + count + " result(s) of " + name);
        }
        event.getChannel().sendMessage(embed);
    }
}
