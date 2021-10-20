package nereus.config;

import it.gotoandplay.smartfoxserver.SmartFoxServer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ConfigData {
   public static String DB_HOST;
   public static String DB_NAME;
   public static String DB_USERNAME;
   public static String DB_PASSWORD;
   public static int DB_PORT;
   public static int DB_MAX_CONNECTIONS;

   public static String SERVER_NAME;
   public static boolean SERVER_GUI;
   public static String SERVER_PROFILE_LINK;
   public static String SERVER_GAME_LINK;

   public static boolean STAFF_ONLY;
   public static long ANTI_MESSAGEFLOOD_MIN_MSG_TIME;
   public static int ANTI_MESSAGEFLOOD_TOLERANCE;
   public static int ANTI_MESSAGEFLOOD_MAX_REPEATED;
   public static int ANTI_MESSAGEFLOOD_WARNINGS;
   public static long ANTI_REQUESTFLOOD_MIN_MSG_TIME;
   public static int ANTI_REQUESTFLOOD_TOLERANCE;
   public static int ANTI_REQUESTFLOOD_MAX_REPEATED;
   public static int ANTI_REQUESTFLOOD_WARNINGS;
   public static String ANTI_REQUESTFLOOD_BANNEDLIST;
   public static boolean ANTI_REQUESTFLOOD_REPEAT_ENABLED;
   public static Map<String, String> REQUESTS;
   public static Set<String> ANTI_REQUESTFLOOD_GUARDED;

   public static String DISCORD_BOT_AVATAR;
   public static String DISCORD_BOT_TOKEN;
   public static String DISCORD_BOT_PREFIX;

   public static long DISCORD_GENERAL_CHANNELID;
   public static long DISCORD_LOGS_CHANNELID;
   public static long DISCORD_MARKET_CHANNELID;

   public static String DISCORD_ZONE_WEBHOOK;
   public static String DISCORD_ZONE_AVATAR;
   public static String DISCORD_ZONE_NAME;

   public static String DISCORD_WORLD_WEBHOOK;
   public static String DISCORD_WORLD_AVATAR;
   public static String DISCORD_WORLD_NAME;

   public static String DISCORD_PARTY_WEBHOOK;
   public static String DISCORD_PARTY_AVATAR;
   public static String DISCORD_PARTY_NAME;

   public static String DISCORD_GUILD_WEBHOOK;
   public static String DISCORD_GUILD_AVATAR;
   public static String DISCORD_GUILD_NAME;

   public static String DISCORD_PRIVATE_WEBHOOK;
   public static String DISCORD_PRIVATE_AVATAR;
   public static String DISCORD_PRIVATE_NAME;

   public static String DISCORD_SERVER_WEBHOOK;
   public static String DISCORD_SERVER_AVATAR;
   public static String DISCORD_SERVER_NAME;

   private ConfigData() {
      super();
      throw new UnsupportedOperationException("not allowed to have an instance of this class");
   }

   static {
      try {
         String e = System.getProperty("Nereus.config") != null ? System.getProperty("nereus.config") : "nereus.conf";
         Properties config = new Properties();
         String curDir = (new File(".")).getCanonicalPath();
         File dir = new File(curDir + File.separatorChar + "conf" + File.separatorChar);
         if(!dir.exists() && !dir.mkdir()) {
            throw new RuntimeException("Unable to create directory.");
         }

         String filePath = curDir + File.separatorChar + "conf" + File.separatorChar + e;
         File conf = new File(filePath);
         if(!conf.exists() && conf.createNewFile()) {
            BufferedWriter fin = null;

            try {
               fin = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "8859_1"));
               config.setProperty("server.name", "nereus");
               config.setProperty("server.staffonly", "false");
               config.setProperty("database.host", "127.0.0.1");
               config.setProperty("database.port", "3306");
               config.setProperty("database.connections.max", "50");
               config.setProperty("database.user", "root");
               config.setProperty("database.pass", "");
               config.setProperty("database.name", "mextv3");
               config.setProperty("antiflood.message.tolerance", "5");
               config.setProperty("antiflood.message.maxrepeated", "3");
               config.setProperty("antiflood.message.warnings", "2");
               config.setProperty("antiflood.message.minimumtime", "1000");
               config.setProperty("antiflood.request.tolerance", "5");
               config.setProperty("antiflood.request.maxrepeated", "3");
               config.setProperty("antiflood.request.enablerepeatfilter", "false");
               config.setProperty("antiflood.request.warnings", "2");
               config.setProperty("antiflood.request.minimumtime", "1000");
               config.store(fin, "pie Configuration");
            } catch (IOException var32) {
               SmartFoxServer.log.severe("Error in writing configuration: " + var32.getMessage());
            } finally {
               try {
                  if(fin != null) {
                     fin.close();
                  }
               } catch (IOException var30) {
                  SmartFoxServer.log.severe("Error in closing write stream: " + var30.getMessage());
               }

            }
         }

         FileInputStream var35 = null;

         try {
            var35 = new FileInputStream(filePath);
            config.load(var35);
         } finally {
            try {
               if(var35 != null) {
                  var35.close();
               }
            } catch (IOException var29) {
               SmartFoxServer.log.severe("Error in closing input stream: " + var29.getMessage());
            }

         }

         DB_HOST = config.getProperty("database.host");
         DB_USERNAME = config.getProperty("database.user");
         DB_PASSWORD = config.getProperty("database.pass");
         DB_NAME = config.getProperty("database.name");
         DB_PORT = Integer.parseInt(config.getProperty("database.port"));
         DB_MAX_CONNECTIONS = Integer.parseInt(config.getProperty("database.connections.max"));
         SERVER_NAME = config.getProperty("server.name");
         STAFF_ONLY = Boolean.parseBoolean(config.getProperty("server.staffonly"));
         SERVER_GUI = Boolean.parseBoolean(config.getProperty("server.gui"));
         ANTI_MESSAGEFLOOD_MIN_MSG_TIME = Long.parseLong(config.getProperty("antiflood.message.minimumtime"));
         ANTI_MESSAGEFLOOD_TOLERANCE = Integer.parseInt(config.getProperty("antiflood.message.tolerance"));
         ANTI_MESSAGEFLOOD_MAX_REPEATED = Integer.parseInt(config.getProperty("antiflood.message.maxrepeated"));
         ANTI_MESSAGEFLOOD_WARNINGS = Integer.parseInt(config.getProperty("antiflood.message.warnings"));
         ANTI_REQUESTFLOOD_MIN_MSG_TIME = Long.parseLong(config.getProperty("antiflood.request.minimumtime"));
         ANTI_REQUESTFLOOD_TOLERANCE = Integer.parseInt(config.getProperty("antiflood.request.tolerance"));
         ANTI_REQUESTFLOOD_MAX_REPEATED = Integer.parseInt(config.getProperty("antiflood.request.maxrepeated"));
         ANTI_REQUESTFLOOD_WARNINGS = Integer.parseInt(config.getProperty("antiflood.request.warnings"));
         ANTI_REQUESTFLOOD_REPEAT_ENABLED = Boolean.parseBoolean(config.getProperty("antiflood.request.enablerepeatfilter"));
         ANTI_REQUESTFLOOD_BANNEDLIST = config.getProperty("antiflood.request.ipbanlist");

         DISCORD_GENERAL_CHANNELID = Long.parseLong(config.getProperty("discord.general.channelid"));
         DISCORD_LOGS_CHANNELID = Long.parseLong(config.getProperty("discord.logs.channelid"));
         DISCORD_MARKET_CHANNELID = Long.parseLong(config.getProperty("discord.market.channelid"));

         DISCORD_BOT_AVATAR = config.getProperty("discord.bot.avatar");
         DISCORD_BOT_TOKEN = config.getProperty("discord.bot.token");
         DISCORD_BOT_PREFIX = config.getProperty("discord.bot.prefix");

         SERVER_GAME_LINK = config.getProperty("server.game.path");

         DISCORD_ZONE_WEBHOOK = config.getProperty("discord.zone.webhook");
         DISCORD_ZONE_AVATAR = config.getProperty("discord.zone.avatar");
         DISCORD_ZONE_NAME = config.getProperty("discord.zone.name");

         DISCORD_WORLD_WEBHOOK = config.getProperty("discord.world.webhook");
         DISCORD_WORLD_AVATAR = config.getProperty("discord.world.avatar");
         DISCORD_WORLD_NAME = config.getProperty("discord.world.name");

         DISCORD_PARTY_WEBHOOK = config.getProperty("discord.party.webhook");
         DISCORD_PARTY_AVATAR = config.getProperty("discord.party.avatar");
         DISCORD_PARTY_NAME = config.getProperty("discord.party.name");

         DISCORD_GUILD_WEBHOOK = config.getProperty("discord.guild.webhook");
         DISCORD_GUILD_AVATAR = config.getProperty("discord.guild.avatar");
         DISCORD_GUILD_NAME = config.getProperty("discord.guild.name");

         DISCORD_PRIVATE_WEBHOOK = config.getProperty("discord.private.webhook");
         DISCORD_PRIVATE_AVATAR = config.getProperty("discord.private.avatar");
         DISCORD_PRIVATE_NAME = config.getProperty("discord.private.name");

         DISCORD_SERVER_WEBHOOK = config.getProperty("discord.server.webhook");
         DISCORD_SERVER_AVATAR = config.getProperty("discord.server.avatar");
         DISCORD_SERVER_NAME = config.getProperty("discord.server.name");

         HashSet filters = new HashSet();

         for(int requests = 1; requests <= 20; ++requests) {
            if(config.getProperty("antiflood.request.guarded." + requests) != null) {
               filters.add(config.getProperty("antiflood.request.guarded." + requests));
            }
         }

         HashMap var36 = new HashMap();

         for(int i = 1; i <= 100; ++i) {
            if(config.getProperty("handler.requests." + i) != null) {
               String request = config.getProperty("handler.requests." + i);
               String[] requestProp = request.split("=");
               var36.put(requestProp[0], requestProp[1]);
            }
         }

         REQUESTS = var36;
         ANTI_REQUESTFLOOD_GUARDED = filters;
      } catch (IOException var34) {
         SmartFoxServer.log.severe("Error in loading configuration: " + var34.getMessage());
      }

   }
}
