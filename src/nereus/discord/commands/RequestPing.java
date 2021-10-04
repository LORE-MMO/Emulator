package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.InteractionType;

import java.awt.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class RequestPing implements IDiscord
{
    private static long inputTime;

    public static void setInputTime(long inputTimeLong) {
        inputTime = inputTimeLong;
    }

    private Color getColorByPing(long ping) {
        if (ping < 100)
            return Color.cyan;
        if (ping < 400)
            return Color.green;
        if (ping < 700)
            return Color.yellow;
        if (ping < 1000)
            return Color.orange;
        return Color.red;
    }

    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        long processing = new Date().getTime() - inputTime;
//        long ping = event.getJDA().getPing();
        long ping  = event.getMessage().getCreationTimestamp().until(event.getMessage().getCreationTimestamp(), ChronoUnit.MILLIS);
//        event.reply("Ping: ...", m -> m.editMessage("Ping: " + event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS) + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Pong", null, ConfigData.DISCORD_BOT_AVATAR);
        embed.setDescription(String.format("The bot took `%s` milliseconds to response.\nIt took `%s` milliseconds to parse the command and the ping is `%s` milliseconds.", processing + ping, processing, ping));
        embed.setFooter("This command is used to give coins to someone.");
        embed.setColor(getColorByPing(ping));
        embed.setThumbnail(event.getMessageAuthor().getAvatar());
        event.getChannel().sendMessage(embed);

//        event.getChannel().sendMessage(getColorByPing(ping) +"Monster data cleared.");
    }
}
