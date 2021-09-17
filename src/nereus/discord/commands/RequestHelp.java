package nereus.discord.commands;

import jdbchelper.QueryResult;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.util.Iterator;

public class RequestHelp implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        EmbedBuilder embed = new EmbedBuilder();
//        embed.setDescription("A list of discord server commands with their required access.");

        JSONArray commands = new JSONArray();
        QueryResult rs = world.db.jdbc.query("SELECT * FROM discord_commands WHERE Enabled = 1");
        while (rs.next()) {
            int AccessID = rs.getInt("AccessID");
            String Command = rs.getString("Command");
            String Description = rs.getString("Description");
            String Parameters = rs.getString("Parameters");

            JSONObject command = new JSONObject();
            command.put("AccessID", Integer.valueOf(AccessID));
            command.put("Command", Command);
            command.put("Description", Description);
            command.put("Parameters", Parameters);
            commands.add(command);
        }
        rs.close();

        StringBuilder string = new StringBuilder();

//        embed.setTitle("Discord Commands");
        embed.setAuthor("Discord Commands", null, event.getMessageAuthor().getAvatar());
//        embed.setThumbnail(event.getMessageAuthor().getAvatar());
        embed.setFooter("A list of discord server commands with their required access.");
        embed.setColor(Color.BLACK);
        embed.setImage("https://cdn.discordapp.com/attachments/722140132085465169/873280500314288198/genshin-dance.gif");
//        embed.setThumbnail(event.getMessageAuthor().getAvatar());
        string.append("\n\n**Player Commands**");
        Iterator<?> iterate;

        for (iterate = commands.iterator(); iterate.hasNext(); ) {
            JSONObject command = (JSONObject)iterate.next();
            if (command.get("AccessID").toString().equals("1"))
                string.append("\n`" + command.get("Command") + " " + command.get("Parameters") + "` - " + command.get("Description"));
        }

        string.append("\n\n**Moderator Commands**");
        for (iterate = commands.iterator(); iterate.hasNext(); ) {
            JSONObject command = (JSONObject)iterate.next();
            if (command.get("AccessID").toString().equals("40"))
                string.append("\n`" + command.get("Command") + " " + command.get("Parameters") + "` - " + command.get("Description"));
        }

        string.append("\n\n**Administrator Commands**");
        for (iterate = commands.iterator(); iterate.hasNext(); ) {
            JSONObject command = (JSONObject)iterate.next();
            if (command.get("AccessID").toString().equals("60"))
                string.append("\n`" + command.get("Command") + " " + command.get("Parameters") + "` - " + command.get("Description"));
        }
        embed.setDescription(string.toString());
        event.getChannel().sendMessage(embed).exceptionally(ExceptionLogger.get(new Class[] { MissingPermissionsException.class }));
    }
}
