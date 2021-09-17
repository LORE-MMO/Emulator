package nereus.discord.events;

import nereus.config.ConfigData;
import nereus.discord.Bot;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;

import java.awt.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class FetchCommand implements MessageCreateListener {
    private World world;

    public FetchCommand(World world) {
        this.world = world;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }

        String key = event.getMessageContent().split(" ")[0].toLowerCase();
        String discordId = event.getMessage().getAuthor().getIdAsString();
        try {
            if (Bot.commands.containsKey(key)) {
                Map.Entry<String, Integer> command = Bot.commands.get(key);
                Class<?> requestDefinition = Class.forName(Bot.settings.get("Path") + command.getKey());
                IDiscord request = (IDiscord)requestDefinition.newInstance();
                if (command.getValue() == 1) {
                    request.process(this.world, event);
                } else {
                    Map.Entry<String, Integer> user = Bot.users.get(discordId);
                    if (!Bot.commands.containsKey(key)) {
                        throw new CommandException("error!", event);
                    }
                    if (!Bot.users.containsKey(discordId)) {
                        throw new CommandException("You are not allowed to use this command!", event);
                    }
                    if (user.getValue() < command.getValue()) {
                        throw new CommandException("Your access level is not allowed for this command!", event);
                    }
                    request.process(this.world, event);
                    this.logCommand(event);
                }
            }
        } catch (CommandException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void logCommand(MessageCreateEvent event)
    {
        TextChannel channel = world.bot.api.getTextChannelById(ConfigData.DISCORD_LOGS_CHANNELID).get();
        Date date = new Date();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(event.getMessageAuthor().getDiscriminatedName() + " | " + event.getMessageContent().split(" ")[0], null, ConfigData.DISCORD_BOT_AVATAR);
        embed.addInlineField("User", "<@" + event.getMessageAuthor().getId() + ">");
        embed.addInlineField("Command", "" + event.getMessageContent());
        embed.addInlineField("Timestamp", "" + new Timestamp(date.getTime()));
        embed.setColor(Color.BLACK);
        embed.setThumbnail(event.getMessageAuthor().getAvatar());
        embed.setFooter("" + event.getMessageAuthor().getId());
        channel.sendMessage(embed);
        this.world.db.jdbc.run("INSERT INTO discord_users_logs (id, Name, Command) VALUES (?, ?, ?)", event.getMessage().getAuthor().getIdAsString(), event.getMessage().getAuthor().getDisplayName(), event.getMessageContent());
    }
}
