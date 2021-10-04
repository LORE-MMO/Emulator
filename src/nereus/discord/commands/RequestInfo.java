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
import java.text.SimpleDateFormat;

public class RequestInfo implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$id")) {
            embed.setAuthor("ID", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$id <user id>`");
            embed.setFooter("This command is used to check user information from user's id.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
        } else {
            String UserID = command.split(" ")[1];
            NumberFormat currency = NumberFormat.getInstance();

            QueryResult rs = world.db.jdbc.query("SELECT a.*, b.Name as Rank FROM users a JOIN access b ON b.id = a.Access WHERE a.id = ?", UserID);
            while (rs.next()) {
                String Username = rs.getString("username");
                String Rank = rs.getString("Rank");
                String DateCreated = (new SimpleDateFormat("yyyy-MM-dd")).format(rs.getDate("DateCreated"));
                String LastLogin = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a")).format(rs.getDate("LastLogin"));
                int Gold = rs.getInt("Gold");
                int Coins = rs.getInt("Coins");

                embed.setDescription("[" + Username + "](" + ConfigData.SERVER_PROFILE_LINK + Username + ")");
                embed.addField("Information", "Account created at **" + DateCreated + "**, Last seen on **" + LastLogin + "**.");
                embed.addInlineField("Status", " " + Rank);
                embed.addInlineField("Gold", " " + currency.format(Gold));
                embed.addInlineField("Coins", " " + currency.format(Coins));
            }
            rs.close();
            embed.setTitle("Username");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(ConfigData.DISCORD_BOT_AVATAR);
        }
        event.getChannel().sendMessage(embed);
    }
}