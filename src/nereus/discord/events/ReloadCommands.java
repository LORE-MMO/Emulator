package nereus.discord.events;

import nereus.discord.Bot;
import nereus.world.World;
import java.util.AbstractMap;
import jdbchelper.QueryResult;

public class ReloadCommands {
    public ReloadCommands(World world) {
        if (!Bot.commands.isEmpty()) {
            Bot.commands.clear();
        }
        QueryResult rs = world.db.jdbc.query("SELECT Command, Script, AccessID FROM discord_commands WHERE Enabled = 1", new Object[0]);
        while (rs.next()) {
            String Command = rs.getString("Command");
            String Script = rs.getString("Script");
            int AccessID = rs.getInt("AccessID");
            Bot.commands.put(Command, new AbstractMap.SimpleEntry<String, Integer>(Script, AccessID));
        }
        rs.close();
    }
}

