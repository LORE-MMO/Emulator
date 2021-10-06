package nereus.world;

import nereus.config.ConfigData;
import nereus.db.Database;
import nereus.db.objects.*;
import nereus.db.objects.Class;
import nereus.tasks.ACGiveaway;
import nereus.tasks.FreeSFSPool;
import nereus.tasks.WarzoneQueue;
import nereus.world.stats.Stats;
import nereus.discord.Bot;
import com.google.common.collect.ArrayListMultimap;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import jdbchelper.BeanCreator;
import jdbchelper.QueryResult;
import jdbchelper.ResultSetMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.util.AbstractMap.SimpleEntry;

public class World {
    private static final ResultSetMapper<String, Double> coreValuesMapper = new ResultSetMapper() {
        public SimpleEntry<String, Double> mapRow(ResultSet rs) throws SQLException {
            return new SimpleEntry(rs.getString("name"), Double.valueOf(rs.getDouble("value")));
        }
    };
    private static final ResultSetMapper<Integer, String> factionsMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, String> mapRow(ResultSet rs) throws SQLException {
            return new SimpleEntry(Integer.valueOf(rs.getInt("id")), rs.getString("Name"));
        }
    };
    private static final ResultSetMapper<String, Integer> chatFiltersMapper = new ResultSetMapper() {
        public SimpleEntry<String, Integer> mapRow(ResultSet rs) throws SQLException {
            return new SimpleEntry(rs.getString("Swear"), Integer.valueOf(rs.getInt("TimeToMute")));
        }
    };
    private static final ResultSetMapper<Integer, Integer> itemSkillsMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new SimpleEntry(Integer.valueOf(rs.getInt("ItemID")), Integer.valueOf(rs.getInt("SkillID")));
        }
    };
    private static final ResultSetMapper<Integer, Double> wheelsMapper = new ResultSetMapper() {
        public SimpleEntry<Integer, Double> mapRow(ResultSet rs) throws SQLException {
            return new SimpleEntry(Integer.valueOf(rs.getInt("ItemID")), Double.valueOf(rs.getDouble("Chance")));
        }
    };
    private static final BeanCreator<String> newsCreator = new BeanCreator() {
        public String createBean(ResultSet rs) throws SQLException {
            StringBuilder sb = new StringBuilder();
            sb.append(rs.getString("name"));
            sb.append("=");
            sb.append(rs.getString("value"));

            while (rs.next()) {
                sb.append(",");
                sb.append(rs.getString("name"));
                sb.append("=");
                sb.append(rs.getString("value"));
            }

            return sb.toString();
        }
    };
    public HashMap<String, Area> areas;
    public HashMap<Integer, Item> items;
    public HashMap<Integer, Shop> shops;
    public HashMap<Integer, Hair> hairs;
    public HashMap<Integer, Skill> skills;
    public HashMap<Integer, Enhancement> enhancements;
    public HashMap<Integer, EnhancementPattern> patterns;
    public HashMap<Integer, Monster> monsters;
    public HashMap<Integer, Aura> auras;
    public HashMap<Integer, AuraEffects> effects;
    public HashMap<Integer, Hairshop> hairshops;
    public HashMap<Integer, Quest> quests;
    public HashMap<Integer, String> factions;
    public HashMap<Integer, Double> wheels;
    public HashMap<String, Double> coreValues;
    public HashMap<String, Integer> chatFilters;
    public HashMap<Integer, Integer> specialskills;
    public HashMap<Integer, Title> titles;
    public Database db;
    public Users users;
    public Rooms rooms;
    public Parties parties;
    public WarzoneQueue warzoneQueue;
    public Zone zone;
    public String messageOfTheDay;
    public String newsString;
    public int EXP_RATE = 1;
    public int CP_RATE = 1;
    public int GOLD_RATE = 1;
    public int REP_RATE = 1;
    public int DROP_RATE = 1;
    private AbstractExtension ext;
    private ScheduledExecutorService tasks;
    public Bot bot;
    public static final Random RANDOM = new Random();

    public World(AbstractExtension ext, Zone zone) {
        super();
        this.ext = ext;
        this.zone = zone;
        this.db = new Database(ConfigData.DB_MAX_CONNECTIONS);
        this.rooms = new Rooms(zone, this);
        this.users = new Users(zone, this);
        this.parties = new Parties();
        this.tasks = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.warzoneQueue = new WarzoneQueue(this);
        this.tasks.scheduleAtFixedRate(this.warzoneQueue, 5L, 5L, TimeUnit.SECONDS);
        this.tasks.scheduleAtFixedRate(new ACGiveaway(this), 30L, 30L, TimeUnit.MINUTES);
        this.tasks.scheduleAtFixedRate(new FreeSFSPool(), 30L, 30L, TimeUnit.MINUTES);
        this.retrieveDatabaseObject("all");
        this.bot = new Bot(ConfigData.DISCORD_BOT_TOKEN, this);
        SmartFoxServer.log.info("World initialized.");
    }

    public void shutdown() {
        this.db.jdbc.run("UPDATE users SET CurrentServer = \'Offline\'", new Object[0]);
    }

    public void destroy() {
        this.coreValues = null;
        this.factions = null;
        this.hairshops = null;
        this.effects = null;
        this.auras = null;
        this.monsters = null;
        this.enhancements = null;
        this.patterns = null;
        this.skills = null;
        this.hairs = null;
        this.areas = null;
        this.shops = null;
        this.items = null;
        this.tasks.shutdown();
        this.tasks = null;
        this.rooms = null;
        this.users = null;
        this.parties = null;
        this.db.destroy();
        this.db = null;
        this.zone = null;
        this.ext = null;
        this.bot.api.disconnect();
        SmartFoxServer.log.info("World destroyed.");
    }

    public String clearHTMLTags(String text) {
        return text.replaceAll("\\<.*?\\>", "");
    }

    public final boolean retrieveDatabaseObject(String type) {
        return retrieveDatabaseObject(type, null);
    }

    public final boolean retrieveDatabaseObject(String type, User user) {
        switch(type.toLowerCase()){
            case "item":
                HashMap<Integer, Item> itemsData = new HashMap<Integer, Item>(this.db.getJdbc().queryForMap("SELECT * FROM items", Item.resultSetMapper));

                for (Item item : itemsData.values()) {
                    Map<Integer, Integer> requirements = new HashMap<Integer, Integer>(this.db.getJdbc().queryForMap("SELECT * FROM items_requirements WHERE ItemID = ?", Item.requirementMapper, item.getId()));
                    item.requirements = requirements;

                    if (item.getEquipment().equals("ar") && !item.getType().equals("Enhancement")) {
                        Class reward = this.db.jdbc.queryForObject("SELECT * FROM classes WHERE ItemID = ?", Class.beanCreator, new Object[]{Integer.valueOf(item.getId())});
                        if (reward == null) {
                            if (user != null)
                                this.send(new String[]{"server", "An item with the equipment type \'Class\' does not have a matching id in the classes table. ItemID: " + item.getId()}, user);
                            throw new NullPointerException("An item with the equipment type \'Class\' does not have a matching id in the classes table. ItemID: " + item.getId());
                        }

                        reward.skills = this.db.jdbc.queryForObject("SELECT id FROM skills WHERE ItemID = ?", Class.beanSkills, new Object[]{Integer.valueOf(item.getId())});
                        if (reward.skills == null) {
                            if (user != null)
                                this.send(new String[]{"server", "A class contains an empty skill set, please delete this item first. ItemID: " + item.getId()}, user);
                            throw new NullPointerException("A class contains an empty skill set, please delete this item first. ItemID: " + item.getId());
                        }

                        item.classObj = reward;
                    }
                }

                this.items = itemsData;

                HashMap<Integer, Skill> skillsData = new HashMap<Integer, Skill>(this.db.getJdbc().queryForMap("SELECT * FROM skills WHERE id > 0", Skill.resultSetMapper));
                this.skills = skillsData;

                Skill skill;
                HashMap skillAurasData;
                for(Iterator var20 = skillsData.values().iterator(); var20.hasNext(); skill.auras = skillAurasData) {
                    skill = (Skill)var20.next();
                    skillAurasData = new HashMap(this.db.getJdbc().queryForMap("SELECT * FROM skills_auras WHERE SkillID = ?", Skill.auraMapper, new Object[]{skill.getId()}));
                    this.auras = skillAurasData;

                    Iterator auraEffects = skillAurasData.values().iterator();
                    while (auraEffects.hasNext()) {
                        Aura ae = (Aura) auraEffects.next();
                        ae.effects = this.db.jdbc.queryForObject("SELECT * FROM skills_auras_effects WHERE AuraID = ?", Aura.beanEffects, new Object[]{Integer.valueOf(ae.getId())});
                        if (ae.effects == null) {
                            ae.effects = Collections.EMPTY_SET;
                        }
                    }
                }

                HashMap<Integer, Aura> aurasData = new HashMap<Integer, Aura>(this.db.getJdbc().queryForMap("SELECT * FROM auras WHERE id > 0", Aura.resultSetMapper));
                this.auras = aurasData;

                HashMap<Integer, AuraEffects> effectsData = new HashMap<Integer, AuraEffects>(this.db.jdbc.queryForMap("SELECT * FROM skills_auras_effects", AuraEffects.resultSetMapper, new Object[0]));
                this.effects = effectsData;

                HashMap<Integer, Hair> hairsData = new HashMap<Integer, Hair>(this.db.getJdbc().queryForMap("SELECT * FROM hairs", Hair.resultSetMapper));
                this.hairs = hairsData;

                HashMap<Integer, String> factionsData = new HashMap<Integer, String>(this.db.getJdbc().queryForMap("SELECT * FROM factions", World.factionsMapper));
                this.factions = factionsData;

                HashMap wheelsData = new HashMap<Integer, Double>(this.db.getJdbc().queryForMap("SELECT * FROM wheels_rewards", wheelsMapper));
                this.wheels = wheelsData;

                HashMap<Integer, Integer> specialsData = new HashMap<Integer, Integer>(this.db.getJdbc().queryForMap("SELECT * FROM items_skills", itemSkillsMapper));
                this.specialskills = specialsData;
                SmartFoxServer.log.info("Item objects retrieved.");

                HashMap<Integer, Enhancement> enhancementsData = new HashMap<Integer, Enhancement>(this.db.getJdbc().queryForMap("SELECT * FROM enhancements", Enhancement.resultSetMapper));
                this.enhancements = enhancementsData;

                HashMap<Integer, EnhancementPattern> patternsData = new HashMap<Integer, EnhancementPattern>(this.db.getJdbc().queryForMap("SELECT * FROM enhancements_patterns WHERE id > 0", EnhancementPattern.resultSetMapper));
                this.patterns = patternsData;

                SmartFoxServer.log.info("Enhancements objects retrieved.");
                break;
            case "map":
                HashMap<String, Area> areasData = new HashMap<String, Area>(this.db.getJdbc().queryForMap("SELECT * FROM maps", Area.resultSetMapper));

                for (Area area : areasData.values()) {
                    area.monsters = this.db.getJdbc().queryForObject("SELECT * FROM maps_monsters WHERE MapID = ?", MapMonster.setCreator, area.getId());
                    if (area.monsters == null)
                        area.monsters = Collections.EMPTY_SET;
                    area.items = this.db.getJdbc().queryForObject("SELECT * FROM maps_items WHERE MapID = ?", Area.beanItems, area.getId());
                    if (area.items == null)
                        area.items = Collections.EMPTY_SET;
                    area.cells = this.db.getJdbc().queryForMap("SELECT * FROM maps_cells WHERE MapID = ?", Cell.resultSetMapper, area.getId());
                }

                if (this.areas != null) {
                    HashMap<String, Area> oldAreas = new HashMap<String, Area>(this.areas);

                    for (Iterator<Map.Entry<String, Area>> it = oldAreas.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<String, Area> entry = it.next();
                        if (!entry.getKey().contains("house-")) it.remove();
                    }

                    areasData.putAll(oldAreas);
                }

                this.areas = areasData;

                HashMap<Integer, Monster> monstersData = new HashMap<Integer, Monster>(this.db.getJdbc().queryForMap("SELECT * FROM monsters", Monster.resultSetMapper));

                for (Monster monster : monstersData.values()) {
                    monster.drops = this.db.getJdbc().queryForObject("SELECT * FROM monsters_drops WHERE MonsterID = ?", Monster.beanDrops, monster.getId());

                    if (monster.drops == null)
                        monster.drops = Collections.EMPTY_SET;

                    monster.skills = this.db.getJdbc().queryForObject("SELECT * FROM monsters_skills WHERE MonsterID = ?", Monster.beanSkills, monster.getId());

                    if (monster.skills == null)
                        monster.skills = Collections.EMPTY_SET;
                }

                this.monsters = monstersData;
                SmartFoxServer.log.info("Map objects retrieved.");
                break;
            case "quest":
                HashMap<Integer, Quest> questsData = new HashMap<Integer, Quest>(this.db.getJdbc().queryForMap("SELECT * FROM quests", Quest.resultSetMapper));

                for (Quest quest : questsData.values()) {
                    quest.reqd = this.db.getJdbc().queryForMap("SELECT * FROM quests_reqditems WHERE QuestID = ?", Quest.requirementsNormalRewardsMapper, quest.getId());
                    quest.rewards = ArrayListMultimap.create();

                    QueryResult rewards = this.db.getJdbc().query("SELECT * FROM quests_rewards WHERE QuestID = ?", quest.getId());
                    while (rewards.next()) {
                        QuestReward questReward = new QuestReward();
                        questReward.itemId = rewards.getInt("ItemID");
                        questReward.quantity = rewards.getInt("Quantity");
                        questReward.rate = rewards.getDouble("Rate");
                        questReward.type = rewards.getString("RewardType");
                        quest.rewards.put(rewards.getInt("ItemID"), questReward);
                    }
                    rewards.close();

                    quest.requirements = this.db.getJdbc().queryForMap("SELECT * FROM quests_requirements WHERE QuestID = ?", Quest.requirementsNormalRewardsMapper, quest.getId());
                    quest.locations = this.db.getJdbc().queryForObject("SELECT * FROM quests_locations WHERE QuestID = ?", Quest.beanLocations, quest.getId());

                    if (quest.locations == null) quest.locations = Collections.EMPTY_SET;
                }

                this.quests = questsData;
                SmartFoxServer.log.info("Quest objects retrieved.");
                break;
            case "shop":
                HashMap<Integer, Shop> shopsData = new HashMap<Integer, Shop>(this.db.getJdbc().queryForMap("SELECT * FROM shops", Shop.resultSetMapper));

                for (Shop shop : shopsData.values()) {
                    shop.items = this.db.getJdbc().queryForMap("SELECT id, ItemID FROM shops_items WHERE ShopID = ?", Shop.shopItemsMapper, shop.getId());

                    shop.locations = this.db.getJdbc().queryForObject("SELECT * FROM shops_locations WHERE ShopID = ?", Shop.beanLocations, shop.getId());

                    if (shop.locations == null)
                        shop.locations = Collections.EMPTY_SET;
                }

                this.shops = shopsData;

                HashMap<Integer, Hairshop> hairshopsData = new HashMap<Integer, Hairshop>(this.db.getJdbc().queryForMap("SELECT * FROM hairs_shops", Hairshop.resultSetMapper));

                for (Hairshop hairshop : hairshopsData.values()) {
                    hairshop.male = this.db.getJdbc().queryForObject("SELECT * FROM hairs_shops_items WHERE Gender = ? AND ShopID = ?", Hairshop.beanHairshopItems, "M", hairshop.getId());
                    hairshop.female = this.db.getJdbc().queryForObject("SELECT * FROM hairs_shops_items WHERE Gender = ? AND ShopID = ?", Hairshop.beanHairshopItems, "F", hairshop.getId());
                }

                this.hairshops = hairshopsData;
                SmartFoxServer.log.info("Shop objects retrieved.");
                break;
            case "title":
                HashMap<Integer, Title> titleData = new HashMap<Integer, Title>(this.db.getJdbc().queryForMap("SELECT * FROM titles", Title.resultSetMapper));
                this.titles = titleData;
                SmartFoxServer.log.info("Title objects retrieved.");
                break;
            case "settings":
                this.messageOfTheDay = this.db.getJdbc().queryForString("SELECT MOTD FROM servers WHERE Name = ?", ConfigData.SERVER_NAME);
                this.newsString = this.db.getJdbc().queryForObject("SELECT * FROM settings", World.newsCreator);

                HashMap<String, Double> coreValuesData = new HashMap<String, Double>(this.db.getJdbc().queryForMap("SELECT * FROM settings_rates", World.coreValuesMapper));
                this.coreValues = coreValuesData;

                HashMap<String, Integer> chatFiltersData = new HashMap<String, Integer>(this.db.getJdbc().queryForMap("SELECT * FROM settings_filters", chatFiltersMapper));
                this.chatFilters = chatFiltersData;
                SmartFoxServer.log.info("Server settings retrieved.");
                break;
            case "all":
                this.retrieveDatabaseObject("item");
                this.retrieveDatabaseObject("map");
                this.retrieveDatabaseObject("quest");
                this.retrieveDatabaseObject("shop");
                this.retrieveDatabaseObject("title");
                this.retrieveDatabaseObject("settings");
                break;
        }

        return true;
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit tu) {
        return this.scheduleTask(task, delay, tu, false);
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit tu, boolean repeat) {
        return repeat ? this.tasks.scheduleAtFixedRate(task, delay, delay, tu) : this.tasks.schedule(task, delay, tu);
    }

    public int roundTens(int val) {
        int x = val;

        for (int i = 0; i < 9 && x % 10 != 0; ++i) {
            ++x;
        }

        return x;
    }

    public int getExpToLevel(int playerLevel) {
        return playerLevel < ((Double) this.coreValues.get("intLevelMax")).intValue() ? this.roundTens(this.getBaseValueByLevel(1000, 850000, 1.66D, playerLevel).intValue()) : 200000000;
    }

    public int getGuildExpToLevel(int guildLevel) {
        return guildLevel < 50 ? this.roundTens(this.getBaseValueByGuildLevel(50, '\ua604', 1.66D, guildLevel).intValue()) : 200000000;
    }

    public Double getBaseValueByGuildLevel(int base, int delta, double curve, int guildLevel) {
        byte levelCap = 50;
        int level = guildLevel < 1 ? 1 : (guildLevel > levelCap ? levelCap : guildLevel);
        double x = (double) (level - 1) / (double) (levelCap - 1);
        return Double.valueOf((double) base + Math.pow(x, curve) * (double) delta);
    }

    public int getManaByLevel(int level) {
        int base = ((Double) this.coreValues.get("PCmpBase1")).intValue();
        int delta = ((Double) this.coreValues.get("PCmpBase100")).intValue();
        double curve = ((Double) this.coreValues.get("curveExponent")).doubleValue() + (double) base / (double) delta;
        return this.getBaseValueByLevel(base, delta, curve, level).intValue();
    }

    public int getHealthByLevel(int level) {
        int base = ((Double) this.coreValues.get("PChpGoal1")).intValue();
        int delta = ((Double) this.coreValues.get("PChpGoal100")).intValue();
        double curve = 1.5D + (double) base / (double) delta;
        return this.getBaseValueByLevel(base, delta, curve, level).intValue();
    }

    public int getBaseHPByLevel(int level) {
        int base = ((Double) this.coreValues.get("PChpBase1")).intValue();
        double curve = ((Double) this.coreValues.get("curveExponent")).doubleValue();
        int delta = ((Double) this.coreValues.get("PChpDelta")).intValue();
        return this.getBaseValueByLevel(base, delta, curve, level).intValue();
    }

    public int getIBudget(int itemLevel, int iRty) {
        int GstBase = ((Double) this.coreValues.get("GstBase")).intValue();
        int GstGoal = ((Double) this.coreValues.get("GstGoal")).intValue();
        double statsExponent = ((Double) this.coreValues.get("statsExponent")).doubleValue();
        int rarity = iRty < 1 ? 1 : iRty;
        int level = itemLevel + rarity - 1;
        int delta = GstGoal - GstBase;
        return this.getBaseValueByLevel(GstBase, delta, statsExponent, level).intValue();
    }

    public int getInnateStats(int userLevel) {
        int PCstBase = ((Double) this.coreValues.get("PCstBase")).intValue();
        int PCstGoal = ((Double) this.coreValues.get("PCstGoal")).intValue();
        double statsExponent = ((Double) this.coreValues.get("statsExponent")).doubleValue();
        int delta = PCstGoal - PCstBase;
        return this.getBaseValueByLevel(PCstBase, delta, statsExponent, userLevel).intValue();
    }

    public Double getBaseValueByLevel(int base, int delta, double curve, int userLevel) {
        int levelCap = ((Double) this.coreValues.get("intLevelCap")).intValue();
        int level = userLevel < 1 ? 1 : (userLevel > levelCap ? levelCap : userLevel);
        double x = (double) (level - 1) / (double) (levelCap - 1);
        return Double.valueOf((double) base + Math.pow(x, curve) * (double) delta);
    }

    public Map<String, Double> getItemStats(Enhancement enhancement, String equipment) {
        LinkedHashMap itemStats = new LinkedHashMap();
        itemStats.put("END", Double.valueOf(0.0D));
        itemStats.put("STR", Double.valueOf(0.0D));
        itemStats.put("INT", Double.valueOf(0.0D));
        itemStats.put("DEX", Double.valueOf(0.0D));
        itemStats.put("WIS", Double.valueOf(0.0D));
        itemStats.put("LCK", Double.valueOf(0.0D));
        if (enhancement != null) {
            int patternId = enhancement.getPatternId();
            int rarity = enhancement.getRarity();
            int level = enhancement.getLevel();
            int iBudget = (int) Math.round((double) this.getIBudget(level, rarity) * ((Double) Stats.ratioByEquipment.get(equipment)).doubleValue());
            Map statPattern = ((EnhancementPattern) this.patterns.get(Integer.valueOf(patternId))).getStats();
            Set keyEntry = itemStats.keySet();
            double valTotal = 0.0D;

            double key;
            for (Iterator keyArray = keyEntry.iterator(); keyArray.hasNext(); valTotal += key) {
                String i = (String) keyArray.next();
                key = (double) (iBudget * ((Integer) statPattern.get(i)).intValue() / 100);
                itemStats.put(i, Double.valueOf(key));
            }

            Object[] var17 = keyEntry.toArray();
            int var18 = 0;

            while (valTotal < (double) iBudget) {
                String var19 = (String) var17[var18];
                double statVal = ((Double) itemStats.get(var19)).doubleValue() + 1.0D;
                itemStats.put(var19, Double.valueOf(statVal));
                ++valTotal;
                ++var18;
                if (var18 > var17.length - 1) {
                    var18 = 0;
                }
            }
        }

        return itemStats;
    }

    public void applyFloodFilter(User user, String message) {
        long lastMsgTime = ((Long) user.properties.get("lastmessagetime")).longValue() + ConfigData.ANTI_MESSAGEFLOOD_MIN_MSG_TIME;
        if (lastMsgTime >= System.currentTimeMillis()) {
            ++user.floodCounter;
            if (user.floodCounter >= ConfigData.ANTI_MESSAGEFLOOD_TOLERANCE) {
                ++user.floodWarningsCounter;
                user.floodCounter = 0;
                this.send(new String[]{"warning", "Please do not flood the server with messages."}, user);
            }
        } else {
            user.floodCounter = 0;
        }

        if (message.equals(user.lastMessage)) {
            ++user.repeatedMsgCounter;
            if (user.repeatedMsgCounter >= ConfigData.ANTI_MESSAGEFLOOD_MAX_REPEATED) {
                ++user.floodWarningsCounter;
                user.repeatedMsgCounter = 0;
                this.send(new String[]{"warning", "Please do not flood the server with messages."}, user);
            }
        } else {
            user.repeatedMsgCounter = 0;
            user.lastMessage = message;
        }

        if (user.floodWarningsCounter >= ConfigData.ANTI_MESSAGEFLOOD_WARNINGS) {
            this.users.mute(user, 2, 12);
            user.floodWarningsCounter = 0;
        }

        user.properties.put("lastmessagetime", Long.valueOf(System.currentTimeMillis()));
    }

    public void sendServerMessage(String message) {
        JSONObject umsg = new JSONObject();
        umsg.put("cmd", "umsg");
        umsg.put("s", message);
        this.send(umsg, this.zone.getChannelList());
    }

    public void sendToUsers(JSONObject params) {
        this.ext.sendResponse(params, -1, (User) null, this.zone.getChannelList());
    }

    public void sendToUsers(String[] params) {
        this.ext.sendResponse(params, -1, (User) null, this.zone.getChannelList());
    }

    public void send(JSONObject params, LinkedList<SocketChannel> channels) {
        this.ext.sendResponse(params, -1, (User) null, channels);
    }

    public void send(JSONObject params, User user) {
        if (user != null && params != null) {
            LinkedList channels = new LinkedList();
            channels.add(user.getChannel());
            this.ext.sendResponse(params, -1, user, channels);
        }
    }

    public void send(String[] params, SocketChannel chan) {
        if (chan != null && params != null) {
            LinkedList channels = new LinkedList();
            channels.add(chan);
            this.ext.sendResponse(params, -1, (User) null, channels);
        }
    }

    public void send(String[] params, User user) {
        if (user != null && params != null) {
            LinkedList channels = new LinkedList();
            channels.add(user.getChannel());
            this.ext.sendResponse(params, -1, user, channels);
        }
    }

    public void send(String[] params, LinkedList<SocketChannel> channels) {
        this.ext.sendResponse(params, -1, (User) null, channels);
    }

    public void sendToRoom(JSONObject params, User user, Room room) {
        if (user != null && room != null) {
            if (user.getRoom() == room.getId()) {
                this.ext.sendResponse(params, -1, (User) null, room.getChannellList());
            } else {
                this.users.kick(user);
            }

        }
    }

    public void sendToRoom(String[] params, User user, Room room) {
        if (user != null && room != null) {
            if (user.getRoom() == room.getId()) {
                this.ext.sendResponse(params, -1, (User) null, room.getChannellList());
            } else {
                this.users.kick(user);
            }

        }
    }

    public void sendToRoomButOne(JSONObject o, User _user, Room room) {
        if (_user != null && room != null) {
            User[] _users = room.getAllUsersButOne(_user);
            LinkedList channels = new LinkedList();
            User[] arr$ = _users;
            int len$ = _users.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                User user = arr$[i$];
                if (user != null) {
                    channels.add(user.getChannel());
                }
            }

            this.send(o, channels);
        }
    }

    public void sendToRoomButOne(String[] o, User _user, Room room) {
        if (_user != null && room != null) {
            User[] _users = room.getAllUsersButOne(_user);
            LinkedList channels = new LinkedList();
            User[] arr$ = _users;
            int len$ = _users.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                User user = arr$[i$];
                if (user != null) {
                    channels.add(user.getChannel());
                }
            }

            this.send(o, channels);
        }
    }

    public void sendToGuild(JSONObject params, JSONObject guildObj) {
        JSONArray members = (JSONArray) guildObj.get("ul");
        if (members != null && members.size() > 0) {
            Iterator it = members.iterator();

            while (it.hasNext()) {
                JSONObject member = (JSONObject) it.next();
                User guildMember = this.zone.getUserByName(member.get("userName").toString().toLowerCase());
                if (guildMember != null) {
                    this.send(params, guildMember);
                }
            }
        }

    }

    public void sendToGuild(String[] params, JSONObject guildObj) {
        JSONArray members = (JSONArray) guildObj.get("ul");
        if (members != null && members.size() > 0) {
            Iterator it = members.iterator();

            while (it.hasNext()) {
                JSONObject member = (JSONObject) it.next();
                User guildMember = this.zone.getUserByName(member.get("userName").toString().toLowerCase());
                if (guildMember != null) {
                    this.send(params, guildMember);
                }
            }
        }

    }

    public void sendGuildUpdate(JSONObject guildObj) {
        this.sendGuildUpdateButOne((User) null, guildObj);
    }

    public void sendGuildUpdateButOne(User user, JSONObject guildObj) {
        JSONObject updateGuild = new JSONObject();
        JSONArray members = (JSONArray) guildObj.get("ul");
        if (members != null && members.size() > 0) {
            Iterator it = members.iterator();

            while (true) {
                User guildMember;
                do {
                    do {
                        if (!it.hasNext()) {
                            return;
                        }

                        JSONObject member = (JSONObject) it.next();
                        guildMember = this.zone.getUserByName(member.get("userName").toString().toLowerCase());
                    } while (guildMember == null);
                } while (user != null && guildMember.equals(user));

                guildMember.properties.put("guildobj", guildObj);
                updateGuild.put("cmd", "updateGuild");
                updateGuild.put("guild", guildObj);
                this.send(updateGuild, guildMember);
            }
        }
    }
}