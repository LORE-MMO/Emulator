package nereus.discord.tasks;

import nereus.config.ConfigData;
import nereus.db.objects.Item;
import nereus.world.World;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AirDrop implements Runnable
{
    private World world;
    private Random rand;
    private String emoji = "\uD83D\uDC3B";
    private Message message;
    private MessageCreateEvent event;
    private TextChannel textChannel;
    private List<Long> excludeList = new ArrayList<Long>();
    private Item item;
    private int quantity;

    public AirDrop(World world, Message message, MessageCreateEvent event, TextChannel textChannel, Item item, int quantity)
    {
        this.world = world;
        this.message = message;
        this.event = event;
        this.textChannel = textChannel;
        this.rand = new Random(System.currentTimeMillis());
        this.item = item;
        this.quantity = quantity;
    }

    private User getRandomUser(MessageCreateEvent event, List<User> reactors) throws ExecutionException, InterruptedException
    {
        User user = reactors.get(this.rand.nextInt(reactors.size()));
        if (reactors.size() - 1 == this.excludeList.size()) {
            return null;
        }
        if (user.isBot()) {
            return this.getRandomUser(event, reactors);
        }
        if (this.excludeList.contains(user.getId())) {
            return this.getRandomUser(event, reactors);
        }
        if (user == event.getMessageAuthor().asUser().get()) {
            this.excludeList.add(user.getId());
            return this.getRandomUser(event, reactors);
        }
        return user;
    }

    @Override
    public void run()
    {
        try {
            Date dt = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            c.add(Calendar.DATE, 1);
            dt = c.getTime();

            Date expire = new Date();
            Reaction reaction = (Reaction)this.message.getReactionByEmoji(this.emoji).get();
            List reactors = (List)reaction.getUsers().get();
            User user = this.getRandomUser(this.event, reactors);
            if (user == null) {
                this.textChannel.sendMessage("It looks like we ran out of contestants, I am now closing the dropbox.");
                return;
            }

            String code = this.generateRedeemCode(this.world, this.item, this.quantity);
            world.db.jdbc.run("INSERT INTO redeems (Code, Enabled, Limited, Quantity, QuantityLeft, ItemID, Gold, Coins, Exp, ClassPoints, DateExpiry) VALUES (?, 1, 1, ?, 1, ?, 0, 0, 0, 0, ?)", code, quantity, item.getId(), dt);

            EmbedBuilder second = new EmbedBuilder();
            second.setAuthor("Duarrrr!", null, ConfigData.DISCORD_BOT_AVATAR);
            second.setDescription("Congratulation <@" + user.getId() + ">!, Please check your **Private Messages** for the Redeem Code!");
            second.setColor(Color.BLACK);
            second.setThumbnail(user.getAvatar());
            second.setFooter("" + user.getId());
            second.setImage("https://cdn.discordapp.com/attachments/722140132085465169/873665623891279902/endro-seira.gif");
//            second.setUrl(ConfigData.SERVER_GAME_LINK);
            this.textChannel.sendMessage(second);

            EmbedBuilder third = new EmbedBuilder();
            third.setAuthor("Discord AirDrop Winner!", null, ConfigData.DISCORD_BOT_AVATAR);
//            **[" + world.clearHTMLTags(item.getName()) + "](" + ConfigData.SERVER_PROFILE_LINK + item.getId() + ") x" + Quantity + "**
            third.setDescription("Congratulation <@" + user.getId() + ">! You won **[" + world.clearHTMLTags(item.getName()) + "](" + ConfigData.SERVER_PROFILE_LINK + item.getId() + ") x" + quantity + "** from discord air box event! Your redeem code is: **" + code + "**, Remember! Your Redeem Code will expire 24 Hours from now.");
            third.setColor(Color.BLACK);
            third.setThumbnail(event.getMessageAuthor().getAvatar());
            third.setFooter("" + user.getId());
            user.sendMessage(third);

            this.world.sendToUsers(new String[]{"serverevent", "Our discord dropbox lucky winner is " + user.getName() + "!"});
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private String generateRedeemCode(World world, Item item, int quantity)
    {
        String Code = this.generateRandomString() + '-' + this.generateRandomString() + '-' + this.generateRandomString();
        int rowcount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM redeems WHERE Code = ?", Code);
        if (rowcount >= 1) {
            this.generateRedeemCode(world, item, quantity);
        }
        return Code;
    }

    private String generateRandomString()
    {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; ++i) {
            int rndCharAt = random.nextInt(characters.length());
            char rndChar = characters.charAt(rndCharAt);
            sb.append(rndChar);
        }
        return sb.toString();
    }
}