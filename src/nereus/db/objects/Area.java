package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

public class Area
{
    protected String name, file, music;
    protected int id, maxPlayers, reqLevel, musicStrength;
    private Boolean upgrade, staff, PvP, firstJoin, WorldBoss;
    public Set<MapMonster> monsters = Collections.EMPTY_SET;
    public Map<Integer, Cell> cells = Collections.EMPTY_MAP;
    public Set<Integer> items = Collections.EMPTY_SET;

    public static final BeanCreator<Set<Integer>> beanItems = new BeanCreator()
    {
        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet();
            set.add(Integer.valueOf(rs.getInt("ItemID")));while (rs.next()) {
                set.add(Integer.valueOf(rs.getInt("ItemID")));
            }
            return set;
        }
    };

    public static final ResultSetMapper<String, Area> resultSetMapper = new ResultSetMapper()
    {
        public AbstractMap.SimpleEntry<String, Area> mapRow(ResultSet rs) throws SQLException {
            Area area = new Area();area.name = rs.getString("Name").toLowerCase();
            area.file = rs.getString("File");
            area.music = rs.getString("Music");
            area.musicStrength = rs.getInt("MusicStrength");
            area.id = rs.getInt("id");
            area.maxPlayers = rs.getInt("MaxPlayers");
            area.reqLevel = rs.getInt("ReqLevel");
            area.upgrade = rs.getBoolean("Upgrade");
            area.staff = rs.getBoolean("Staff");
            area.PvP = rs.getBoolean("PvP");
            area.firstJoin = rs.getBoolean("firstJoin");
            area.WorldBoss = rs.getBoolean("WorldBoss");

            return new AbstractMap.SimpleEntry(area.getName(), area);
        }
    };

    public String getName()
    {
        return this.name;
    }

    public String getFile()
    {
        return this.file;
    }

    public String getMusic()
    {
        return this.music;
    }

    public int getMusicStrength() {
        return this.musicStrength;
    }

    public int getMaxPlayers()
    {
        return this.maxPlayers;
    }

    public int getReqLevel()
    {
        return this.reqLevel;
    }

    public boolean isUpgrade()
    {
        return this.upgrade;
    }

    public boolean isStaff()
    {
        return this.staff;
    }

    public int getId()
    {
        return this.id;
    }

    public boolean isPvP()
    {
        return this.PvP;
    }

    public boolean isFirstJoin() {
        return firstJoin;
    }
    public boolean isWorldBoss() {
        return this.WorldBoss;
    }
}
