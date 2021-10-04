package nereus.discord.commands;

import jdbchelper.QueryResult;
import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

public class RequestMapName implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$map")) {
            embed.setAuthor("Map Search", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$map <name>`");
            embed.setFooter("This command is used to search map by name.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
        } else {
            int count = 0;
            String name = event.getMessageContent().split(" ")[1];

            QueryResult rs = world.db.jdbc.query("SELECT * FROM maps WHERE Name LIKE ? LIMIT 10", "%" + name.toLowerCase() + "%");
            while (rs.next()) {
                int id = rs.getInt("id");
                String Name = rs.getString("Name");
                int MaxPlayers = rs.getInt("MaxPlayers");
                int ReqLevel = rs.getInt("ReqLevel");

                embed.addInlineField("Name", "[" + Name + "](" + ConfigData.SERVER_PROFILE_LINK + id + ")");
                embed.addInlineField("Max Players", " " + MaxPlayers);
                embed.addInlineField("Required Level", " " + ReqLevel);
                ++count;
            }
            rs.close();

            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
            embed.setAuthor("Search Map", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.setDescription("Here is an accurate list of the searched keyword: '**" + name + "**'.\nThere are **" + count + "** results for your keyword: " + name + ".");
        }
        event.getChannel().sendMessage(embed);
    }
}
