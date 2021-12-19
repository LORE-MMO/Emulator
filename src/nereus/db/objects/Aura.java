//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Aura {
    public int id;
    public int duration;
    public String name;
    public String category;
    public double damageIncrease;
    public double damageTakenDecrease;
    public Set<Integer> effects;
    public boolean stackable;
    public int maxstack;
    public int eatAuraId;
    //private int comboWith;
    //private int comboTo;
    public int SkillID;
    public double applychance;
    public int selfCastt;
    public int dotinterval;


    public static final BeanCreator<Set<Integer>> beanEffects = new BeanCreator() {
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet();
            set.add(rs.getInt("id"));

            while(rs.next()) {
                set.add(rs.getInt("id"));
            }

            return set;
        }
    };
    public static final ResultSetMapper<Integer, Aura> resultSetMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, Aura> mapRow(ResultSet rs) throws SQLException {
            Aura aura = new Aura();
            aura.id = rs.getInt("id");
            aura.duration = rs.getInt("Duration");
            aura.name = rs.getString("Name");
            aura.category = rs.getString("Category");
            aura.damageIncrease = rs.getDouble("DamageIncrease");
            aura.damageTakenDecrease = rs.getDouble("DamageTakenDecrease");
            aura.stackable = rs.getBoolean("Stackable");
            aura.maxstack = rs.getInt("MaxStack");
            aura.eatAuraId = rs.getInt("EatAura");
            aura.selfCastt = rs.getInt("SelfCast");
            aura.applychance = rs.getDouble("ApplyChance");
            aura.dotinterval = rs.getInt("DoTInterval");
            return new SimpleEntry(aura.id, aura);
        }
    };

    public Aura() {
    }

    public JSONArray getAuraArray(boolean isNew) {
        JSONArray auras = new JSONArray();
        JSONObject auraInfo = new JSONObject();
        if (!this.getCategory().isEmpty() && !this.getCategory().equals("d")) {
            auraInfo.put("cat", this.getCategory());
            if (this.getCategory().equals("stun")) {
                auraInfo.put("s", "s");
            }
        }

        auraInfo.put("nam", this.getName());
        auraInfo.put("t", "s");
        auraInfo.put("dur", String.valueOf(this.getDuration()));
        auraInfo.put("isNew", isNew);
        auras.add(auraInfo);
        return auras;
    }

    public boolean isskillcast() {
        return this.selfCastt > 0;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public double getDamageIncrease() {
        return this.damageIncrease;
    }

    public double getDamageTakenDecrease() {
        return this.damageTakenDecrease;
    }

    public int getDuration() {
        return this.duration;
    }

    public boolean canStack() {
        return this.stackable;
    }

    public int getMaxStack() { return this.maxstack; }

    public boolean eatsAura() {
        return this.eatAuraId > 0;
    }

    public int getAuraToEat() {
        return this.eatAuraId;
    }

    public double getApplychance(){ return this.applychance;}

    public int getDotinterval() {
        return this.dotinterval;
    }
}
