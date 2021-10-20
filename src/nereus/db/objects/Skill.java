package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map;

import jdbchelper.ResultSetMapper;

public class Skill {
    private String name;
    private String animation;
    private String description;
    private String icon;
    private String dsrc;
    private String reference;
    private String target;
    private String effects;
    private String type;
    private String strl;
    private double damage,hpregen;
    private int id;
    private int mana;
    private int range;
    private int hitTargets;
    private int cooldown, auraId;
    public Map<Integer, Integer> auras;

    public static final ResultSetMapper<Integer, Integer> auraMapper = new ResultSetMapper() {
        public AbstractMap.SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry(rs.getInt("AuraID"), rs.getInt("SkillID"));
        }
    };

    public static final ResultSetMapper<Integer, Skill> resultSetMapper = new ResultSetMapper() {
        public AbstractMap.SimpleEntry<Integer, Skill> mapRow(ResultSet rs)
                throws SQLException {
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

            skill.mana = rs.getInt("Mana");
            skill.range = rs.getInt("Range");
            skill.hitTargets = rs.getInt("HitTargets");
            skill.cooldown = rs.getInt("Cooldown");
            skill.auraId = rs.getInt("AuraID");

            skill.hpregen = rs.getInt("HealthRegeneration");


            return new AbstractMap.SimpleEntry(Integer.valueOf(skill.id), skill);
        }
    };

    public boolean hasAuraId() {
        return auraId > 0;
    }

    public int getAuraId() {
        return auraId;
    }

    public boolean hasAura() {
        return !this.auras.isEmpty();
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

    public Map getAura() {
        return this.auras;
    }

    public int getId() {
        return this.id;
    }
}