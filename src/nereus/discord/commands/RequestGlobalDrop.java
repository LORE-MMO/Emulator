package nereus.discord.commands;

import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import nereus.config.ConfigData;
import nereus.db.objects.Item;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class RequestGlobalDrop implements IDiscord
{
    private Room room;

    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        if (command.equalsIgnoreCase("$globaldrop")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Global Drop", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$globaldrop <item id> <quantity>`");
            embed.setFooter("Drops the item globally.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
            event.getChannel().sendMessage(embed);
        } else {
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[1])) {
                throw new CommandException("Your first key is not a numeric", event);
            }
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[2])) {
                throw new CommandException("Your second key is not a numeric", event);
            }

            int ItemID = Integer.parseInt(command.split(" ")[1]);
            int Quantity = Integer.parseInt(command.split(" ")[2]);
            Item item = world.items.get(ItemID);
            if (item == null) {
                event.getChannel().sendMessage("There is no such item as Item ID: " + ItemID);
                return;
            }

            LinkedList listOfChannels = world.zone.getAllUsersInZone();
            Iterator var52 = listOfChannels.iterator();
            while (var52.hasNext()) {
                Object temp = var52.next();
                User tgt = ExtensionHelper.instance().getUserByChannel((SocketChannel) temp);
                if (tgt == null) continue;
                world.send(new String[]{"server", "You won the item " + item.getName() + " from a discord global drop."}, tgt);
                world.users.dropItem(tgt, ItemID, Quantity);
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Announcements!", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.setDescription("A global drop with the item **[" + world.clearHTMLTags(item.getName()) + "](" + ConfigData.SERVER_PROFILE_LINK + item.getId() + ")** has been rewarded to the users who are online at the server **" + ConfigData.SERVER_NAME + "**.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail("https://cdn.discordapp.com/attachments/722140132085465169/873874005017362442/treasure.png");
//            embed.setUrl(ConfigData.SERVER_GAME_LINK);
//            embed.setThumbnail(event.getMessageAuthor().getAvatar());
            embed.setFooter("" + event.getMessageAuthor().getId());
            event.getChannel().sendMessage(embed).exceptionally(ExceptionLogger.get((Class[])new Class[]{MissingPermissionsException.class}));
        }
    }
}
