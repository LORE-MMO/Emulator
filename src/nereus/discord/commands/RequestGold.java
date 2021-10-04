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

import java.awt.*;
import java.text.NumberFormat;

public class RequestGold implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$gold")) {
            embed.setAuthor("Gold", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$gold <add|remove|set> <user id> <value>`");
            embed.setFooter("This command is used to give gold to someone.");
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

            String username = world.db.jdbc.queryForString("SELECT username FROM users WHERE id = ?", UserID);
            User user = world.zone.getUserByName(username.toLowerCase());

            embed.setColor(Color.BLACK);
            embed.setThumbnail(ConfigData.DISCORD_BOT_AVATAR);

            JSONObject var16 = new JSONObject();

            switch (Type) {
                case "add":
                    var16.put("cmd", "goldAdd");
                    var16.put("intAmount", Value);
                    var16.put("CharItemID", UserID);
                    var16.put("bGold", 1);

                    world.send(var16, user);
                    world.db.jdbc.run("UPDATE users SET Gold = (Gold + ?) WHERE id = ?", Value, UserID);
                    world.send(new String[] {"server", "An in game staff has given you " + Value + " Golds."}, user);

                    embed.setAuthor("Gold Added!", null, event.getMessageAuthor().getAvatar());
                    embed.setFooter("" + event.getMessageAuthor().getId());
                    embed.setDescription("<@" + event.getMessageAuthor().getId() + "> added **" + currency.format(Value) + " Gold(s)** to user **[" + username + "](" + ConfigData.SERVER_PROFILE_LINK + username + ")**.");
                    break;
                case "remove":
                    world.db.jdbc.run("UPDATE users SET Gold = (Gold - ?) WHERE id = ?", Value, UserID);
                    world.send(new String[] {"server", "An in game staff has remove " + Value + " Gold(s) from your inventory."}, user);

                    embed.setAuthor("Gold Removed!", null, event.getMessageAuthor().getAvatar());
                    embed.setFooter("" + event.getMessageAuthor().getId());
                    embed.setDescription("<@" + event.getMessageAuthor().getId() + "> removed **" + currency.format(Value) + " Gold(s)** from user **[" + username + "](" + ConfigData.SERVER_PROFILE_LINK + username + ")**.");
                    break;
                case "set":
                    var16.put("cmd", "goldSet");
                    var16.put("id", UserID);
                    var16.put("intAmount", Value);
                    var16.put("CharItemID", UserID);
                    var16.put("bGold", 1);

                    world.send(var16, user);
                    world.db.jdbc.run("UPDATE users SET Gold = ? WHERE id = ?", Value, UserID);
                    world.send(new String[] {"server", "An in game staff has remove " + Value + " Gold(s) from your inventory."}, user);

                    embed.setAuthor("Gold are set!", null, event.getMessageAuthor().getAvatar());
                    embed.setFooter("" + event.getMessageAuthor().getId());
                    embed.setDescription("<@" + event.getMessageAuthor().getId() + "> set **Gold(s)** for user **[" + username + "](" + ConfigData.SERVER_PROFILE_LINK + username + ")** to **" + currency.format(Value) + "**");
                    break;
            }
        }
        event.getChannel().sendMessage(embed);
    }
}
