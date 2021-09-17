package nereus.discord.commands;

import jdbchelper.QueryResult;
import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.text.NumberFormat;

public class RequestSearchUser implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$user")) {
            embed.setAuthor("User", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$user <username>`");
            embed.setFooter("This command is used to search users by username.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
        } else {
            int count = 0;
            String username = event.getMessageContent().split(" ")[1];

            QueryResult rs = world.db.jdbc.query("SELECT a.*, b.Name as Rank FROM users a JOIN access b ON b.id = a.Access WHERE a.username LIKE ? LIMIT 20", "%" + username.toLowerCase() + "%");
            while (rs.next()) {
                String iName = rs.getString("username");
                int UserID = rs.getInt("id");
                int iLevel = rs.getInt("Level");
                int Gold = rs.getInt("Gold");
                int Coins = rs.getInt("Coins");
                NumberFormat currency = NumberFormat.getInstance();

                embed.addInlineField("ID", " " + UserID);
                embed.addInlineField("Username", "[" + iName + "](" + ConfigData.SERVER_PROFILE_LINK + iName + ")");
                embed.addInlineField("Level", " " + iLevel);

                ++count;
            }
            rs.close();
            embed.setColor(Color.BLACK);
            embed.setThumbnail(ConfigData.DISCORD_BOT_AVATAR);
            embed.setAuthor("Search User", null, event.getMessageAuthor().getAvatar());
            embed.setDescription("Here is an accurate list of the searched keyword: **'" + username + "'**.\nThere are **" + count + "** results for your keyword: " + username + ".");
        }
        event.getChannel().sendMessage(embed);
    }
}