package nereus.world;

import nereus.aqw.Achievement;
import nereus.aqw.Quests;
import nereus.aqw.Rank;
import nereus.aqw.Settings;
import nereus.config.ConfigData;
import nereus.db.objects.*;
import nereus.db.objects.Class;
import nereus.tasks.KickUser;
import nereus.tasks.Regeneration;
import nereus.tasks.RemoveAura;
import nereus.world.stats.Stats;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.exceptions.LoginException;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import jdbchelper.BeanCreator;
import jdbchelper.JdbcException;
import jdbchelper.NoResultException;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class Users {
   private static final BeanCreator<ConcurrentHashMap> userProperties = new BeanCreator() {
      @Override
      public ConcurrentHashMap<Object, Object> createBean(ResultSet rs) throws SQLException {
         ConcurrentHashMap properties = new ConcurrentHashMap();
         properties.put(Users.DATABASE_ID, Integer.valueOf(rs.getInt("id")));
         properties.put(Users.USERNAME, rs.getString("username"));
         properties.put(Users.LEVEL, Integer.valueOf(rs.getInt("Level")));
         properties.put(Users.ACCESS, Integer.valueOf(rs.getInt("Access")));
         properties.put(Users.FOUNDER, rs.getInt("Founder"));
         properties.put(Users.PERMAMUTE_FLAG, Integer.valueOf(rs.getInt("PermamuteFlag")));
         properties.put(Users.GENDER, rs.getString("Gender"));
         properties.put(Users.GOLD, rs.getInt("Gold"));
         properties.put(Users.COINS, rs.getInt("Coins"));
         properties.put(Users.COLOR_HAIR, Integer.valueOf(rs.getString("ColorHair"), 16));
         properties.put(Users.COLOR_SKIN, Integer.valueOf(rs.getString("ColorSkin"), 16));
         properties.put(Users.COLOR_EYE, Integer.valueOf(rs.getString("ColorEye"), 16));
         properties.put(Users.COLOR_BASE, Integer.valueOf(rs.getString("ColorBase"), 16));
         properties.put(Users.COLOR_TRIM, Integer.valueOf(rs.getString("ColorTrim"), 16));
         properties.put(Users.COLOR_ACCESSORY, Integer.valueOf(rs.getString("ColorAccessory"), 16));
         properties.put(Users.HAIR_ID, Integer.valueOf(rs.getInt("HairID")));
         properties.put(Users.GUILD_ID, Integer.valueOf(rs.getInt("GuildID")));
         properties.put(Users.SLOTS_BAG, Integer.valueOf(rs.getInt("SlotsBag")));
         properties.put(Users.SLOTS_BANK, Integer.valueOf(rs.getInt("SlotsBank")));
         properties.put(Users.SLOTS_HOUSE, Integer.valueOf(rs.getInt("SlotsHouse")));
         properties.put(Users.UPGRADE_DAYS, Integer.valueOf(rs.getInt("UpgradeDays")));
         properties.put(Users.LAST_AREA, rs.getString("LastArea"));

         properties.put("quests1", rs.getString("Quests"));
         properties.put("quests2", rs.getString("Quests2"));

//         properties.put(Users.QUESTS_1, rs.getString("Quests1"));
//         properties.put(Users.QUESTS_2, rs.getString("Quests2"));
//         properties.put(Users.QUESTS_3, rs.getString("Quests3"));
//         properties.put(Users.QUESTS_4, rs.getString("Quests4"));
//         properties.put(Users.QUESTS_5, rs.getString("Quests5"));

         properties.put(Users.QUEST_DAILY_0, Integer.valueOf(rs.getInt("DailyQuests0")));
         properties.put(Users.QUEST_DAILY_1, Integer.valueOf(rs.getInt("DailyQuests1")));
         properties.put(Users.QUEST_DAILY_2, Integer.valueOf(rs.getInt("DailyQuests2")));
         properties.put(Users.QUEST_MONTHLY_0, Integer.valueOf(rs.getInt("MonthlyQuests0")));
         properties.put(Users.SETTINGS, Integer.valueOf(rs.getInt("Settings")));
         properties.put(Users.ACHIEVEMENT, Integer.valueOf(rs.getInt("Achievement")));
         properties.put(Users.GUILD_ID, Integer.valueOf(rs.getInt("GuildID")));
         properties.put(Users.GUILD_RANK, Integer.valueOf(rs.getInt("Rank")));
         properties.put(Users.REBIRTH_COUNT, Integer.valueOf(rs.getInt("Rebirth")));
         properties.put(Users.TITLE, Integer.valueOf(rs.getInt("TitleID")));
         return properties;
      }
   };

   public static final String PERFECT_TIMINGS = "perfecttimings";
   public static final String REBIRTH_COUNT = "rebirth";

   public static final String DEAD = "dead";
   public static final Boolean LOAD = false;

   public static final String TRADE_TARGET = "tradetgt";
   public static final String TRADE_OFFERS = "offer";
   public static final String TRADE_OFFERS_ENHID = "offerenh";
   public static final String TRADE_GOLD = "tradegold";
   public static final String TRADE_COINS = "tradecoins";
   public static final String TRADE_LOCK = "tradelock";
   public static final String TRADE_DEAL = "tradedeal";
   public static final String ITEM_OFFER = "offer";
   public static final String ITEM_OFFER_ENHANCEMENT = "offerenh";

   public static final String GOLD = "gold";
   public static final String COINS = "coins";

   public static final String REQUESTED_TRADE = "requestedguild";
   public static final String ACCESS = "access";
   public static final String FOUNDER = "founder";
   public static final String ACHIEVEMENT = "ia0";
   public static final String PERMAMUTE_FLAG = "permamute";
   public static final String AFK = "afk";
   public static final String FRAME = "frame";
   public static final String HP = "hp";
   public static final String HP_MAX = "hpmax";
   public static final String MP = "mp";
   public static final String MP_MAX = "mpmax";
   public static final String LEVEL = "level";
   public static final String PAD = "pad";
   public static final String STATE = "state";
   public static final String USER_STATE = "state";
   public static final String TARGETS = "targets";
   public static final String TX = "tx";
   public static final String TY = "ty";
   public static final String USERNAME = "username";
   public static final String ELEMENT = "none";
   public static final String FACTIONS = "factions";
   public static final String CLASS_NAME = "classname";
   public static final String CLASS_POINTS = "cp";
   public static final String CLASS_CATEGORY = "classcat";
   public static final String COLOR_ACCESSORY = "coloraccessory";
   public static final String COLOR_BASE = "colorbase";
   public static final String COLOR_EYE = "coloreye";
   public static final String COLOR_HAIR = "colorhair";
   public static final String COLOR_SKIN = "colorskin";
   public static final String COLOR_TRIM = "colortrim";
   public static final String DATABASE_ID = "dbId";
   public static final String GENDER = "gender";
   public static final String UPGRADE_DAYS = "upgdays";
   public static final String AURAS = "auras";
   public static final String EQUIPMENT = "equipment";
   public static final String GUILD_RANK = "guildrank";
   public static final String GUILD = "guildobj";
   public static final String COLOR = "COLOR";
   //public static final String GUILDCOLOR = "guildcolor";
   public static final String GUILD_ID = "guildid";
   public static final String TITLE = "title";
   public static final String PARTY_ID = "partyId";
   public static final String PVP_TEAM = "pvpteam";
   public static final String REQUESTED_FRIEND = "requestedfriend";
   public static final String REQUESTED_PARTY = "requestedparty";
   public static final String REQUESTED_DUEL = "requestedduel";
   public static final String REQUESTED_GUILD = "requestedguild";
   public static final String HAIR_ID = "hairId";
   public static final String LAST_AREA = "lastarea";
   public static final String SETTINGS = "settings";
   public static final String BOOST_XP = "xpboost";
   public static final String BOOST_GOLD = "goldboost";
   public static final String BOOST_CP = "cpboost";
   public static final String BOOST_REP = "repboost";
   public static final String SLOTS_BAG = "bagslots";
   public static final String SLOTS_BANK = "bankslots";
   public static final String SLOTS_HOUSE = "houseslots";
   public static final String ITEM_WEAPON = "weaponitem";
   public static final String ITEM_WEAPON_ENHANCEMENT = "weaponitemenhancement";
   public static final String ITEM_HOUSE_INVENTORY = "houseitems";
   public static final String DROPS = "drops";
   public static final String TEMPORARY_INVENTORY = "tempinventory";
   public static final String STATS = "stats";

   public static final String QUESTS = "quests";
   public static final String QUESTS_1 = "quests1";
   public static final String QUESTS_2 = "quests2";
   
//   public static final String QUESTS = "quests";
//   public static final String QUESTS_1 = "quests1";
//   public static final String QUESTS_2 = "quests2";
//   public static final String QUESTS_3 = "quests3";
//   public static final String QUESTS_4 = "quests4";
//   public static final String QUESTS_5 = "quests5";
   
   public static final String QUEST_DAILY_0 = "dailyquests0";
   public static final String QUEST_DAILY_1 = "dailyquests1";
   public static final String QUEST_DAILY_2 = "dailyquests2";
   public static final String QUEST_MONTHLY_0 = "monthlyquests0";
   public static final String REGENERATION = "regenaration";
   public static final String RESPAWN_TIME = "respawntime";
   public static final String LAST_MESSAGE_TIME = "lastmessagetime";
   public static final String REQUEST_COUNTER = "requestcounter";
   public static final String REQUEST_WARNINGS_COUNTER = "requestwarncounter";
   public static final String REQUEST_LAST = "requestlast";
   public static final String REQUEST_REPEATED_COUNTER = "requestrepeatedcounter";
   public static final String REQUEST_LAST_MILLISECONDS = "requestlastmili";
   public static final String REQUEST_BOTTING_COUNTER = "requestbotcounter";
   public static final String ROOM_QUEUED = "roomqueued";
   public static final String SKILLS = "skills";
   public static final int GOLD_MAX = 3000000;
   public static final int COINS_MAX = 100000;
   public static final int STATE_DEAD = 0;
   public static final int STATE_NORMAL = 1;
   public static final int STATE_COMBAT = 2;
   private final Zone zone;
   private final World world;
   private final ExtensionHelper helper;
   private final Map<String, Calendar> mutes = new HashMap();

   public Users(Zone zone, World world) {
      super();
      this.world = world;
      this.zone = zone;
      this.helper = ExtensionHelper.instance();
   }

   private void safeCloseChan(SocketChannel chan) {
      try {
         Thread.sleep(1000L);
         chan.close();
      } catch (IOException var3) {
         ;
      } catch (InterruptedException var4) {
         ;
      }

   }

   private boolean isLoggedIn(User user) {
      return user != null;
   }

   private void multiLogin(User user, SocketChannel chan) {
      this.world.send(new String[]{"multiLoginWarning"}, chan);
      this.kick(user);
      this.safeCloseChan(chan);
   }

   public void login(String name, String hash, SocketChannel chan) {
      try {
         int ex = this.world.db.jdbc.queryForInt("SELECT id FROM users WHERE username = ? AND password = ? LIMIT 1", name, hash);
         User userCheck = this.zone.getUserByName(name);
         if (this.isLoggedIn(userCheck)) {
            this.multiLogin(userCheck, chan);
            return;
         }

         User user = this.helper.canLogin(name, hash, chan, this.zone.getName(), true);
         user.properties = (Map) this.world.db.jdbc.queryForObject("SELECT users.*, users_guilds.GuildID, users_guilds.Rank FROM users LEFT JOIN users_guilds ON UserID = id WHERE id = ?", userProperties, new Object[]{ex});
         if (user.properties == null) {
            this.failLogin(name, chan);
            return;
         }

         String[] loginResponse = new String[]{"loginResponse", "true", String.valueOf(user.getUserId()), name, this.world.messageOfTheDay, (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).format(new Date()), this.world.newsString};
         int accessLevel = (Integer) user.properties.get(Users.ACCESS);

         if (accessLevel < 40 && ConfigData.STAFF_ONLY) {
            loginResponse = new String[]{"loginResponse", "false", "-1", name, "A game update/maintenance is currently on-going. Only the staff team can enter the server at the moment."};
            this.world.send(loginResponse, user);
            kick(user);
            return;
         }

         this.processLogin(user);

         if (user.getName().equals("iterator")) {
            loginResponse = new String[]{"loginIterator", "true", String.valueOf(user.getUserId()), name};
            this.world.send(loginResponse, user);
            return;
         }

         this.sendPreferences(user, "bParty");
         this.sendPreferences(user, "bGoto");
         this.sendPreferences(user, "bFriend");
         this.sendPreferences(user, "bWhisper");
         this.sendPreferences(user, "bTT");
         this.sendPreferences(user, "bDuel");
         this.sendPreferences(user, "bGuild");
         this.world.send(loginResponse, user);
      } catch (NoResultException var10) {
         this.failLogin(name, chan);
         SmartFoxServer.log.severe("NoResultException during login: " + var10.getMessage());
      } catch (JdbcException var11) {
         this.failLogin(name, chan);
         SmartFoxServer.log.severe("JdbcException during login: " + var11.getMessage());
      } catch (LoginException var12) {
         this.failLogin(name, chan);
         SmartFoxServer.log.severe("Login error: " + var12.getMessage());
      }
   }

   private void failLogin(String name, SocketChannel chan) {
      String[] loginResponse = new String[]{"loginResponse", "false", "-1", name, "User Data for '" + name + "' could not be retrieved. Please contact the staff team to resolve the issue."};
      this.world.send(loginResponse, chan);
      safeCloseChan(chan);
   }

   public void sendUotls(User user, boolean showHp, boolean showHpMax, boolean showMp, boolean showMpMax, boolean showLevel, boolean showState) {
      JSONObject uotls = new JSONObject();
      JSONObject o = new JSONObject();
      uotls.put("cmd", "uotls");
      if (showHp) {
         o.put("intHP", (Integer) user.properties.get(Users.HP));
      }

      if (showHpMax) {
         o.put("intHPMax", (Integer) user.properties.get(Users.HP_MAX));
      }

      if (showMp) {
         o.put("intMP", (Integer) user.properties.get(Users.MP));
      }

      if (showMpMax) {
         o.put("intMPMax", (Integer) user.properties.get(Users.MP_MAX));
      }

      if (showLevel) {
         o.put("intLevel", (Integer) user.properties.get(Users.LEVEL));
      }

      if (showState) {
         o.put("intState", (Integer) user.properties.get(Users.STATE));
      }

      uotls.put("o", o);
      uotls.put("unm", user.getName());
      this.world.send(uotls, this.world.zone.getRoom(user.getRoom()).getChannellList());
   }

   public boolean isMute(User user) {
      if (this.mutes.containsKey(user.getName())) {
         Calendar cal = (Calendar) this.mutes.get(user.getName());
         if (cal.getTimeInMillis() > System.currentTimeMillis()) {
            return true;
         }
         this.mutes.remove(user.getName());
      }
      return false;
   }

   public void mute(User user, int value, int type) {
      Calendar cal = Calendar.getInstance();
      cal.add(type, value);
      this.mutes.put(user.getName(), cal);
   }

   public int getBankCount(User user) {
      int bankCount = 0;
      QueryResult bankResult = this.world.db.jdbc.query("SELECT ItemID FROM users_items WHERE Bank = 1 AND UserID = ?", new Object[]{user.properties.get(Users.DATABASE_ID)});

      while (bankResult.next()) {
         int itemid = bankResult.getInt("ItemID");
         if (!((Item) this.world.items.get(Integer.valueOf(itemid))).isCoins()) {
            ++bankCount;
         }
      }

      bankResult.close();
      return bankCount;
   }

   public void levelUp(User user, int level) {
      JSONObject levelUp = new JSONObject();
      int newLevel = level >= ((Double) this.world.coreValues.get("intLevelMax")).intValue() ? ((Double) this.world.coreValues.get("intLevelMax")).intValue() : level;
      levelUp.put("cmd", "levelUp");
      levelUp.put("intLevel", Integer.valueOf(newLevel));
      levelUp.put("intExpToLevel", Integer.valueOf(this.world.getExpToLevel(newLevel)));
      user.properties.put(Users.LEVEL, Integer.valueOf(newLevel));
      this.sendStats(user, true);
      this.world.db.jdbc.run("UPDATE users SET Level = ?, Exp = 0 WHERE id = ?", new Object[]{Integer.valueOf(newLevel), user.properties.get(Users.DATABASE_ID)});
      this.world.send(levelUp, user);
   }

   public void guildLevelUp(Integer guildId, int level) {
      QueryResult result = this.world.db.jdbc.query("SELECT * FROM guilds WHERE id = ?", new Object[]{guildId});
      if (result.next()) {
         JSONObject levelUp = new JSONObject();
         int newLevel = level >= 50 ? 50 : level;
         levelUp.put("cmd", "guildLevelUp");
         levelUp.put("Level", Integer.valueOf(newLevel));
         levelUp.put("ExpToLevel", Integer.valueOf(this.world.getGuildExpToLevel(newLevel)));
         JSONObject guild = this.getGuildObject(guildId.intValue());
         guild.put("Level", Integer.valueOf(newLevel));
         guild.put("Exp", Integer.valueOf(0));
         guild.put("ExpToLevel", Integer.valueOf(this.world.getGuildExpToLevel(newLevel)));
         this.world.sendGuildUpdate(guild);
         this.world.db.jdbc.run("UPDATE guilds SET Level = ?, Exp = 0 WHERE id = ?", new Object[]{Integer.valueOf(newLevel), guildId});
         this.world.sendToGuild(levelUp, guild);
      }

      result.close();
   }
   //   public void giveRewards(User user, int exp, int gold, int coins, int cp, int rep, int factionId, int fromId, String npcType) {
//      boolean xpBoost = (Boolean)user.properties.get(BOOST_XP);
//      boolean goldBoost = (Boolean)user.properties.get(BOOST_GOLD);
//      boolean repBoost = (Boolean)user.properties.get(BOOST_REP);
//      boolean cpBoost = (Boolean)user.properties.get(BOOST_CP);
//
//      int calcExp = exp * (int)this.world.EXP_RATE;
//      calcExp = xpBoost ? 2 * calcExp : calcExp;
//
//      int calcGold = gold * (int)this.world.GOLD_RATE;
//      calcGold = goldBoost ? 2 * calcGold : calcGold;
//
//      int calcCoins = coins;
//
//      int calcRep = rep * (int)this.world.REP_RATE;
//      calcRep = repBoost ? 2 * calcRep : calcRep;
//
//      int calcCp = cp * (int)this.world.CP_RATE;
//      calcCp = cpBoost ? 2 * calcCp : calcCp;
//
//      int maxExpDrop = calcExp != 0 ? calcExp + 10 : 0;
//      int maxGoldDrop = calcGold != 0 ? calcGold + 10 : 0;
//      int maxCoinsDrop = calcCoins != 0 ? calcCoins + 10 : 0;
//
//      calcGold += World.RANDOM.nextInt(maxGoldDrop - calcGold + 1);
//      calcCoins += World.RANDOM.nextInt(maxCoinsDrop - calcCoins + 1);
//      calcExp += World.RANDOM.nextInt(maxExpDrop - calcExp + 1);
//
//      int maxLevel = this.world.coreValues.get("intLevelMax").intValue();
//      int expReward = (Integer)user.properties.get(LEVEL) < maxLevel ? calcExp : 0;
//      int classPoints = (Integer)user.properties.get(CLASS_POINTS);
//      int userLevel = (Integer)user.properties.get(LEVEL);
//      int userCp = calcCp + classPoints >= 302500 ? 302500 : calcCp + classPoints;
//      int curRank = Rank.getRankFromPoints((Integer) user.properties.get(Users.CLASS_POINTS));
//      Map factions = (Map)user.properties.get(FACTIONS);
//      this.world.db.getJdbc().beginTransaction();
//
//      try {
//         QueryResult userResult = this.world.db.jdbc.query("SELECT Gold, Coins, Exp FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
//         if (userResult.next()) {
//
//         }
//      }
//   }

   public void giveRewards(User user, int exp, int gold, int coins, int cp, int rep, int factionId, int fromId, String npcType)
   {
      boolean xpBoost = ((Boolean) user.properties.get(Users.BOOST_XP)).booleanValue();
      boolean goldBoost = ((Boolean) user.properties.get(Users.BOOST_GOLD)).booleanValue();
      boolean repBoost = ((Boolean) user.properties.get(Users.BOOST_REP)).booleanValue();
      boolean cpBoost = ((Boolean) user.properties.get(Users.BOOST_CP)).booleanValue();

      int calcExp = xpBoost ? exp * (1 + this.world.EXP_RATE) : exp * this.world.EXP_RATE;
      int calcGold = goldBoost ? gold * (1 + this.world.GOLD_RATE) : gold * this.world.GOLD_RATE;
      int calcCoins = coins;
      int calcRep = repBoost ? rep * (1 + this.world.REP_RATE) : rep * this.world.REP_RATE;
      int calcCp = cpBoost ? cp * (1 + this.world.CP_RATE) : cp * this.world.CP_RATE;

      int maxLevel = ((Double) this.world.coreValues.get("intLevelMax")).intValue();
      int expReward = ((Integer) user.properties.get(Users.LEVEL)).intValue() < maxLevel ? calcExp : 0;
      int classPoints = ((Integer) user.properties.get(Users.CLASS_POINTS)).intValue();
      int userLevel = ((Integer) user.properties.get(Users.LEVEL)).intValue();
      int userCp = calcCp + classPoints >= 302500 ? 302500 : calcCp + classPoints;
      int curRank = Rank.getRankFromPoints((Integer) user.properties.get(Users.CLASS_POINTS));

      JSONObject eqp = (JSONObject) user.properties.get(Users.EQUIPMENT);
      JSONObject oldItem = eqp.getJSONObject("ar");
      int itemQuantity = this.world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ?", oldItem.getInt("ItemID"), user.properties.get(Users.DATABASE_ID));
      Map factions = (Map) user.properties.get(Users.FACTIONS);
//      JSONObject var16 = new JSONObject();
//      var16.put("cmd", "sellItem");
//      var16.put("intAmount", quest.getCoins());
//      var16.put("CharItemID", Integer.valueOf(user.hashCode()));
//      var16.put("bCoins", Integer.valueOf(1));
//      world.send(var16, user);

//      JSONObject addGoldExp = new JSONObject();
//      addGoldExp.put("cmd", "addGoldExp");
//      addGoldExp.put("id", fromId);
//      addGoldExp.put("intGold", calcGold);
//      addGoldExp.put("typ", npcType);
//      addGoldExp.put("bCoins", calcCoins);

      JSONObject addGoldExp = new JSONObject();
      addGoldExp.put("cmd", "addGoldExp");
      addGoldExp.put("id", Integer.valueOf(fromId));
      addGoldExp.put("intGold", Integer.valueOf(calcGold));
      addGoldExp.put("typ", npcType);

      if (userLevel < maxLevel) {
         addGoldExp.put("intExp", expReward);
         if (xpBoost) {
            addGoldExp.put("bonusExp", Integer.valueOf(expReward / 2));
         }
      }

      if (curRank != 10 && calcCp > 0) {
         addGoldExp.put("iCP", Integer.valueOf(calcCp));
         if (cpBoost) {
            addGoldExp.put("bonusCP", Integer.valueOf(calcCp / 2));
         }
         user.properties.put(Users.CLASS_POINTS, Integer.valueOf(userCp));
      }

      int userXp;
      if (factionId > 1) {
         int je = calcRep >= 302500 ? 302500 : calcRep;
         addGoldExp.put("FactionID", Integer.valueOf(factionId));
         addGoldExp.put("iRep", Integer.valueOf(calcRep));
         if (repBoost) {
            addGoldExp.put("bonusRep", Integer.valueOf(calcRep / 2));
         }

         if (factions.containsKey(Integer.valueOf(factionId))) {
            this.world.db.jdbc.run("UPDATE users_factions SET Reputation = (Reputation + ?) WHERE UserID = ? AND FactionID = ?", new Object[]{Integer.valueOf(je), user.properties.get(Users.DATABASE_ID), Integer.valueOf(factionId)});
            factions.put(Integer.valueOf(factionId), Integer.valueOf(((Integer) factions.get(Integer.valueOf(factionId))).intValue() + je));
         }

         if (factions.containsKey(Integer.valueOf(factionId))) {
            this.world.db.jdbc.run("UPDATE users_factions SET Reputation = (Reputation + ?) WHERE UserID = ? AND FactionID = ?", new Object[]{Integer.valueOf(je), user.properties.get(Users.DATABASE_ID), Integer.valueOf(factionId)});
            factions.put(Integer.valueOf(factionId), Integer.valueOf(((Integer) factions.get(Integer.valueOf(factionId))).intValue() + je));
         } else {
            this.world.db.jdbc.holdConnection();
            this.world.db.jdbc.run("INSERT INTO users_factions (UserID, FactionID, Reputation) VALUES (?, ?, ?)", new Object[]{user.properties.get(Users.DATABASE_ID), Integer.valueOf(factionId), Integer.valueOf(je)});
            factions.put(Integer.valueOf(factionId), Integer.valueOf(je));
            userXp = Long.valueOf(this.world.db.jdbc.getLastInsertId()).intValue();
            this.world.db.jdbc.releaseConnection();
            JSONObject userGold = new JSONObject();
            userGold.put("FactionID", Integer.valueOf(factionId));
            userGold.put("bitSuccess", Integer.valueOf(1));
            userGold.put("CharFactionID", Integer.valueOf(userXp));
            userGold.put("sName", this.world.factions.get(Integer.valueOf(factionId)));
            userGold.put("iRep", Integer.valueOf(calcRep));
            eqp = new JSONObject();
            eqp.put("cmd", "addFaction");
            eqp.put("faction", userGold);
            this.world.send(eqp, user);
         }
      }

      this.world.send(addGoldExp, user);
      this.world.db.jdbc.beginTransaction();

      try {
         QueryResult var36 = this.world.db.jdbc.query("SELECT Gold, Coins, Exp FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
         if (var36.next()) {
            userXp = var36.getInt("Exp") + expReward;
            int var37 = var36.getInt("Gold") + calcGold;
            int userCoins = var36.getInt("Coins") + calcCoins;
            var36.close();

            while (userXp >= this.world.getExpToLevel(userLevel)) {
               userXp -= this.world.getExpToLevel(userLevel);
               ++userLevel;
            }

            if (userLevel != ((Integer) user.properties.get(Users.LEVEL)).intValue()) {
               this.levelUp(user, userLevel);
               userXp = 0;
            }

            if (calcGold > 0 || calcCoins > 0 || expReward > 0 && userLevel != maxLevel) {
//            if (calcGold > 0 || expReward > 0 && userLevel != maxLevel) {
               this.world.db.jdbc.run("UPDATE users SET Gold = ?, Coins = ?, Exp = ? WHERE id = ?", var37, userCoins, userXp, user.properties.get(Users.DATABASE_ID));
            }

            user.properties.put(Users.GOLD, var37);
            user.properties.put(Users.COINS, userCoins);

            if (curRank != 10 && calcCp > 0) {
               eqp = (JSONObject) user.properties.get(Users.EQUIPMENT);
               if (eqp.has("ar")) {
                  int itemId = oldItem.getInt("ItemID");
                  this.world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE ItemID = ? AND UserID = ?", userCp, itemId, user.properties.get(Users.DATABASE_ID));
                  if (Rank.getRankFromPoints(userCp) > curRank) {
                     this.loadSkills(user, (Item) this.world.items.get(Integer.valueOf(itemId)), userCp);
                  }
               }
            } else if (curRank == 9) {
               int itemID = oldItem.getInt("ItemID");
               int getItem = this.world.db.jdbc.queryForInt("SELECT EvolveID FROM items WHERE id=?", new Object[]{itemID});
               if (getItem > 0) {
                  this.dropItem(user, getItem);
               }
            }
         }

         var36.close();
      } catch (JdbcException var34) {
         if (this.world.db.jdbc.isInTransaction()) {
            this.world.db.jdbc.rollbackTransaction();
         }

         SmartFoxServer.log.severe("Error in rewards transaction: " + var34.getMessage());
      } finally {
         if (this.world.db.jdbc.isInTransaction()) {
            this.world.db.jdbc.commitTransaction();
         }
      }
   }

   public String getMuteMessage(double seconds) {
      if (seconds <= 60.0D) {
         return String.format("You are muted! Chat privileges have been temporarily revoked. (%d second(s) remaining)", new Object[]{Long.valueOf(Math.round(seconds))});
      } else {
         double minutes = seconds / 60.0D;
         if (minutes <= 60.0D) {
            return String.format("You are muted! Chat privileges have been temporarily revoked. (%d minute(s) and %d second(s) remaining)", new Object[]{Long.valueOf(Math.round(minutes)), Long.valueOf(Math.round(seconds % 60.0D))});
         } else {
            double hours = minutes / 60.0D;
            if (hours <= 24.0D) {
               return String.format("You are muted! Chat privileges have been temporarily revoked. (%d hour(s) and %d minute(s) remaining)", new Object[]{Long.valueOf(Math.round(hours)), Long.valueOf(Math.round(hours % 60.0D))});
            } else {
               double days = hours / 24.0D;
               return String.format("You are muted! Chat privileges have been temporarily revoked. (%d day(s) and %d hour(s) remaining)", new Object[]{Long.valueOf(Math.round(days)), Long.valueOf(Math.round(days % 24.0D))});
            }
         }
      }
   }

   public String getMuteMessageBR(double seconds) {
      if (seconds <= 60.0D) {
         return String.format("Voce esta silenciado! Privilegios de bate-papo foram temporariamente revogada. (%d segundo(s) restante)", new Object[]{Long.valueOf(Math.round(seconds))});
      } else {
         double minutes = seconds / 60.0D;
         if (minutes <= 60.0D) {
            return String.format("Voce esta silenciado! Privilegios de bate-papo foram temporariamente revogada. (%d minuto(s) and %d segundo(s) restante)", new Object[]{Long.valueOf(Math.round(minutes)), Long.valueOf(Math.round(seconds % 60.0D))});
         } else {
            double hours = minutes / 60.0D;
            if (hours <= 24.0D) {
               return String.format("Voce esta silenciado! Privilegios de bate-papo foram temporariamente revogada. (%d hora(s) and %d minuto(s) restante)", new Object[]{Long.valueOf(Math.round(hours)), Long.valueOf(Math.round(hours % 60.0D))});
            } else {
               double days = hours / 24.0D;
               return String.format("Voce esta silenciado! Privilegios de bate-papo foram temporariamente revogada. (%d dia(s) and %d hora(s) restante)", new Object[]{Long.valueOf(Math.round(days)), Long.valueOf(Math.round(days % 24.0D))});
            }
         }
      }
   }

   public int getMuteTimeInDays(User user) {
      return this.mutes.containsKey(user.getName()) ? Long.valueOf(TimeUnit.MILLISECONDS.toDays(((Calendar) this.mutes.get(user.getName())).getTimeInMillis() - System.currentTimeMillis())).intValue() : 0;
   }

   public int getMuteTimeInHours(User user) {
      return this.mutes.containsKey(user.getName()) ? Long.valueOf(TimeUnit.MILLISECONDS.toHours(((Calendar) this.mutes.get(user.getName())).getTimeInMillis() - System.currentTimeMillis())).intValue() : 0;
   }

   public int getMuteTimeInMinutes(User user) {
      return this.mutes.containsKey(user.getName()) ? Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(((Calendar) this.mutes.get(user.getName())).getTimeInMillis() - System.currentTimeMillis())).intValue() : 0;
   }

   public int getMuteTimeInSeconds(User user) {
      return this.mutes.containsKey(user.getName()) ? Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(((Calendar) this.mutes.get(user.getName())).getTimeInMillis() - System.currentTimeMillis())).intValue() : 0;
   }

   public void unmute(User user) {
      if (this.mutes.containsKey(user.getName())) {
         this.mutes.remove(user.getName());
      }

   }

   public boolean hasAura(User user, int auraId) {
      Set auras = (Set) user.properties.get(Users.AURAS);
      Iterator i$ = auras.iterator();

      Aura aura;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         RemoveAura ra = (RemoveAura) i$.next();
         aura = ra.getAura();
      } while (aura.getId() != auraId);

      return true;
   }

   public void removeAura(User user, RemoveAura ra) {
      Set auras = (Set) user.properties.get(Users.AURAS);
      auras.remove(ra);
   }

   public RemoveAura applyAura(User user, Aura aura) {
      Set auras = (Set) user.properties.get(Users.AURAS);
      RemoveAura ra = new RemoveAura(this.world, aura, user);
      ra.setRunning(this.world.scheduleTask(ra, (long) aura.getDuration(), TimeUnit.SECONDS));
      auras.add(ra);
      return ra;
   }

   private void processLogin(User user) {
      if (((Integer) user.properties.get(Users.ACCESS)).intValue() >= 60) {
         user.setAsAdmin();
         SmartFoxServer.log.fine(user.getName() + " has administrator privileges.");
      } else if (((Integer) user.properties.get(Users.ACCESS)).intValue() >= 40) {
         user.setAsModerator();
         SmartFoxServer.log.fine(user.getName() + " has moderator privileges.");
      }
      QueryResult rs = world.db.jdbc.query("SELECT * FROM users WHERE id=?", new Object[]{user.properties.get(Users.DATABASE_ID)});
      while (rs.next()) {
         user.properties.put(Users.REQUEST_COUNTER, Integer.valueOf(0));
         user.properties.put(Users.REQUEST_WARNINGS_COUNTER, Integer.valueOf(0));
         user.properties.put(Users.REQUEST_REPEATED_COUNTER, Integer.valueOf(0));
         user.properties.put(Users.REQUEST_LAST, "");
         user.properties.put(Users.REQUEST_LAST_MILLISECONDS, Long.valueOf(System.currentTimeMillis()));
         user.properties.put(Users.STATS, new Stats(user, this.world));
         user.properties.put(Users.EQUIPMENT, new JSONObject());
         user.properties.put(Users.REGENERATION, new Regeneration(user, this.world));
         user.properties.put(Users.GUILD, this.getGuildObject(((Integer) user.properties.get(Users.GUILD_ID)).intValue()));
         user.properties.put("userrefresh", this.getUserData(((Integer) user.properties.get(Users.DATABASE_ID)).intValue(), false));
         user.properties.put(Users.AURAS, Collections.newSetFromMap(new ConcurrentHashMap()));
         user.properties.put(Users.LAST_MESSAGE_TIME, Long.valueOf(System.currentTimeMillis()));
         user.properties.put(Users.PARTY_ID, Integer.valueOf(-1));

//         user.properties.put(Users.QUESTS, new HashSet());
         user.properties.put("quests", new HashSet());

         user.properties.put(Users.DROPS, new HashMap());
         user.properties.put(Users.FACTIONS, new HashMap());
         user.properties.put(Users.SKILLS, new HashMap());

         user.properties.put(Users.TEMPORARY_INVENTORY, new HashMap());
         user.properties.put(Users.BOOST_XP, Boolean.valueOf(false));
         user.properties.put(Users.BOOST_GOLD, Boolean.valueOf(false));
         user.properties.put(Users.BOOST_CP, Boolean.valueOf(false));
         user.properties.put(Users.BOOST_REP, Boolean.valueOf(false));

         user.properties.put(Users.REQUESTED_PARTY, new HashSet());
         user.properties.put(Users.REQUESTED_FRIEND, new HashSet());
         user.properties.put(Users.REQUESTED_DUEL, new HashSet());
         user.properties.put(Users.REQUESTED_GUILD, new HashSet());

// Trade
         user.properties.put(Users.ITEM_OFFER, new HashMap());
         user.properties.put(Users.ITEM_OFFER_ENHANCEMENT, new HashMap());
         user.properties.put(Users.TRADE_TARGET, -1);
         user.properties.put(Users.TRADE_GOLD, 0);
         user.properties.put(Users.TRADE_LOCK, false);
         user.properties.put(Users.TRADE_DEAL, false);

         user.properties.put(Users.AFK, Boolean.valueOf(false));
         user.properties.put(Users.HP, Integer.valueOf(100));
         user.properties.put(Users.GOLD, rs.getInt("Gold"));
         user.properties.put(Users.COINS, rs.getInt("Coins"));
         user.properties.put(Users.HP_MAX, Integer.valueOf(100));
         user.properties.put(Users.MP, Integer.valueOf(100));
         user.properties.put(Users.MP_MAX, Integer.valueOf(100));
         user.properties.put(Users.STATE, Integer.valueOf(1));
         user.properties.put(Users.PVP_TEAM, Integer.valueOf(0));
         user.properties.put(Users.LOAD, false);
      }
   }

   public void log(User user, String violation, String details) {
      int userId = ((Integer) user.properties.get(Users.DATABASE_ID)).intValue();
      this.world.db.jdbc.run("INSERT INTO users_logs (UserID, Violation, Details) VALUES (?, ?, ?)", new Object[]{Integer.valueOf(userId), violation, details});
      if (!user.isBeingKicked) {
         this.world.send(new String[]{"suspicious"}, user);
      }

   }

   public void changePreferences(User user, String pref, boolean value) {
      int ia1 = ((Integer) user.properties.get(Users.SETTINGS)).intValue();
      ia1 = Settings.setPreferences(pref, ia1, value);
      user.properties.put(Users.SETTINGS, Integer.valueOf(ia1));
      JSONObject uotls = new JSONObject();
      uotls.put("cmd", "uotls");
      uotls.put("unm", user.getName());
      if (pref.equals("bHelm")) {
         uotls.put("o", (new JSONObject()).put("showHelm", Boolean.valueOf(Settings.getPreferences("bHelm", ia1))));
         this.world.sendToRoomButOne(uotls, user, this.world.zone.getRoom(user.getRoom()));
      }

      if (pref.equals("bCloak")) {
         uotls.put("o", (new JSONObject()).put("showCloak", Boolean.valueOf(Settings.getPreferences("bCloak", ia1))));
         this.world.sendToRoomButOne(uotls, user, this.world.zone.getRoom(user.getRoom()));
      }

      this.sendPreferences(user, pref);
      this.world.db.jdbc.run("UPDATE users SET Settings = ? WHERE id = ?", new Object[]{Integer.valueOf(ia1), user.properties.get(Users.DATABASE_ID)});
   }

   private void sendPreferences(User user, String pref) {
      int ia1 = ((Integer) user.properties.get(Users.SETTINGS)).intValue();
      boolean value = Settings.getPreferences(pref, ia1);
      if (pref.equals("bParty") && value) {
         this.world.send(new String[]{"server", "Accepting party invites."}, user);
      } else if (pref.equals("bParty") && !value) {
         this.world.send(new String[]{"warning", "Ignoring party invites."}, user);
      }

      if (pref.equals("bGoto") && value) {
         this.world.send(new String[]{"server", "Accepting goto requests."}, user);
      } else if (pref.equals("bGoto") && !value) {
         this.world.send(new String[]{"warning", "Blocking goto requests."}, user);
      }

      if (pref.equals("bFriend") && value) {
         this.world.send(new String[]{"server", "Accepting Friend requests."}, user);
      } else if (pref.equals("bFriend") && !value) {
         this.world.send(new String[]{"warning", "Ignoring Friend requests."}, user);
      }

      if (pref.equals("bWhisper") && value) {
         this.world.send(new String[]{"server", "Accepting PMs."}, user);
      } else if (pref.equals("bWhisper") && !value) {
         this.world.send(new String[]{"warning", "Ignoring PMs."}, user);
      }

      if (pref.equals("bTT") && value) {
         this.world.send(new String[]{"server", "Ability ToolTips will always show on mouseover."}, user);
      } else if (pref.equals("bTT") && !value) {
         this.world.send(new String[]{"warning", "Ability ToolTips will not show on mouseover during combat."}, user);
      }

      if (pref.equals("bDuel") && value) {
         this.world.send(new String[]{"server", "Accepting duel invites."}, user);
      } else if (pref.equals("bDuel") && !value) {
         this.world.send(new String[]{"warning", "Ignoring duel invites."}, user);
      }

      if (pref.equals("bGuild") && value) {
         this.world.send(new String[]{"server", "Accepting guild invites."}, user);
      } else if (pref.equals("bGuild") && !value) {
         this.world.send(new String[]{"warning", "Ignoring guild invites."}, user);
      }
   }

   public void updateClass(User user, Item item, int classPoints) {
      JSONObject updateClass = new JSONObject();
      updateClass.put("cmd", "updateClass");
      updateClass.put("iCP", Integer.valueOf(classPoints));
      updateClass.put("sClassCat", item.classObj.getCategory());
      updateClass.put("sDesc", item.classObj.getDescription());
      updateClass.put("sStats", item.classObj.getStatsDescription());
      updateClass.put("uid", Integer.valueOf(user.getUserId()));
      if (item.classObj.getManaRegenerationMethods().contains(":")) {
         JSONArray aMRM = new JSONArray();
         String[] arr$ = item.classObj.getManaRegenerationMethods().split(",");
         int len$ = arr$.length;

         for (int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            aMRM.add(s + "\r");
         }

         updateClass.put("aMRM", aMRM);
      } else {
         updateClass.put("aMRM", item.classObj.getManaRegenerationMethods());
      }

      updateClass.put("sClassName", item.getName());
      this.world.send(updateClass, user);
      updateClass.clear();
      updateClass.put("cmd", "updateClass");
      updateClass.put("iCP", Integer.valueOf(classPoints));
      updateClass.put("sClassCat", item.classObj.getCategory());
      updateClass.put("sClassName", item.getName());
      updateClass.put("uid", Integer.valueOf(user.getUserId()));
      user.properties.put(Users.CLASS_POINTS, Integer.valueOf(classPoints));
      user.properties.put(Users.CLASS_NAME, item.getName());
      user.properties.put(Users.CLASS_CATEGORY, item.classObj.getCategory());

//Element element = (Element)this.world.elements.get(Integer.valueOf(item.getElementId()));
//user.properties.put("none", element.getElement());
      user.properties.put("none", "Fire");
      this.world.sendToRoomButOne(updateClass, user, this.world.zone.getRoom(user.getRoom()));
//this.loadUserSkills(user, item, classPoints);
      this.loadSkills(user, item, classPoints);
   }

   public void regen(User user) {
      Regeneration regen = (Regeneration) user.properties.get(Users.REGENERATION);
      regen.setRunning(this.world.scheduleTask(regen, 4L, TimeUnit.SECONDS, true));
   }

   private void clearAuras(User user) {
      Set auras = (Set) user.properties.get(Users.AURAS);
      Iterator stats = auras.iterator();

      while (stats.hasNext()) {
         RemoveAura ca = (RemoveAura) stats.next();
         ca.cancel();
      }

      auras.clear();
      Stats stats1 = (Stats) user.properties.get(Users.STATS);
      stats1.effects.clear();
      JSONObject ca1 = new JSONObject();
      ca1.put("cmd", "clearAuras");
      this.world.send(ca1, user);
   }

   private void applyPassiveAuras(User user, int rank, Class classObj) {
      if (rank < 4) return;

      JSONObject aurap = new JSONObject();
      JSONArray auras = new JSONArray();

      Stats stats = (Stats) user.properties.get(Users.STATS);

//      for (Iterator i$ = classObj.skills.iterator(); i$.hasNext(); ) {
//         Map.Entry<Integer, Integer> entry1 = (Map.Entry) i$.next();
//         Skill skill = this.world.skills.get(Integer.valueOf(entry1.getKey()));
//
//         if (skill.getType().equals("passive") && skill.hasAura()) {
//            for (Iterator o$ = skill.auras.entrySet().iterator(); o$.hasNext(); ) {
//               Entry<Integer, Integer> entry2 = (Entry) o$.next();
//               Aura aura = this.world.auras.get(entry2.getKey());
//
//               if (!aura.effects.isEmpty()) {
//                  JSONObject auraObj = new JSONObject();
//                  JSONArray effects = new JSONArray();
//
//                  for (int effectId : aura.effects) {
//                     AuraEffects ae = this.world.effects.get(effectId);
//
//                     JSONObject effect = new JSONObject();
//
//                     effect.put("typ", ae.getType());
//                     effect.put("sta", ae.getStat());
//                     effect.put("id", ae.getId());
//                     effect.put("val", ae.getValue());
//
//                     effects.add(effect);
//
//                     stats.effects.add(ae);
//                  }
//
//                  auraObj.put("nam", aura.getName());
//                  auraObj.put("e", effects);
//
//                  auras.add(auraObj);
//               }
//            }
//         }
//      }

      for (int skillId : classObj.skills) {
         Skill skill = this.world.skills.get(skillId);

         if (skill.getType().equals("passive") && skill.hasAuraId()) {
            Aura aura = this.world.auras.get(skill.getAuraId());

            if (!aura.effects.isEmpty()) {

               JSONObject auraObj = new JSONObject();
               JSONArray effects = new JSONArray();

               for (int effectId : aura.effects) {
                  AuraEffects ae = this.world.effects.get(effectId);

                  JSONObject effect = new JSONObject();

                  effect.put("typ", ae.getType());
                  effect.put("sta", ae.getStat());
                  effect.put("id", ae.getId());
                  effect.put("val", ae.getValue());

                  effects.add(effect);

                  stats.effects.add(ae);
               }

               auraObj.put("nam", aura.getName());
               auraObj.put("e", effects);

               auras.add(auraObj);
            }
         }
      }

      aurap.put("auras", auras);
      aurap.put("cmd", "aura+p");
      aurap.put("tInf", "p:" + user.getUserId());

      this.world.send(aurap, user);
   }

   public JSONArray getGuildHallData(int guildId) {
      JSONArray guildData = new JSONArray();
      QueryResult halls = this.world.db.jdbc.query("SELECT * FROM guilds_halls WHERE GuildID = ?", new Object[]{Integer.valueOf(guildId)});

      while (halls.next()) {
         JSONObject hall = new JSONObject();
         hall.put("intY", Integer.valueOf(halls.getInt("Y")));
         hall.put("intX", Integer.valueOf(halls.getInt("X")));
         hall.put("strLinkage", halls.getString("Linkage"));
         hall.put("ID", Integer.valueOf(halls.getInt("id")));
         hall.put("strCell", halls.getString("Cell"));
         hall.put("strBuildings", this.getBuildingString(halls.getInt("id")));
         hall.put("strConnections", this.getConnectionsString(halls.getInt("id")));
         hall.put("strInterior", halls.getString("Interior"));
         guildData.add(hall);
      }

      halls.close();
      return guildData;
   }

   public String getConnectionsString(int hallId) {
      StringBuilder sb = new StringBuilder();
      QueryResult result = this.world.db.jdbc.query("SELECT * FROM guilds_halls_connections WHERE HallID = ?", new Object[]{Integer.valueOf(hallId)});

      while (result.next()) {
         sb.append(result.getString("Pad")).append(",");
         sb.append(result.getString("Cell")).append(",");
         sb.append(result.getString("PadPosition")).append("|");
      }

      result.close();
      if (sb.length() <= 0) {
         return sb.toString();
      } else {
         int index = sb.length() - 1;
         return sb.deleteCharAt(index).toString();
      }
   }

   public String getBuildingString(int hallId) {
      StringBuilder sb = new StringBuilder();
      QueryResult result = this.world.db.jdbc.query("SELECT * FROM guilds_halls_buildings WHERE HallID = ?", new Object[]{Integer.valueOf(hallId)});

      while (result.next()) {
         Item index = (Item) this.world.items.get(Integer.valueOf(result.getInt("ItemID")));
         sb.append("slot:").append(result.getInt("Slot")).append(",");
         sb.append("size:").append(result.getInt("Size")).append(",");
         sb.append("itemID:").append(result.getInt("ItemID")).append(",");
         sb.append("linkage:").append(index.getLink()).append(",");
         sb.append("file:").append(index.getFile()).append("|");
      }

      result.close();
      if (sb.length() <= 0) {
         return sb.toString();
      } else {
         int index1 = sb.length() - 1;
         return sb.deleteCharAt(index1).toString();
      }
   }

   public JSONObject getGuildObject(int guildId) {
      JSONObject guild = new JSONObject();
      QueryResult result = this.world.db.jdbc.query("SELECT * FROM guilds WHERE id = ?", new Object[]{Integer.valueOf(guildId)});
      if (result.next()) {
         JSONArray members = new JSONArray();
         guild.put("Name", result.getString("Name"));
         guild.put("Color", result.getString("Color"));
//       guild.put("guildColor", result.getString("GuildColor"));
         guild.put("MOTD", result.getString("MessageOfTheDay").length() > 0 ? result.getString("MessageOfTheDay") : "undefined");
         guild.put("pending", new JSONObject());
         guild.put("MaxMembers", Integer.valueOf(result.getInt("MaxMembers")));
         guild.put("dateUpdated", (new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss")).format(result.getDate("LastUpdated")));
         guild.put("HallSize", Integer.valueOf(result.getInt("HallSize")));
         guild.put("Wins", Integer.valueOf(result.getInt("Wins")));
         guild.put("Loses", Integer.valueOf(result.getInt("Loss")));
         guild.put("TotalKills", Integer.valueOf(result.getInt("TotalKills")));
         guild.put("Level", Integer.valueOf(result.getInt("Level")));
         guild.put("Exp", Integer.valueOf(result.getInt("Experience")));
         guild.put("ExpToLevel", Integer.valueOf(this.world.getGuildExpToLevel(result.getInt("Level"))));
         guild.put("Gold", Integer.valueOf(1));
         guild.put("Coins", Integer.valueOf(1));
         guild.put("Silvers", Integer.valueOf(1));
         guild.put("guildHall", new JSONArray());
         result.close();
         QueryResult memberResult = this.world.db.jdbc.query("SELECT id, username, Level, CurrentServer, Rank FROM users_guilds JOIN users WHERE id = UserID AND users_guilds.GuildID = ?", new Object[]{Integer.valueOf(guildId)});

         while (memberResult.next()) {
            JSONObject member = new JSONObject();
            member.put("ID", Integer.valueOf(memberResult.getInt("id")));
            member.put("userName", memberResult.getString("username"));
            member.put("Level", memberResult.getString("Level"));
            member.put("Rank", Integer.valueOf(memberResult.getInt("Rank")));
            member.put("Server", memberResult.getString("CurrentServer"));
            members.add(member);
         }

         memberResult.close();
         guild.put("ul", members);
      }

      result.close();
      return guild;
   }

   public void loadSkills(User user, Item item, int classPoints) {
      int rank = Rank.getRankFromPoints(classPoints);
      Map skills = (Map) user.properties.get(Users.SKILLS);
      Item weaponItem = (Item) user.properties.get(Users.ITEM_WEAPON);
      JSONArray active = new JSONArray();
      JSONArray passive = new JSONArray();
      JSONObject sAct = new JSONObject();
      sAct.put("cmd", "sAct");
      Iterator actions = item.classObj.skills.iterator();

      while (actions.hasNext()) {
         int skill = ((Integer) actions.next()).intValue();
         Skill actObj = this.world.skills.get(Integer.valueOf(skill));
         JSONObject arrAuras;
         if (actObj.getType().equals("passive")) {
            arrAuras = new JSONObject();
            arrAuras.put("desc", actObj.getDescription());
            arrAuras.put("fx", actObj.getEffects());
            arrAuras.put("icon", actObj.getIcon());
            arrAuras.put("id", Integer.valueOf(skill));
            arrAuras.put("nam", actObj.getName());
            arrAuras.put("range", Integer.valueOf(actObj.getRange()));
            arrAuras.put("ref", actObj.getReference());
            arrAuras.put("tgt", actObj.getTarget());
            arrAuras.put("typ", actObj.getType());

            JSONArray arrAuras1 = new JSONArray();
            arrAuras1.add(new JSONObject());
            arrAuras.put("auras", arrAuras1);
            if (rank < 4) {
               arrAuras.put("isOK", Boolean.valueOf(false));
            } else {
               arrAuras.put("isOK", Boolean.valueOf(true));
            }

            passive.add(arrAuras);
            skills.put(actObj.getReference(), Integer.valueOf(skill));
         } else {
            arrAuras = new JSONObject();
            arrAuras.put("anim", actObj.getAnimation());
            arrAuras.put("cd", String.valueOf(actObj.getCooldown()));
            arrAuras.put("damage", Double.valueOf(actObj.getDamage()));
            arrAuras.put("desc", actObj.getDescription());
            if (!actObj.getDsrc().isEmpty()) {
               arrAuras.put("dsrc", actObj.getDsrc());
            }

            arrAuras.put("fx", actObj.getEffects());
            arrAuras.put("icon", actObj.getIcon());
            arrAuras.put("id", Integer.valueOf(skill));
            arrAuras.put("isOK", Boolean.valueOf(true));
            arrAuras.put("mp", String.valueOf(actObj.getMana()));
            arrAuras.put("nam", actObj.getName());
            arrAuras.put("range", String.valueOf(actObj.getRange()));
            arrAuras.put("ref", actObj.getReference());
            if (!actObj.getStrl().isEmpty()) {
               arrAuras.put("strl", actObj.getStrl());
            }

            arrAuras.put("tgt", actObj.getTarget());
            arrAuras.put("typ", actObj.getType());
            if (rank < 2 && actObj.getReference().equals("a2")) {
               arrAuras.put("isOK", Boolean.valueOf(false));
            }

            if (rank < 3 && actObj.getReference().equals("a3")) {
               arrAuras.put("isOK", Boolean.valueOf(false));
            }

            if (rank < 5 && actObj.getReference().equals("a4")) {
               arrAuras.put("isOK", Boolean.valueOf(false));
            }

            if (actObj.getHitTargets() > 0) {
               arrAuras.put("tgtMax", String.valueOf(actObj.getHitTargets()));
               arrAuras.put("tgtMin", "1");
            }

            if (actObj.getReference().equals("aa")) {
               arrAuras.put("auto", Boolean.valueOf(true));
               arrAuras.put("typ", "aa");
               active.element(0, arrAuras);
            } else if (actObj.getReference().equals("a1")) {
               active.element(1, arrAuras);
            } else if (actObj.getReference().equals("a2")) {
               if (rank < 2) {
                  arrAuras.put("isOK", Boolean.valueOf(false));
               }

               active.element(2, arrAuras);
            } else if (actObj.getReference().equals("a3")) {
               if (rank < 3) {
                  arrAuras.put("isOK", Boolean.valueOf(false));
               }

               active.element(3, arrAuras);
            } else if (actObj.getReference().equals("a4")) {
               if (rank < 5) {
                  arrAuras.put("isOK", Boolean.valueOf(false));
               }

               active.element(4, arrAuras);
            }

            skills.put(actObj.getReference(), Integer.valueOf(skill));
         }
      }

      JSONObject actions1;
      if (weaponItem != null && this.world.specialskills.containsKey(Integer.valueOf(weaponItem.getId()))) {
         int actions2 = (this.world.specialskills.get(Integer.valueOf(weaponItem.getId()))).intValue();
         Skill skill1 = this.world.skills.get(Integer.valueOf(actions2));
         JSONObject actObj1;
         if (skill1.getType().equals("passive")) {
            actObj1 = new JSONObject();
            actObj1.put("desc", skill1.getDescription());
            actObj1.put("fx", skill1.getEffects());
            actObj1.put("icon", skill1.getIcon());
            actObj1.put("id", Integer.valueOf(actions2));
            actObj1.put("nam", skill1.getName());
            actObj1.put("range", Integer.valueOf(skill1.getRange()));
            actObj1.put("ref", skill1.getReference());
            actObj1.put("tgt", skill1.getTarget());
            actObj1.put("typ", skill1.getType());
            JSONArray arrAuras2 = new JSONArray();
            arrAuras2.add(new JSONObject());
            actObj1.put("auras", arrAuras2);
            if (rank < 4) {
               actObj1.put("isOK", Boolean.valueOf(false));
            } else {
               actObj1.put("isOK", Boolean.valueOf(true));
            }

            passive.add(actObj1);
            skills.put(skill1.getReference(), Integer.valueOf(actions2));
         } else {
            actObj1 = new JSONObject();
            actObj1.put("anim", skill1.getAnimation());
            actObj1.put("cd", String.valueOf(skill1.getCooldown()));
            actObj1.put("damage", Double.valueOf(skill1.getDamage()));
            actObj1.put("desc", skill1.getDescription());
            if (!skill1.getDsrc().isEmpty()) {
               actObj1.put("dsrc", skill1.getDsrc());
            }

            actObj1.put("fx", skill1.getEffects());
            actObj1.put("icon", skill1.getIcon());
            actObj1.put("id", Integer.valueOf(actions2));
            actObj1.put("isOK", Boolean.valueOf(true));
            actObj1.put("mp", String.valueOf(skill1.getMana()));
            actObj1.put("nam", skill1.getName());
            actObj1.put("range", String.valueOf(skill1.getRange()));
            actObj1.put("ref", skill1.getReference());
            if (!skill1.getStrl().isEmpty()) {
               actObj1.put("strl", skill1.getStrl());
            }

            actObj1.put("tgt", skill1.getTarget());
            actObj1.put("typ", skill1.getType());
            if (skill1.getHitTargets() > 0) {
               actObj1.put("tgtMax", String.valueOf(skill1.getHitTargets()));
            }

            actObj1.put("tgtMin", "1");
            active.element(5, actObj1);
            skills.put(skill1.getReference(), Integer.valueOf(actions2));
         }

         this.world.send(new String[]{"server", "Special skill activated"}, user);
      } else {
         actions1 = new JSONObject();
         actions1.put("anim", "Cheer");
         actions1.put("cd", "60000");
         actions1.put("desc", "Equip a potion or scroll from your inventory to use it here.");
         actions1.put("fx", "");
         actions1.put("icon", "icu1");
         actions1.put("isOK", Boolean.valueOf(true));
         actions1.put("mp", "0");
         actions1.put("nam", "Potions");
         actions1.put("range", Integer.valueOf(808));
         actions1.put("ref", "i1");
         actions1.put("str1", "");
         actions1.put("tgt", "f");
         actions1.put("typ", "i");
         active.element(5, actions1);
      }

      actions1 = new JSONObject();
      actions1.put("active", active);
      actions1.put("passive", passive);
      sAct.put("actions", actions1);
      this.clearAuras(user);
      this.applyPassiveAuras(user, rank, item.classObj);
      this.world.send(sAct, user);
   }

   public JSONObject getProperties(User user, Room room) {
      JSONObject userprop = new JSONObject();
      userprop.put("afk", user.properties.get(Users.AFK));
      userprop.put("entID", Integer.valueOf(user.getUserId()));
      userprop.put("entType", "p");
      userprop.put("intHP", user.properties.get(Users.HP));
      userprop.put("intHPMax", user.properties.get(Users.HP_MAX));
      userprop.put("intLevel", user.properties.get(Users.LEVEL));
      userprop.put("intMP", user.properties.get(Users.MP));
      userprop.put("intMPMax", user.properties.get(Users.MP_MAX));
      userprop.put("intState", user.properties.get(Users.STATE));
      userprop.put("showCloak", Boolean.valueOf(true));
      userprop.put("showHelm", Boolean.valueOf(true));
      userprop.put("strFrame", user.properties.get(Users.FRAME));
      userprop.put("strPad", user.properties.get(Users.PAD));
      userprop.put("strUsername", user.properties.get(Users.USERNAME));
      userprop.put("strElement", user.properties.get("none"));
      userprop.put("tx", user.properties.get(Users.TX));
      userprop.put("ty", user.properties.get(Users.TY));
      userprop.put("uoName", user.getName());
      if (!room.getName().contains("house") && (this.world.areas.get(room.getName().split("-")[0])).isPvP()) {
         userprop.put("pvpTeam", user.properties.get(Users.PVP_TEAM));
      }

      return userprop;
   }

   public void updateStats(User user, Enhancement enhancement, String equipment) {
      Map<String, Double> itemStats = this.world.getItemStats(enhancement, equipment);
      Stats stats = (Stats) user.properties.get(Users.STATS);
      if (equipment.equals("ar")) {
         for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
            stats.armor.put(entry.getKey(), entry.getValue());
         }
      } else if (equipment.equals("Weapon")) {
         for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
            stats.weapon.put(entry.getKey(), entry.getValue());
         }
      } else if (equipment.equals("ba")) {
         for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
            stats.cape.put(entry.getKey(), entry.getValue());
         }
      } else if (equipment.equals("he")) {
         for (Map.Entry<String, Double> entry : itemStats.entrySet()) {
            stats.helm.put(entry.getKey(), entry.getValue());
         }
      } else {
         throw new IllegalArgumentException("equipment " + equipment + " cannot have stat values!");
      }
   }

   public void sendStats(User user) {
      this.sendStats(user, false);
   }

   public void sendStats(User user, boolean levelUp) {
      JSONObject stu = new JSONObject();
      JSONObject tempStat = new JSONObject();
      int userLevel = ((Integer) user.properties.get(Users.LEVEL)).intValue();
      Stats stats = (Stats) user.properties.get(Users.STATS);
      stats.update();
      int END = (int) ((double) stats.get$END() + stats.get_END());
      int WIS = (int) ((double) stats.get$WIS() + stats.get_WIS());
      int intHPperEND = ((Double) this.world.coreValues.get("intHPperEND")).intValue();
      int intMPperWIS = ((Double) this.world.coreValues.get("intMPperWIS")).intValue();
      int addedHP = END * intHPperEND;
      int userHp = this.world.getHealthByLevel(userLevel);
      userHp += addedHP;
      int userMp = this.world.getManaByLevel(userLevel) + WIS * intMPperWIS;
      user.properties.put(Users.HP_MAX, Integer.valueOf(userHp));
      user.properties.put(Users.MP_MAX, Integer.valueOf(userMp));
      if (((Integer) user.properties.get(Users.STATE)).intValue() == 1 || levelUp) {
         user.properties.put(Users.HP, Integer.valueOf(userHp));
      }

      if (((Integer) user.properties.get(Users.STATE)).intValue() == 1 || levelUp) {
         user.properties.put(Users.MP, Integer.valueOf(userMp));
      }

      this.world.users.sendUotls(user, true, true, true, true, levelUp, false);
      JsonConfig config = new JsonConfig();
      config.setExcludes(new String[]{"maxDmg", "minDmg"});
      JSONObject stat = JSONObject.fromObject(stats, config);
      JSONObject ba = new JSONObject();
      JSONObject he = new JSONObject();
      JSONObject Weapon = new JSONObject();
      JSONObject innate = new JSONObject();
      JSONObject ar = new JSONObject();
      innate.put("INT", stats.innate.get("INT"));
      innate.put("STR", stats.innate.get("STR"));
      innate.put("DEX", stats.innate.get("DEX"));
      innate.put("END", stats.innate.get("END"));
      innate.put("LCK", stats.innate.get("LCK"));
      innate.put("WIS", stats.innate.get("WIS"));
      Iterator i$ = stats.armor.entrySet().iterator();

      Entry entry;
      while (i$.hasNext()) {
         entry = (Entry) i$.next();
         if (((Double) entry.getValue()).doubleValue() > 0.0D) {
            ar.put(entry.getKey(), Integer.valueOf(((Double) entry.getValue()).intValue()));
         }
      }

      i$ = stats.helm.entrySet().iterator();

      while (i$.hasNext()) {
         entry = (Entry) i$.next();
         if (((Double) entry.getValue()).doubleValue() > 0.0D) {
            he.put(entry.getKey(), Integer.valueOf(((Double) entry.getValue()).intValue()));
         }
      }

      i$ = stats.weapon.entrySet().iterator();

      while (i$.hasNext()) {
         entry = (Entry) i$.next();
         if (((Double) entry.getValue()).doubleValue() > 0.0D) {
            Weapon.put(entry.getKey(), Integer.valueOf(((Double) entry.getValue()).intValue()));
         }
      }

      i$ = stats.cape.entrySet().iterator();

      while (i$.hasNext()) {
         entry = (Entry) i$.next();
         if (((Double) entry.getValue()).doubleValue() > 0.0D) {
            ba.put(entry.getKey(), Integer.valueOf(((Double) entry.getValue()).intValue()));
         }
      }

      if (!ba.isEmpty()) {
         tempStat.put("ba", ba);
      }

      if (!ar.isEmpty()) {
         tempStat.put("ar", ar);
      }

      if (!Weapon.isEmpty()) {
         tempStat.put("Weapon", Weapon);
      }

      if (!he.isEmpty()) {
         tempStat.put("he", he);
      }

      tempStat.put("innate", innate);
      stu.put("tempSta", tempStat);
      stu.put("cmd", "stu");
      stu.put("sta", stat);
      stu.put("wDPS", Integer.valueOf(stats.wDPS));
      this.world.send(stu, user);
   }

   public JSONArray getFriends(User user) {
      JSONArray friends = new JSONArray();
      QueryResult result = this.world.db.jdbc.query("SELECT id, Level, username, CurrentServer FROM users LEFT JOIN users_friends ON FriendID = id WHERE UserID = ?", user.properties.get(Users.DATABASE_ID));

      while (result.next()) {
         JSONObject temp = new JSONObject();
         temp.put("iLvl", Integer.valueOf(result.getInt("Level")));
         temp.put("ID", Integer.valueOf(result.getInt("id")));
         temp.put("sName", result.getString("Name"));
         temp.put("sServer", result.getString("CurrentServer"));
         friends.add(temp);
      }

      result.close();
      return friends;
   }

   public void dropItem(User user, int itemId) {
      this.dropItem(user, itemId, 1);
   }

   public void dropItem(User user, int itemId, int quantity)
   {
//      Item itemObj = (Item) this.world.items.get(Integer.valueOf(itemId));
      Item itemObj = this.world.items.get(itemId);
      Map<Integer, Integer> tempInventory = (Map<Integer, Integer>) user.properties.get(Users.TEMPORARY_INVENTORY);

      if (!itemObj.getReqQuests().isEmpty()) {
         String[] arrQuests;
         Boolean getDrop = false;

         if (itemObj.getReqQuests().contains(","))
            arrQuests = itemObj.getReqQuests().split(",");
         else
            arrQuests = new String[]{itemObj.getReqQuests()};

         Set<Integer> acceptedQuests = (Set<Integer>) user.properties.get(Users.QUESTS);

         for (String questId : arrQuests)
            if (acceptedQuests.contains(Integer.parseInt(questId))) {
               getDrop = true;
               break;
            }

         if (!getDrop)
            return;
      }

//      Map var12 = (Map) user.properties.get(Users.TEMPORARY_INVENTORY);
      if (itemObj.isTemporary()) {
         if (tempInventory.containsKey(itemId)) {
            if (tempInventory.get(itemId) < itemObj.getStack())
               addTemporaryItem(user, itemId, quantity);
            else
               return;
         } else
            addTemporaryItem(user, itemId, quantity);
      } else {
         QueryResult itemResult = this.world.db.getJdbc().query("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0", itemId, user.properties.get(Users.DATABASE_ID));
         if (itemResult.next()) {
            int quantityInInventory = itemResult.getInt("Quantity");
            itemResult.close();
            if (quantityInInventory >= itemObj.getStack())
               return;
         }
         itemResult.close();

         Map userDrops = (Map) user.properties.get(Users.DROPS);
         int totalDropQty = quantity;
         if (userDrops.containsKey(itemId)) {
            if (itemObj.getStack() == 1)
               return;
            totalDropQty += (Integer) userDrops.get(itemId);
         }

         userDrops.put(itemId, totalDropQty);
      }


//      JSONObject item = itemObj.getType().equals("Fortification") ? Item.getItemJSON(itemObj, world.enhancements.get(itemObj.getEnhId())) : Item.getItemJSON(itemObj);
//
//      JSONObject var14 = new JSONObject();
//      JSONObject var16 = new JSONObject();
//      JSONObject var17;
//      if (itemObj.getType().equals("Enhancement")) {
//         var17 = Item.getItemJSON(itemObj, (Enhancement) this.world.enhancements.get(Integer.valueOf(itemObj.getEnhId())));
//      } else {
//         var17 = Item.getItemJSON(itemObj);
//      }

      JSONObject item = Item.getItemJSON(itemObj, (Enhancement) this.world.enhancements.get(Integer.valueOf(itemObj.getEnhId())));
      item.put("iQty", quantity);
      item.put("showDrop", itemObj.isTemporary() ? 0 : 1);
//      var16.put(String.valueOf(itemId), var17);

      JSONObject arrItems = new JSONObject();
      arrItems.put(String.valueOf(itemId), item);

      JSONObject di = new JSONObject();
      di.put("items", arrItems);
      di.put("cmd", itemObj.isTemporary() ? "addItems" : "dropItem");

      this.world.send(di, user);
   }

   public void setQuestValue(User user, int index, int value) {
      if (index > 99) {
         user.properties.put("quests2", Quests.updateValue((String) user.properties.get("quests2"), index - 100, value));
         this.world.db.jdbc.run("UPDATE users SET Quests2 = ? WHERE id =  ?", user.properties.get("quests2"), user.properties.get("dbId"));
      } else {
         user.properties.put("quests1", Quests.updateValue((String) user.properties.get("quests1"), index, value));
         this.world.db.jdbc.run("UPDATE users SET Quests = ? WHERE id = ?", user.properties.get("quests1"), user.properties.get("dbId"));
      }

      JSONObject updateQuest = new JSONObject();
      updateQuest.put("cmd", "updateQuest");
      updateQuest.put("iIndex", Integer.valueOf(index));
      updateQuest.put("iValue", Integer.valueOf(value));
      this.world.send(updateQuest, user);
   }

//   public void setQuestValue(User user, int index, int value) {
//      if (index > 99) {
//         user.properties.put(Users.QUESTS_2, Quests.updateValue((String) user.properties.get(Users.QUESTS_2), (index - 100), value));
//         this.world.db.getJdbc().run("UPDATE users_characters SET Quests2 = ? WHERE id =  ?", user.properties.get(Users.QUESTS_2), user.properties.get(Users.DATABASE_ID));
//      } else {
//         user.properties.put(Users.QUESTS_1, Quests.updateValue((String) user.properties.get(Users.QUESTS_1), index, value));
//         this.world.db.getJdbc().run("UPDATE users_characters SET Quests = ? WHERE id = ?", user.properties.get(Users.QUESTS_1), user.properties.get(Users.DATABASE_ID));
//      }
//
//      JSONObject updateQuest = new JSONObject();
//      updateQuest.put("cmd", "kqmwjgmasfasf"); //updateQuest
//      updateQuest.put("iIndex", index);
//      updateQuest.put("iValue", value);
//
//      this.world.send(updateQuest, user);
//   }

//   public void setQuestValue(User user, int index, int value) {
//      if (index > 399) {
//         user.properties.put(Users.QUESTS_5, Quests.updateValue((String) user.properties.get(Users.QUESTS_5), index - 400, value));
//         this.world.db.jdbc.run("UPDATE users SET Quests5 = ? WHERE id =  ?", new Object[]{user.properties.get(Users.QUESTS_5), user.properties.get(Users.DATABASE_ID)});
//      } else if (index > 299) {
//         user.properties.put(Users.QUESTS_4, Quests.updateValue((String) user.properties.get(Users.QUESTS_4), index - 300, value));
//         this.world.db.jdbc.run("UPDATE users SET Quests4 = ? WHERE id =  ?", new Object[]{user.properties.get(Users.QUESTS_4), user.properties.get(Users.DATABASE_ID)});
//      } else if (index > 199) {
//         user.properties.put(Users.QUESTS_3, Quests.updateValue((String) user.properties.get(Users.QUESTS_3), index - 200, value));
//         this.world.db.jdbc.run("UPDATE users SET Quests3 = ? WHERE id =  ?", new Object[]{user.properties.get(Users.QUESTS_3), user.properties.get(Users.DATABASE_ID)});
//      } else if (index > 99) {
//         user.properties.put(Users.QUESTS_2, Quests.updateValue((String) user.properties.get(Users.QUESTS_2), index - 100, value));
//         this.world.db.jdbc.run("UPDATE users SET Quests2 = ? WHERE id =  ?", new Object[]{user.properties.get(Users.QUESTS_2), user.properties.get(Users.DATABASE_ID)});
//      } else {
//         user.properties.put(Users.QUESTS_1, Quests.updateValue((String) user.properties.get(Users.QUESTS_1), index, value));
//         this.world.db.jdbc.run("UPDATE users SET Quests1 = ? WHERE id = ?", new Object[]{user.properties.get(Users.QUESTS_1), user.properties.get(Users.DATABASE_ID)});
//      }
//
//      JSONObject updateQuest = new JSONObject();
//      updateQuest.put("cmd", "updateQuest");
//      updateQuest.put("iIndex", Integer.valueOf(index));
//      updateQuest.put("iValue", Integer.valueOf(value));
//      this.world.send(updateQuest, user);
//   }

   public int getQuestValue(User user, int index) {
      if (index > 99) return Quests.lookAtValue((String)user.properties.get("quests2"), index - 100);
      return Quests.lookAtValue((String)user.properties.get("quests1"), index);
//      if (index > 99) return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_2), (index - 100));
//      return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_1), index);
//      return index > 99 ? Quests.lookAtValue((String) user.properties.get("quests2"), index - 100) : Quests.lookAtValue((String) user.properties.get("quests1"), index);
   }

//   public int getQuestValue(User user, int index) {
//      if (index > 399) {
//         return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_5), index - 400);
//      } else if (index > 299) {
//         return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_4), index - 300);
//      } else if (index > 199) {
//         return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_3), index - 200);
//      } else if (index > 99) {
//         return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_2), index - 100);
//      } else {
//         return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_1), index);
//      }
//   }

   public void setAchievement(String field, int index, int value, User user) {
      if (field.equals("ia0")) {
         user.properties.put(Users.ACHIEVEMENT, Integer.valueOf(Achievement.update(((Integer) user.properties.get(Users.ACHIEVEMENT)).intValue(), index, value)));
         this.world.db.jdbc.run("UPDATE users SET Achievement = ? WHERE id = ?", new Object[]{user.properties.get(Users.ACHIEVEMENT), user.properties.get(Users.DATABASE_ID)});
      } else if (field.equals("id0")) {
         user.properties.put(Users.QUEST_DAILY_0, Integer.valueOf(Achievement.update(((Integer) user.properties.get(Users.QUEST_DAILY_0)).intValue(), index, value)));
         this.world.db.jdbc.run("UPDATE users SET DailyQuests0 = ? WHERE id = ?", new Object[]{user.properties.get(Users.QUEST_DAILY_0), user.properties.get(Users.DATABASE_ID)});
      } else if (field.equals("id1")) {
         user.properties.put(Users.QUEST_DAILY_1, Integer.valueOf(Achievement.update(((Integer) user.properties.get(Users.QUEST_DAILY_1)).intValue(), index, value)));
         this.world.db.jdbc.run("UPDATE users SET DailyQuests1 = ? WHERE id = ?", new Object[]{user.properties.get(Users.QUEST_DAILY_1), user.properties.get(Users.DATABASE_ID)});
      } else if (field.equals("id2")) {
         user.properties.put(Users.QUEST_DAILY_2, Integer.valueOf(Achievement.update(((Integer) user.properties.get(Users.QUEST_DAILY_2)).intValue(), index, value)));
         this.world.db.jdbc.run("UPDATE users SET DailyQuests2 = ? WHERE id = ?", new Object[]{user.properties.get(Users.QUEST_DAILY_2), user.properties.get(Users.DATABASE_ID)});
      } else if (field.equals("im0")) {
         user.properties.put(Users.QUEST_MONTHLY_0, Integer.valueOf(Achievement.update(((Integer) user.properties.get(Users.QUEST_MONTHLY_0)).intValue(), index, value)));
         this.world.db.jdbc.run("UPDATE users SET MonthlyQuests0 = ? WHERE id = ?", new Object[]{user.properties.get(Users.QUEST_MONTHLY_0), user.properties.get(Users.DATABASE_ID)});
      }

      JSONObject sa = new JSONObject();
      sa.put("cmd", "setAchievement");
      sa.put("field", field);
      sa.put("index", Integer.valueOf(index));
      sa.put("value", Integer.valueOf(value));
      this.world.send(sa, user);
   }

   public int getAchievement(String field, int index, User user) {
      return field.equals("ia0") ? Achievement.get(((Integer) user.properties.get(Users.ACHIEVEMENT)).intValue(), index) : (field.equals("id0") ? Achievement.get(((Integer) user.properties.get(Users.QUEST_DAILY_0)).intValue(), index) : (field.equals("id1") ? Achievement.get(((Integer) user.properties.get(Users.QUEST_DAILY_1)).intValue(), index) : (field.equals("id2") ? Achievement.get(((Integer) user.properties.get(Users.QUEST_DAILY_2)).intValue(), index) : (field.equals("im0") ? Achievement.get(((Integer) user.properties.get(Users.QUEST_MONTHLY_0)).intValue(), index) : -1))));
   }

   public String getGuildRank(int rank) {
      String rankName = "";
      switch (rank) {
         case 0:
            rankName = "duffer";
            break;
         case 1:
            rankName = "member";
            break;
         case 2:
            rankName = "officer";
            break;
         case 3:
            rankName = "leader";
      }

      return rankName;
   }

   public String getCustomGuildRank(Integer rank, Integer guildId) {
      String rankName = "";
      QueryResult result = this.world.db.jdbc.query("SELECT * FROM guilds WHERE id = ?", new Object[]{guildId});
      if (result.next()) {
         String Rank0 = this.world.db.jdbc.queryForString("SELECT Rank0Name FROM guilds_customranks WHERE id = ?", new Object[]{guildId});
         String Rank1 = this.world.db.jdbc.queryForString("SELECT Rank1Name FROM guilds_customranks WHERE id = ?", new Object[]{guildId});
         String Rank2 = this.world.db.jdbc.queryForString("SELECT Rank2Name FROM guilds_customranks WHERE id = ?", new Object[]{guildId});
         String Rank3 = this.world.db.jdbc.queryForString("SELECT Rank3Name FROM guilds_customranks WHERE id = ?", new Object[]{guildId});
         switch (rank.intValue()) {
            case 0:
               rankName = Rank0;
               break;
            case 1:
               rankName = Rank1;
               break;
            case 2:
               rankName = Rank2;
               break;
            case 3:
               rankName = Rank3;
         }
      }

      result.close();
      return rankName;
   }

   public boolean turnInItem(User user, int itemId, int quantity) {
      HashMap items = new HashMap();
      items.put(Integer.valueOf(itemId), Integer.valueOf(quantity));
      return this.turnInItems(user, items);
   }

   public boolean turnInItems(User user, Map<Integer, Integer> items) {
      boolean valid = true;
      StringBuilder sItems = new StringBuilder();
      this.world.db.jdbc.beginTransaction();

      try {
         Iterator ti = items.entrySet().iterator();

         while (ti.hasNext()) {
            Entry entry = (Entry) ti.next();
            int itemId = ((Integer) entry.getKey()).intValue();
            int quantityRequirement = ((Integer) entry.getValue()).intValue();
            Item item = (Item) this.world.items.get(Integer.valueOf(itemId));
            if (item.isTemporary()) {
               Map itemResult = (Map) user.properties.get(Users.TEMPORARY_INVENTORY);
               if (!itemResult.containsKey(Integer.valueOf(itemId))) {
                  valid = false;
                  this.log(user, "Suspicous TurnIn", "Turning in a temporary item not found in temp. inventory.");
                  this.world.db.jdbc.rollbackTransaction();
                  break;
               }

               if (((Integer) itemResult.get(Integer.valueOf(itemId))).intValue() < quantityRequirement) {
                  valid = false;
                  this.log(user, "Suspicous TurnIn", "Quantity requirement for turning in item is lacking.");
                  this.world.db.jdbc.rollbackTransaction();
                  break;
               }

               itemResult.remove(Integer.valueOf(itemId));
               valid = true;
            } else {
               QueryResult itemResult1 = this.world.db.jdbc.query("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", new Object[]{Integer.valueOf(itemId), user.properties.get(Users.DATABASE_ID)});
               if (!itemResult1.next()) {
                  valid = false;
                  itemResult1.close();
                  this.world.users.log(user, "Suspicous TurnIn", "Item to turn in not found in database.");
                  this.world.db.jdbc.rollbackTransaction();
                  break;
               }

               int quantity = itemResult1.getInt("Quantity");
               itemResult1.close();
               if (item.getStack() > 1) {
                  int quantityLeft = quantity - quantityRequirement;
                  if (quantityLeft > 0) {
                     this.world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE ItemID = ? AND UserID = ?", new Object[]{Integer.valueOf(quantityLeft), Integer.valueOf(itemId), user.properties.get(Users.DATABASE_ID)});
                  } else {
                     this.world.db.jdbc.run("DELETE FROM users_items WHERE ItemID = ? AND UserID = ?", new Object[]{Integer.valueOf(itemId), user.properties.get(Users.DATABASE_ID)});
                  }
               } else {
                  this.world.db.jdbc.run("DELETE FROM users_items WHERE ItemID = ? AND UserID = ?", new Object[]{Integer.valueOf(itemId), user.properties.get(Users.DATABASE_ID)});
               }

               valid = true;
               itemResult1.close();
            }

            sItems.append(itemId);
            sItems.append(":");
            sItems.append(quantityRequirement);
            sItems.append(",");
         }
      } catch (JdbcException var16) {
         if (this.world.db.jdbc.isInTransaction()) {
            this.world.db.jdbc.rollbackTransaction();
         }

         SmartFoxServer.log.severe("Error in turn in transaction: " + var16.getMessage());
      } finally {
         if (this.world.db.jdbc.isInTransaction()) {
            this.world.db.jdbc.commitTransaction();
         }

      }

      if (valid && !items.isEmpty()) {
         JSONObject ti1 = new JSONObject();
         ti1.put("cmd", "turnIn");
         ti1.put("sItems", sItems.toString().substring(0, sItems.toString().length() - 1));
         this.world.send(ti1, user);
      }

      return valid;
   }

   public void addTemporaryItem(User user, int itemId, int quantity) {
      Map tempInventory = (Map) user.properties.get(Users.TEMPORARY_INVENTORY);
      if (tempInventory.containsKey(Integer.valueOf(itemId))) {
         int deltaQuantity = ((Integer) tempInventory.get(Integer.valueOf(itemId))).intValue() + quantity;
         tempInventory.put(Integer.valueOf(itemId), Integer.valueOf(deltaQuantity));
      } else {
         tempInventory.put(Integer.valueOf(itemId), Integer.valueOf(quantity));
      }

   }

   public void lost(User user) {

      this.world.warzoneQueue.removeUserFromQueues(user.getUserId());
      int partyId = ((Integer) user.properties.get(Users.PARTY_ID)).intValue();
      JSONObject updateFriend;
      JSONObject friendInfo;
      if (partyId > 0) {
         PartyInfo guildId = this.world.parties.getPartyInfo(partyId);
         if (guildId.getOwner().equals(user.properties.get(Users.USERNAME))) {
            guildId.setOwner(guildId.getNextOwner());
         }

         guildId.removeMember(user);
         updateFriend = new JSONObject();
         updateFriend.put("cmd", "pr");
         updateFriend.put("owner", guildId.getOwner());
         updateFriend.put("typ", "l");
         updateFriend.put("unm", user.properties.get(Users.USERNAME));
         this.world.send(updateFriend, guildId.getChannelListButOne(user));
         this.world.send(updateFriend, user);
         if (guildId.getMemberCount() <= 0) {
            friendInfo = new JSONObject();
            friendInfo.put("cmd", "pc");
            this.world.send(friendInfo, guildId.getOwnerObject());
            this.world.parties.removeParty(partyId);
            guildId.getOwnerObject().properties.put(Users.PARTY_ID, Integer.valueOf(-1));
         }
      }

      this.world.db.jdbc.run("UPDATE users SET LastArea = ?, CurrentServer = \'Offline\' WHERE id = ?", new Object[]{user.properties.get(Users.LAST_AREA), user.properties.get(Users.DATABASE_ID)});
      int guildId1 = ((Integer) user.properties.get(Users.GUILD_ID)).intValue();
      if (guildId1 > 0) {
         this.world.sendGuildUpdate(this.getGuildObject(guildId1));
      }

      updateFriend = new JSONObject();
      friendInfo = new JSONObject();
      updateFriend.put("cmd", "updateFriend");
      friendInfo.put("iLvl", user.properties.get(Users.LEVEL));
      friendInfo.put("ID", user.properties.get(Users.DATABASE_ID));
      friendInfo.put("sName", user.properties.get(Users.USERNAME));
      friendInfo.put("sServer", "Offline");
      updateFriend.put("friend", friendInfo);
      QueryResult result = this.world.db.jdbc.query("SELECT username FROM users LEFT JOIN users_friends ON FriendID = id WHERE UserID = ?", new Object[]{user.properties.get(Users.DATABASE_ID)});

      while (result.next()) {
         User client = this.world.zone.getUserByName(result.getString("Name").toLowerCase());
         if (client != null) {
            this.world.send(updateFriend, client);
            this.world.send(new String[]{"server", user.getName() + " has logged out."}, client);
         }
      }

      result.close();
   }

   public JSONObject getUserData(int id, boolean self) {
      JSONObject userData = new JSONObject();
      User user = this.helper.getUserById(id);
      if (user != null) {
         int hairId = ((Integer) user.properties.get(Users.HAIR_ID)).intValue();
         Hair hair = (Hair) this.world.hairs.get(Integer.valueOf(hairId));
         String lastArea = (String) user.properties.get(Users.LAST_AREA);
         lastArea = lastArea.split("\\|")[0];
         userData.put("eqp", user.properties.get(Users.EQUIPMENT));
         userData.put("iCP", (Integer) user.properties.get(Users.CLASS_POINTS));
         userData.put("iUpgDays", (Integer) user.properties.get(Users.UPGRADE_DAYS));
         userData.put("intAccessLevel", (Integer) user.properties.get(Users.ACCESS));
         userData.put("intColorAccessory", (Integer) user.properties.get(Users.COLOR_ACCESSORY));
         userData.put("intColorBase", (Integer) user.properties.get(Users.COLOR_BASE));
         userData.put("intColorEye", (Integer) user.properties.get(Users.COLOR_EYE));
         userData.put("intColorHair", (Integer) user.properties.get(Users.COLOR_HAIR));
         userData.put("intColorSkin", (Integer) user.properties.get(Users.COLOR_SKIN));
         userData.put("intColorTrim", (Integer) user.properties.get(Users.COLOR_TRIM));
         userData.put("intLevel", (Integer) user.properties.get(Users.LEVEL));
         userData.put("intRebirth", (Integer) user.properties.get(Users.REBIRTH_COUNT));
         userData.put("strElement", user.properties.get("none"));
         userData.put("strClassName", user.properties.get(Users.CLASS_NAME));
         userData.put("strGender", user.properties.get(Users.GENDER));
         userData.put("strHairFilename", hair.getFile());
         userData.put("strHairName", hair.getName());
         userData.put("strUsername", user.properties.get(Users.USERNAME));
         userData.put("iFounder", user.properties.get(Users.FOUNDER));

         if (((Integer) user.properties.get(Users.GUILD_ID)).intValue() > 0) {
            JSONObject result = (JSONObject) user.properties.get(Users.GUILD);
            JSONObject guild = new JSONObject();
            guild.put("id", user.properties.get(Users.GUILD_ID));
            guild.put("Name", result.get("Name"));
            guild.put("Color", result.get("Color"));
            guild.put("MOTD", result.get("MOTD"));
            userData.put("guild", guild);
            userData.put("guildRank", user.properties.get(Users.GUILD_RANK));
         }

         JSONObject titleObj = new JSONObject();
         Title title = (Integer) user.properties.get(Users.TITLE) >= 0 ? world.titles.get(user.properties.get(Users.TITLE)) : null;
         if (title != null) {
            titleObj.put("id", title.getId());
            titleObj.put("Name", title.getName());
            titleObj.put("Color", title.getColor());
         }
         userData.put("title", title);

         if (self) {
            QueryResult result1 = this.world.db.jdbc.query("SELECT HouseInfo, ActivationFlag, Gold, Coins, Exp, Country, Email, DateCreated, UpgradeExpire, Age, Upgraded FROM users WHERE id = ?", new Object[]{user.properties.get(Users.DATABASE_ID)});
            if (result1.next()) {
               userData.put("CharID", (Integer) user.properties.get(Users.DATABASE_ID));
               userData.put("HairID", Integer.valueOf(hairId));
               userData.put("UserID", Integer.valueOf(user.getUserId()));
               userData.put("bPermaMute", user.properties.get(Users.PERMAMUTE_FLAG));
               userData.put("bitSuccess", "1");
               userData.put("dCreated", (new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss")).format(result1.getDate("DateCreated")));
               userData.put("dUpgExp", (new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss")).format(result1.getDate("UpgradeExpire")));
               userData.put("iAge", result1.getString("Age"));
               userData.put("iBagSlots", user.properties.get(Users.SLOTS_BAG));
               userData.put("iBankSlots", user.properties.get(Users.SLOTS_BANK));
               userData.put("iBoostCP", Integer.valueOf(0));
               userData.put("iBoostG", Integer.valueOf(0));
               userData.put("iBoostRep", Integer.valueOf(0));
               userData.put("iBoostXP", Integer.valueOf(0));
               userData.put("iDBCP", user.properties.get(Users.CLASS_POINTS));
               userData.put("iDEX", Integer.valueOf(0));
               userData.put("iDailyAdCap", Integer.valueOf(6));
               userData.put("iDailyAds", Integer.valueOf(0));
               userData.put("iEND", Integer.valueOf(0));
               //userData.put("iFounder", Integer.valueOf(0));
               userData.put("iHouseSlots", (Integer) user.properties.get(Users.SLOTS_HOUSE));
               userData.put("iINT", Integer.valueOf(0));
               userData.put("iLCK", Integer.valueOf(0));
               userData.put("iSTR", Integer.valueOf(0));
               userData.put("iUpg", Integer.valueOf(result1.getInt("Upgraded")));
               userData.put("iWIS", Integer.valueOf(0));
               userData.put("ia0", user.properties.get(Users.ACHIEVEMENT));
               userData.put("ia1", user.properties.get(Users.SETTINGS));
               userData.put("id0", user.properties.get(Users.QUEST_DAILY_0));
               userData.put("id1", user.properties.get(Users.QUEST_DAILY_1));
               userData.put("id2", user.properties.get(Users.QUEST_DAILY_2));
               userData.put("im0", user.properties.get(Users.QUEST_MONTHLY_0));
               userData.put("intActivationFlag", Integer.valueOf(result1.getInt("ActivationFlag")));
               userData.put("intCoins", Integer.valueOf(result1.getInt("Coins")));
               userData.put("intDBExp", Integer.valueOf(result1.getInt("Exp")));
               userData.put("intDBGold", Integer.valueOf(result1.getInt("Gold")));
               userData.put("intExp", Integer.valueOf(result1.getInt("Exp")));
               userData.put("intExpToLevel", Integer.valueOf(this.world.getExpToLevel(((Integer) user.properties.get(Users.LEVEL)).intValue())));
               userData.put("intGold", user.properties.get(Users.GOLD));
               userData.put("intHP", user.properties.get(Users.HP));
               userData.put("intHPMax", user.properties.get(Users.HP_MAX));
               userData.put("intHits", Integer.valueOf(1267));
               userData.put("intMP", user.properties.get(Users.MP));
               userData.put("intMPMax", user.properties.get(Users.MP_MAX));
               userData.put("ip0", Integer.valueOf(0));
               userData.put("ip1", Integer.valueOf(0));
               userData.put("ip2", Integer.valueOf(0));
               userData.put("iq0", Integer.valueOf(0));
               userData.put("lastArea", lastArea);
               userData.put("sCountry", result1.getString("Country"));
               userData.put("sHouseInfo", result1.getString("HouseInfo"));
               userData.put("strEmail", result1.getString("Email"));
               userData.put("strMapName", this.zone.getRoom(user.getRoom()).getName().split("-")[0]);

               userData.put("strQuests", user.properties.get("quests1"));
               userData.put("strQuests2", user.properties.get("quests2"));

//               userData.put("strQuests", user.properties.get(Users.QUESTS_1));
//               userData.put("strQuests2", user.properties.get(Users.QUESTS_2));
//               userData.put("strQuests3", user.properties.get(Users.QUESTS_3));
//               userData.put("strQuests4", user.properties.get(Users.QUESTS_4));
//               userData.put("strQuests5", user.properties.get(Users.QUESTS_5));
            }

            result1.close();
         }
      }

      return userData;
   }

   public void respawn(User user) {
      user.properties.put(Users.HP, user.properties.get(Users.HP_MAX));
      user.properties.put(Users.MP, user.properties.get(Users.MP_MAX));
      user.properties.put(Users.STATE, Integer.valueOf(1));
      this.clearAuras(user);
      this.sendUotls(user, true, false, true, false, false, true);
   }

   public void kick(User user) {
      user.isBeingKicked = true;
      this.world.send(new String[]{"logoutWarning", "", "65"}, user);
      this.world.scheduleTask(new KickUser(user, this.world), 0L, TimeUnit.SECONDS);
   }

   public void die(User user) {
      user.properties.put(Users.HP, Integer.valueOf(0));
      user.properties.put(Users.MP, Integer.valueOf(0));
      user.properties.put(Users.STATE, Integer.valueOf(0));
      user.properties.put(Users.RESPAWN_TIME, Long.valueOf(System.currentTimeMillis()));
   }

   public void addOfferItem(User user, int itemId, int quantity, int enhId) {
      Map<Integer, Integer> offers = (Map<Integer, Integer>) user.properties.get(Users.ITEM_OFFER);
      Map<Integer, Integer> enhances = (Map<Integer, Integer>) user.properties.get(Users.ITEM_OFFER_ENHANCEMENT);

      if (offers.containsKey(itemId)) {
         int deltaQuantity = (Integer) offers.get(itemId) + quantity;
         offers.put(itemId, deltaQuantity);
      } else
         offers.put(itemId, quantity);
      enhances.put(itemId, enhId);
   }

   public void removeOfferItem(User user, int itemId, int quantity) {
      Map<Integer, Integer> tempInventory = (Map<Integer, Integer>) user.properties.get(Users.ITEM_OFFER);
      Map<Integer, Integer> enhances = (Map<Integer, Integer>) user.properties.get(Users.ITEM_OFFER_ENHANCEMENT);

      if (tempInventory.containsKey(itemId)) {
         int deltaQuantity = (Integer) tempInventory.get(itemId) - quantity;
         if (deltaQuantity < 1) {
            tempInventory.remove(itemId);
            enhances.remove(itemId);
         } else
            tempInventory.put(itemId, deltaQuantity);
      }
   }
}