
package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Set;

import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

public class Class {
    public int itemid;
    public String category,description,manaRegenerationMethods,statsDescription;

    public Set skills;
    public static final BeanCreator<Set<Integer>> beanSkills = new BeanCreator()
    {
        public Set<Integer> createBean(ResultSet rs)
                throws SQLException
        {
            Set<Integer> set = new HashSet();

            set.add(Integer.valueOf(rs.getInt("id")));
            while (rs.next()) {
                set.add(Integer.valueOf(rs.getInt("id")));
            }
            return set;
        }
    };

    public static final ResultSetMapper<Integer, Class> resultSetMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, Class> mapRow(ResultSet rs) throws SQLException {
            Class cl = new Class();
            cl.itemid = rs.getInt("ItemID");
            cl.category = rs.getString("Category");
            cl.description = rs.getString("Description");
            cl.manaRegenerationMethods = rs.getString("ManaRegenerationMethods");
            cl.statsDescription = rs.getString("StatsDescription");
            return new SimpleEntry(cl.itemid, cl);
        }
    };

    public Class() {
    }
    public int getitemid() {
        return this.itemid;
    }

    public String getCategory(){
        return this.category;
    }
    public String getDescription(){
        return this.description;
    }
    public String getManaRegenerationMethods(){
        return this.manaRegenerationMethods;
    }
    public String getStatsDescription(){
        return this.statsDescription;
    }


}
