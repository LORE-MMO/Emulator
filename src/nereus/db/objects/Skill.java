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

import com.google.common.collect.Multimap;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

public class Skill {
    private String name,animation,description,icon,dsrc,reference,target,effects,type,strl;
    private double damage,hpregen,lifesteal;
    private int id,itemid,mana,range,hitTargets,cooldown,aurarandom;
    public Multimap<Integer, SkillAuras> auraskill;

    public static final ResultSetMapper<Integer, Integer> auraMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new SimpleEntry(rs.getInt("AuraID"), rs.getInt("SkillID"));
        }
    };
    public static final ResultSetMapper<Integer, Skill> resultSetMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, Skill> mapRow(ResultSet rs) throws SQLException {
            Skill skill = new Skill();
            skill.id = rs.getInt("id");
            skill.name = rs.getString("Name");
            skill.animation = rs.getString("Animation");
            skill.description = rs.getString("Description");
            skill.icon = rs.getString("Icon");
            skill.dsrc = rs.getString("Dsrc");
            skill.reference = rs.getString("Reference");
            skill.target = rs.getString("Target");
            skill.effects = rs.getString("Effects");
            skill.type = rs.getString("Type");
            skill.strl = rs.getString("Strl");
            skill.damage = rs.getDouble("Damage");
            skill.lifesteal = rs.getDouble("LifeSteal");
            skill.mana = rs.getInt("Mana");
            skill.range = rs.getInt("Range");
            skill.hitTargets = rs.getInt("HitTargets");
            skill.cooldown = rs.getInt("Cooldown");
            skill.aurarandom = rs.getInt("AuraRandom");
            skill.hpregen = rs.getInt("HealthRegeneration");
            skill.itemid = rs.getInt("ItemID");
            return new SimpleEntry(skill.id, skill);
        }
    };

    public Skill() {
    }

    public String getName() {
        return this.name;
    }

    public String getAnimation() {
        return this.animation;
    }

    public String getDescription() {
        return this.description;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getDsrc() {
        return this.dsrc;
    }

    public String getReference() {
        return this.reference;
    }

    public String getTarget() {
        return this.target;
    }

    public String getEffects() {
        return this.effects;
    }

    public String getType() {
        return this.type;
    }

    public String getStrl() {
        return this.strl;
    }

    public double getDamage() {
        return this.damage;
    }

    public double getHpregen() {
        return this.hpregen;
    }

    public int getMana() {
        return this.mana;
    }

    public int getRange() {
        return this.range;
    }

    public int getHitTargets() {
        return this.hitTargets;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public int getId() {
        return this.id;
    }

    public int getItemid() {
        return this.itemid;
    }

    public boolean isAuraRandom() {
        return this.aurarandom > 0;
    }
    public double getLifeSteal() {
        return this.lifesteal;
    }
}
