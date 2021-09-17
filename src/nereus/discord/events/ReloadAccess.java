package nereus.discord.events;

import nereus.discord.Bot;
import nereus.world.World;
import jdbchelper.QueryResult;

public class ReloadAccess {
    public ReloadAccess(World world) {
        if (!Bot.accesses.isEmpty()) {
            Bot.accesses.clear();
        }
        QueryResult rs = world.db.jdbc.query("SELECT * FROM access");
        while (rs.next()) {
            int id = rs.getInt("id");
            String Name = rs.getString("Name");
            Bot.accesses.put(id, Name);
        }
        rs.close();
    }
}

