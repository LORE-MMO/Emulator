package nereus.db.objects;

import com.google.common.collect.Multimap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

public class Quest {
   private int id;
   private int factionId;
   private int reqReputation;
   private int reqClassId;
   private int reqClassPoints;
   private int experience;
   private int gold;
   private int reputation;
   private int classPoints;
   private int level;
   private int slot;
   private int value;
   private int index;
   private String name;
   private String description;
   private String endText;
   private String rewardType;
   private String field;
   private boolean upgrade;
   private boolean once;
   public Multimap<Integer, QuestReward> rewards;
   public Map<Integer, Integer> reqd;
   public Map<Integer, Integer> requirements;
   public Set<Integer> locations;

   public static final BeanCreator < Set < Integer >> beanLocations = new BeanCreator() {
      public Set < Integer > createBean(ResultSet rs) throws SQLException {
         Set<Integer> set = new HashSet();
         set.add(rs.getInt("MapID"));
         while (rs.next()) {
            set.add(rs.getInt("MapID"));
         }
         return set;
      }
   };

   //
   public static final ResultSetMapper < Integer, Integer > requirementsNormalRewardsMapper = new ResultSetMapper() {
      public AbstractMap.SimpleEntry < Integer, Integer > mapRow(ResultSet rs) throws SQLException {
         return new AbstractMap.SimpleEntry(rs.getInt("ItemID"), rs.getInt("Quantity"));
      }
   };

   public static final ResultSetMapper < Integer, Integer > requirementslocationsRewardsMapper = new ResultSetMapper() {
      public AbstractMap.SimpleEntry < Integer, Integer > mapRow(ResultSet rs) throws SQLException {
         return new AbstractMap.SimpleEntry(rs.getInt("ItemID"), rs.getInt("MapID"));
      }
   };
   //

//   public static final ResultSetMapper< Integer, Integer > requirementsRewardsMapper = new ResultSetMapper() {
//      public AbstractMap.SimpleEntry < Integer, Integer > mapRow(ResultSet rs) throws SQLException {
//            return new AbstractMap.SimpleEntry(rs.getInt("ItemID"), rs.getInt("Quantity"));
//      }
//   };

   public static final ResultSetMapper< Integer, Quest > resultSetMapper = new ResultSetMapper() {
      public AbstractMap.SimpleEntry< Integer, Quest > mapRow(ResultSet rs) throws SQLException {
         Quest quest = new Quest();
         quest.id = rs.getInt("id");
         quest.factionId = rs.getInt("FactionID");
         quest.reqReputation = rs.getInt("ReqReputation");
         quest.reqClassId = rs.getInt("ReqClassID");
         quest.reqClassPoints = rs.getInt("ReqClassPoints");
         quest.experience = rs.getInt("Experience");
         quest.gold = rs.getInt("Gold");
         quest.reputation = rs.getInt("Reputation");
         quest.classPoints = rs.getInt("ClassPoints");
         quest.level = rs.getInt("Level");
         quest.slot = rs.getInt("Slot");
         quest.value = rs.getInt("Value");
         quest.index = rs.getInt("Index");

         quest.name = rs.getString("Name");
         quest.description = rs.getString("Description");
         quest.endText = rs.getString("EndText");
         quest.rewardType = rs.getString("RewardType");
         quest.field = rs.getString("Field");

         quest.once = rs.getBoolean("Once");
         quest.upgrade = rs.getBoolean("Upgrade");

         return new AbstractMap.SimpleEntry(Integer.valueOf(quest.getId()), quest);
      }
   };

   public Quest() {
      super();
   }

   public int getId() {
      return this.id;
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

   public int getExperience() {
      return this.experience;
   }

   public int getGold() {
      return this.gold;
   }

   public int getReputation() {
      return this.reputation;
   }

   public int getClassPoints() {
      return this.classPoints;
   }

   public int getLevel() {
      return this.level;
   }

   public int getSlot() {
      return this.slot;
   }

   public int getValue() {
      return this.value;
   }

   public int getIndex() {
      return this.index;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getEndText() {
      return this.endText;
   }

   public String getRewardType() {
      return this.rewardType;
   }

   public String getField() {
      return this.field;
   }

   public boolean isUpgrade() {
      return this.upgrade;
   }

   public boolean isOnce() {
      return this.once;
   }
}
