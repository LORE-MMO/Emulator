

package nereus.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Set;

import jdbchelper.ResultSetMapper;
import net.sf.json.JSONObject;
import jdbchelper.BeanCreator;


public class Item {
    private String name,description,type,element,file,link,icon,equipment,reqQuests,meta;
    private int id,level,DPS,range,rarity,cost,quantity,stack,enhId,factionId,reqReputation,reqClassId,reqClassPoints,questStringIndex,questStringValue;
    private boolean coins,upgrade,staff,temporary,sell;
    public Set<ItemEffect> passives;
    public Map<Integer, Integer> requirements;

    public static final ResultSetMapper<Integer, Integer> requirementMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new SimpleEntry(rs.getInt("ReqItemID"), rs.getInt("Quantity"));
        }
    };

    public static final BeanCreator<Set<ItemEffect>> beanPassives = new BeanCreator() {
        public Set<ItemEffect> createBean(ResultSet rs) throws SQLException {
            Set<ItemEffect> passives = new HashSet<ItemEffect>();
            ItemEffect ip = new ItemEffect();
            ip.itemid = rs.getInt("ItemID");
            ip.damageincrease = rs.getDouble("DamageIncrease");
            ip.damagetaken = rs.getDouble("DamageIncrease");
            ip.ExtraExp = rs.getDouble("Exp");
            ip.extragold = rs.getDouble("Gold");
            ip.extracoins = rs.getDouble("Coins");
            ip.extrarep = rs.getDouble("Rep");
            ip.haste = rs.getDouble("Haste");
            ip.dodge = rs.getDouble("Dodge");
            ip.hit = rs.getDouble("Hit");
            ip.crit = rs.getDouble("Crit");
            passives.add(ip);
            while (rs.next()) {
                ItemEffect ip2 = new ItemEffect();
                ip2.itemid = rs.getInt("ItemID");
                ip2.damageincrease = rs.getDouble("DamageIncrease");
                ip2.damagetaken = rs.getDouble("DamageIncrease");
                ip2.ExtraExp = rs.getDouble("Exp");
                ip2.extragold = rs.getDouble("Gold");
                ip2.extracoins = rs.getDouble("Coins");
                ip2.extrarep = rs.getDouble("Rep");
                ip2.haste = rs.getDouble("Haste");
                ip2.dodge = rs.getDouble("Dodge");
                ip2.hit = rs.getDouble("Hit");
                ip2.crit = rs.getDouble("Crit");
                passives.add(ip2);
            }
            return passives;
        }
    };

    public static final ResultSetMapper<Integer, Item> resultSetMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, Item> mapRow(ResultSet rs) throws SQLException {
            Item item = new Item();
            item.id = rs.getInt("id");
            item.name = rs.getString("Name");
            item.description = rs.getString("Description");
            item.type = rs.getString("Type");
            item.element = rs.getString("Element");
            item.file = rs.getString("File");
            item.link = rs.getString("Link");
            item.icon = rs.getString("Icon");
            item.equipment = rs.getString("Equipment");
            item.reqQuests = rs.getString("ReqQuests");
            item.meta = rs.getString("Meta");
            item.level = rs.getInt("Level");
            item.DPS = rs.getInt("DPS");
            item.range = rs.getInt("Range");
            item.rarity = rs.getInt("Rarity");
            item.cost = rs.getInt("Cost");
            item.quantity = rs.getInt("Quantity");
            item.stack = rs.getInt("Stack");
            item.enhId = rs.getInt("EnhID");
            item.factionId = rs.getInt("FactionID");
            item.reqReputation = rs.getInt("ReqReputation");
            item.reqClassId = rs.getInt("ReqClassID");
            item.reqClassPoints = rs.getInt("ReqClassPoints");
            item.questStringIndex = rs.getInt("QuestStringIndex");
            item.questStringValue = rs.getInt("QuestStringValue");
            item.coins = rs.getBoolean("Coins");
            item.upgrade = rs.getBoolean("Upgrade");
            item.staff = rs.getBoolean("Staff");
            item.temporary = rs.getBoolean("Temporary");
            item.sell = rs.getBoolean("Sell");
            return new SimpleEntry(item.getId(), item);
        }
    };

    public Item() {
    }

    public static JSONObject getItemJSON(Item itemObj) {
        return getItemJSON(itemObj, (Enhancement)null);
    }

    public static JSONObject getItemJSON(Item itemObj, Enhancement enhancement) {
        if (itemObj == null) {
            throw new NullPointerException("itemObj is null");
        } else {
            JSONObject item = new JSONObject();
            item.put("ItemID", itemObj.getId());
            item.put("bCoins", itemObj.isCoins() ? 1 : 0);
            item.put("bHouse", itemObj.isHouse() ? 1 : 0);
            item.put("bPTR", 0);
            item.put("bStaff", itemObj.isStaff() ? 1 : 0);
            item.put("bTemp", itemObj.isTemporary() ? 1 : 0);
            item.put("bUpg", itemObj.isUpgrade() ? 1 : 0);
            item.put("iCost", itemObj.getCost());
            item.put("iDPS", itemObj.getDPS());
            item.put("iLvl", itemObj.getLevel());
            item.put("iQSindex", itemObj.getQuestStringIndex());
            item.put("iQSvalue", itemObj.getQuestStringValue());
            item.put("iRng", itemObj.getRange());
            item.put("iRty", itemObj.getRarity());
            item.put("iStk", itemObj.getStack());
            item.put("sDesc", itemObj.getDescription());
            item.put("sES", itemObj.getEquipment());
            item.put("sElmt", itemObj.getElement());
            item.put("sFile", itemObj.getFile());
            item.put("sIcon", itemObj.getIcon());
            item.put("sLink", itemObj.getLink());
            item.put("sMeta", itemObj.getMeta());
            item.put("sName", itemObj.getName());
            item.put("sReqQuests", itemObj.getReqQuests());
            item.put("sType", itemObj.getType());
            if (enhancement != null) {
                if (itemObj.getType().equals("Enhancement")) {
                    item.put("PatternID", enhancement.getPatternId());
                    item.put("iDPS", enhancement.getDPS());
                    item.put("iLvl", enhancement.getLevel());
                    item.put("iRty", enhancement.getRarity());
                    item.put("EnhID", 0);
                    item.remove("sFile");
                } else {
                    item.put("EnhID", enhancement.getId());
                    item.put("EnhLvl", enhancement.getLevel());
                    item.put("EnhPatternID", enhancement.getPatternId());
                    item.put("EnhRty", enhancement.getRarity());
                    item.put("iRng", itemObj.getRange());
                    item.put("EnhRng", itemObj.getRange());
                    item.put("InvEnhPatternID", enhancement.getPatternId());
                    item.put("EnhDPS", enhancement.getDPS());
                }
            }

            return item;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getType() {
        return this.type;
    }

    public String getElement() {
        return this.element;
    }

    public String getFile() {
        return this.file;
    }

    public String getLink() {
        return this.link;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getEquipment() {
        return this.equipment;
    }

    public int getLevel() {
        return this.level;
    }

    public int getDPS() {
        return this.DPS;
    }

    public int getRange() {
        return this.range;
    }

    public int getRarity() {
        return this.rarity;
    }

    public int getCost() {
        return this.cost;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public int getStack() {
        return this.stack;
    }

    public int getEnhId() {
        return this.enhId;
    }

    public int getFactionId() {
        return this.factionId;
    }

    public int getReqReputation() {
        return this.reqReputation;
    }

    public int getReqClassId() {
        return this.reqClassId;
    }

    public int getReqClassPoints() {
        return this.reqClassPoints;
    }

    public int getQuestStringIndex() {
        return this.questStringIndex;
    }

    public int getQuestStringValue() {
        return this.questStringValue;
    }

    public boolean isCoins() {
        return this.coins;
    }

    public boolean isSellable() {
        return this.sell;
    }

    public boolean isUpgrade() {
        return this.upgrade;
    }

    public boolean isHouse() {
        return this.getEquipment().equals("ho") || this.getEquipment().equals("hi");
    }

    public boolean isStaff() {
        return this.staff;
    }

    public int getId() {
        return this.id;
    }

    public boolean isTemporary() {
        return this.temporary;
    }

    public String getReqQuests() {
        return this.reqQuests;
    }

    public String getMeta() {
        return this.meta;
    }


}
