
package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import jdbchelper.ResultSetMapper;

public class ItemPassive {
    public int itemid;
    public double damageincrease, damagetaken, extraexp, extragold, extracoins, extrarep, haste, dodge, hit, crit;
    public static final ResultSetMapper<Integer, ItemPassive> resultSetMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, ItemPassive> mapRow(ResultSet rs) throws SQLException {
            ItemPassive ip = new ItemPassive();
            ip.itemid = rs.getInt("ItemID");
            ip.damageincrease = rs.getDouble("DamageTaken");
            ip.damagetaken = rs.getDouble("DamageIncrease");
            ip.extraexp = rs.getDouble("Exp");
            ip.extragold = rs.getDouble("Gold");
            ip.extracoins = rs.getDouble("Coins");
            ip.extrarep = rs.getDouble("Rep");
            ip.haste = rs.getDouble("Haste");
            ip.dodge = rs.getDouble("Dodge");
            ip.hit = rs.getDouble("Hit");
            ip.crit = rs.getDouble("Crit");
            return new SimpleEntry(ip.itemid, ip);
        }
    };

    public ItemPassive() {
    }
    public int getitemid() {
        return this.itemid;
    }
    public double getDamageincrease() {
        return this.damageincrease;
    }
    public double getDamagetaken() {
        return this.damagetaken;
    }
    public double getExtraexp() {
        return this.extraexp;
    }
    public double getExtragold() {
        return this.extragold;
    }
    public double getExtracoins() {
        return this.extracoins;
    }
    public double getExtrarep() {
        return this.extrarep;
    }
    public double getHaste() {
        return this.haste;
    }
    public double getDodge() {
        return this.dodge;
    }
    public double getHit() {
        return this.hit;
    }
    public double getCrit() {
        return this.crit;
    }
}
