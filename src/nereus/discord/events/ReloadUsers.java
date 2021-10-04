package nereus.discord.events;

import nereus.discord.Bot;
import nereus.world.World;
import java.util.AbstractMap;
import jdbchelper.QueryResult;

public class ReloadUsers {
    public ReloadUsers(World world) {
        if (!Bot.users.isEmpty()) {
            Bot.users.clear();
        }
        QueryResult rs = world.db.jdbc.query("SELECT * FROM discord_users", new Object[0]);
        while (rs.next()) {
            String id = rs.getString("id");
            String Name = rs.getString("Name");
            int AccessID = rs.getInt("AccessID");
            Bot.users.put(id, new AbstractMap.SimpleEntry<String, Integer>(Name, AccessID));
        }
        rs.close();
    }
}

