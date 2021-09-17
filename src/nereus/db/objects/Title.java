package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import jdbchelper.ResultSetMapper;

public class Title {

    private int id;
    private String name, color, description;

    public static final ResultSetMapper<Integer, Title> resultSetMapper = new ResultSetMapper() {
        public AbstractMap.SimpleEntry<String, Title> mapRow(ResultSet rs) throws SQLException {
            Title title = new Title();

            title.id = rs.getInt("id");
            title.name = rs.getString("Name");
            title.description = rs.getString("Description");
            title.color = rs.getString("Color");

            return new AbstractMap.SimpleEntry(title.id, title);
        }
    };

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getColor() {
        return this.color;
    }
}