package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;

public class ServerRates implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        EmbedBuilder embed = new EmbedBuilder();
        StringBuilder string = new StringBuilder();

        embed.setAuthor("Server Rates", null, ConfigData.DISCORD_BOT_AVATAR);
        embed.setDescription("The server **" + ConfigData.SERVER_NAME + "** is currently running the rates:");

        embed.addField("Experience", world.EXP_RATE + "x", true);
//        embed.addField("Drop", " " + world.DROP_RATE, true);
        embed.addField("Reputation", " " + world.REP_RATE + "x", true);
        embed.addField("Class Points", " " + world.CP_RATE + "x");
//        embed.addField("Gold", " " + world.GOLD_RATE);

//        embed.addInlineField("Experience", " " + world.EXP_RATE);
        embed.addInlineField("Drop", " " + world.DROP_RATE + "x");
//        embed.addInlineField("Reputation", " " + world.REP_RATE);
//        embed.addInlineField("Class Points", " " + world.CP_RATE);
        embed.addInlineField("Gold", " " + world.GOLD_RATE + "x");

//        string.append("\n **Experience:** x" + world.EXP_RATE);
//        string.append("\n **Drop:** x" + world.DROP_RATE);
//        string.append("\n **Reputation:** x" + world.REP_RATE);
//        string.append("\n **Class Point:** x" + world.CP_RATE);
//        string.append("\n **Gold:** x" + world.GOLD_RATE);

        embed.setColor(Color.BLACK);
        embed.setImage("https://cdn.discordapp.com/attachments/699575783144030278/873180088127094784/Screenshot_20210806-202410_YouTube.jpg");
        embed.setThumbnail(event.getMessageAuthor().getAvatar());

        event.getChannel().sendMessage(embed).exceptionally(ExceptionLogger.get((Class[])new Class[]{MissingPermissionsException.class}));
    }
}
