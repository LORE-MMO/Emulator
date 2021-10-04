package nereus.discord.events;

import nereus.discord.Bot;
import nereus.world.World;
import jdbchelper.QueryResult;

public class ReloadSettings {
    public ReloadSettings(World world) {
        if (!Bot.settings.isEmpty()) {
            Bot.settings.clear();
        }
        QueryResult rs = world.db.jdbc.query("SELECT * FROM discord_settings", new Object[0]);
        while (rs.next()) {
            String Key = rs.getString("Key");
            String Value = rs.getString("Value");
            Bot.settings.put(Key, Value);
        }
        rs.close();
    }
}

