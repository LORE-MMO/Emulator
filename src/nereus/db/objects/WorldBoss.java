//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import jdbchelper.ResultSetMapper;

public class WorldBoss {
    public int monsterId;
    public int mapId;
    public int roomNumber;
    public int kills;
    public int deaths;
    public long spawnInterval = 0L;
    public long timeLimit = 0L;
    public String description;
    public String image;
    public boolean isActive = false;
    public boolean isNotified = false;
    public static final ResultSetMapper<Integer, WorldBoss> resultSetMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, WorldBoss> mapRow(ResultSet rs) throws SQLException {
            WorldBoss worldboss = new WorldBoss();
            worldboss.monsterId = rs.getInt("MonsterID");
            worldboss.spawnInterval = rs.getLong("SpawnInterval");
            worldboss.timeLimit = (long)rs.getInt("TimeLimit");
            worldboss.description = rs.getString("Description");
            worldboss.mapId = rs.getInt("MapID");
            worldboss.image = rs.getString("Image");
            worldboss.kills = rs.getInt("Kills");
            worldboss.deaths = rs.getInt("Deaths");
            return new SimpleEntry(worldboss.monsterId, worldboss);
        }
    };

    public WorldBoss() {
    }
}
