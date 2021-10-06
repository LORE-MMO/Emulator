package nereus.discord.commands;

import it.gotoandplay.smartfoxserver.data.User;
import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.text.NumberFormat;

public class RequestCoin implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$coin")) {
            embed.setAuthor("Coin", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$coin <add|remove|set> <user id> <value>`");
            embed.setFooter("This command is used to give coins to someone.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
        } else {
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[2])) {
                throw new CommandException("Your first key is not a numeric", event);
            }
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[3])) {
                throw new CommandException("Your second key is not a numeric", event);
            }

            String Type = command.split(" ")[1];
            int UserID = Integer.parseInt(command.split(" ")[2]);
            int Value = Integer.parseInt(command.split(" ")[3]);
            NumberFormat currency = NumberFormat.getInstance();

            String username = world.db.jdbc.queryForString("SELECT a.username FROM users a JOIN users b ON b.id = a.id WHERE a.id = ? LIMIT 1", UserID);
            User user = world.zone.getUserByName(username.toLowerCase());

            embed.setColor(Color.BLACK);
            embed.setThumbnail(ConfigData.DISCORD_BOT_AVATAR);
            embed.setImage("https://cdn.discordapp.com/attachments/722140132085465169/873268314028245002/ganyu-dance.gif");

            JSONObject var16 = new JSONObject();

            switch (Type) {
                case "add":
                    var16.put("cmd", "coinAdd");
                    var16.put("intAmount", Value);
                    var16.put("CharItemID", UserID);
                    world.send(var16, user);

                    world.db.jdbc.run("UPDATE users SET Coins = (Coins + ?) WHERE id = ?", Value, UserID);
                    world.send(new String[] {"server", "An in game staff has given you " + Value + " Coins."}, user);

                    embed.setAuthor("Coin Added!", null, event.getMessageAuthor().getAvatar());
                    embed.setFooter("" + event.getMessageAuthor().getId());
                    embed.setDescription("<@" + event.getMessageAuthor().getId() + "> added **" + currency.format(Value) + " Coins** to user **[" + username + "](" + ConfigData.SERVER_PROFILE_LINK + username + ")**.");
                    break;
                case "remove":
                    world.db.jdbc.run("UPDATE users SET Coins = (Coins - ?) WHERE id = ?", Value, UserID);
                    world.send(new String[] {"server", "An in game staff has remove " + Value + " Coins from your inventory."}, user);

                    embed.setAuthor("Coin Removed!", null, event.getMessageAuthor().getAvatar());
                    embed.setFooter("" + event.getMessageAuthor().getId());
                    embed.setDescription("<@" + event.getMessageAuthor().getId() + "> removed **" + currency.format(Value) + " Coins** from user **[" + username + "](" + ConfigData.SERVER_PROFILE_LINK + username + ")**.");
                    break;
                case "set":
                    var16.put("cmd", "coinSet");
                    var16.put("id", UserID);
                    var16.put("intCoins", Integer.valueOf(Value));
                    var16.put("CharItemID", UserID);

                    world.send(var16, user);
                    world.db.jdbc.run("UPDATE users SET Coins = ? WHERE id = ?", Value, UserID);
                    world.send(new String[] {"server", "An in game staff has remove " + Value + " Coins from your inventory."}, user);

                    embed.setAuthor("Coin are set!", null, event.getMessageAuthor().getAvatar());
                    embed.setFooter("" + event.getMessageAuthor().getId());
                    embed.setDescription("<@" + event.getMessageAuthor().getId() + "> set **Coins** for user **[" + username + "](" + ConfigData.SERVER_PROFILE_LINK + username + ")** to **" + currency.format(Value) + "**");
//                    embed.setDescription("<@" + event.getMessageAuthor().getId() + "> set user **" + currency.format(Value) + "** for user **[" + username + "](" + ConfigData.SERVER_PROFILE_LINK + username + ")**.");
                    break;
            }
        }
        event.getChannel().sendMessage(embed);
    }
}
