package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.db.objects.Item;
import nereus.discord.tasks.AirDrop;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RequestAirDrop implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        if (command.equalsIgnoreCase("$airdrop")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Drop Box", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$dropbox <item id> <quantity>`");
            embed.setFooter("Hosts a airdrop event.");
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
            if (!world.bot.api.getTextChannelById(ConfigData.DISCORD_GENERAL_CHANNELID).isPresent()) {
                event.getChannel().sendMessage("Discord Channel ID: " + ConfigData.DISCORD_GENERAL_CHANNELID + " Not Found.");
                return;
            }
            world.sendToUsers(new String[] {"serverevent", "A dropbox has spawned into our discord channel containing " + item.getName() + ", hop in to win!"});
            TextChannel textchannel = (TextChannel)world.bot.api.getTextChannelById(ConfigData.DISCORD_GENERAL_CHANNELID).get();

            EmbedBuilder first = new EmbedBuilder();
            first.setAuthor("An airdrop has landed!", null, ConfigData.DISCORD_BOT_AVATAR);
//            first.setTitle("An airdrop has landed!");
            first.setDescription("This airdrop containing **[" + world.clearHTMLTags(item.getName()) + "](" + ConfigData.SERVER_PROFILE_LINK + item.getId() + ") x" + Quantity + "** has just landed. React \uD83D\uDC3B to join.");
            first.setColor(Color.BLACK);
            first.setImage("https://cdn.discordapp.com/attachments/722140132085465169/873649323013967903/kanna-kamui-angry.gif");
            first.setFooter("" + event.getMessageAuthor().getId());
//            first.setThumbnail(event.getMessageAuthor().getAvatar());
            try {
                Message message = (Message)textchannel.sendMessage(first).get();
                message.addReaction("\uD83D\uDC3B");
                world.bot.api.getThreadPool().getScheduler().schedule(new AirDrop(world, message, event, textchannel, item, Quantity), 15L, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
