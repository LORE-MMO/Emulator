package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;

import jdbchelper.ResultSetMapper;

public class AuraEffects {

    private int id;
    private String stat;
    private String type;
    private double value;
    private int stack;
    public static final ResultSetMapper<Integer, AuraEffects> resultSetMapper = new ResultSetMapper() {
        public AbstractMap.SimpleEntry<Integer, AuraEffects> mapRow(ResultSet rs)
                throws SQLException {
            AuraEffects ae = new AuraEffects();

            ae.id = rs.getInt("id");
            ae.stat = rs.getString("Stat");
            ae.type = rs.getString("Type");
            ae.value = rs.getDouble("Value");

            return new AbstractMap.SimpleEntry(Integer.valueOf(ae.id), ae);
        }
    };

    public int getId() {
        return this.id;
    }

    public String getStat() {
        return this.stat;
    }

    public String getType() {
        return this.type;
    }

    public double getValue() {
        return this.value;
    }

    public void setStack(int stack){
        this.stack = stack;
    }

    public int getStack(){
        return this.stack;
    }
}

/* Location:           F:\HP\copypasta.jar

 * Qualified Name:     copypasta.db.objects.AuraEffects

 * JD-Core Version:    0.7.0.1

 */
