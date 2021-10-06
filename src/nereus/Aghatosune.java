package nereus;

import nereus.config.ConfigData;
import nereus.console.Console;
import nereus.discord.Bot;
import nereus.discord.Webhook;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.log.SimpleLogFormat;
import nereus.world.Users;
import nereus.world.World;

import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.events.InternalEventObject;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

import net.sf.json.JSONObject;

public class Aghatosune extends AbstractExtension
{
   private final List<String> allowedRequestsForBannedUsers = Arrays.asList("mv", "firstJoin", "afk", "isModerator", "retrieveInventory", "moveToCell", "retrieveUserData", "retrieveUserDatas", "emotea");
   private final Map<String, String> requests = new HashMap();
   private ExtensionHelper helper;
   private Console console;
   private World world;
   private HashMap<String, Long> IPList = new HashMap();
   private HashMap<String, Integer> IPCounter = new HashMap();
//   public Bot bot = new Bot(ConfigData.DISCORD_BOT_TOKEN);
   public Webhook webhook = new Webhook(ConfigData.DISCORD_SERVER_WEBHOOK);

   public Aghatosune() {
      super();
      Handler[] handlers = SmartFoxServer.log.getHandlers();
      Handler[] arr$ = handlers;
      int len$ = handlers.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Handler handler = arr$[i$];
         handler.setFormatter(new SimpleLogFormat());
      }
      
      this.console = new Console();
   }

   public void init() {
      this.requests.putAll(ConfigData.REQUESTS);
      this.helper = ExtensionHelper.instance();
      this.world = new World(this, this.helper.getZone(this.getOwnerZone()));
      this.console.setWorld(this.world);
      this.console.setHelper(this.helper);
      this.world.db.jdbc.run("UPDATE servers SET Online = 1 WHERE Name = ?", ConfigData.SERVER_NAME);
   }

   public void handleRequest(String cmd, ActionscriptObject ao, User user, int fromRoom) {
      throw new UnsupportedOperationException("ActionScriptObject requests are not supported.");
   }

   private boolean isRequestFiltered(User user, String request) {
      boolean filtered = false;
      long lastRequestTime = ((Long)user.properties.get("requestlastmili")).longValue() + ConfigData.ANTI_REQUESTFLOOD_MIN_MSG_TIME;
      int requestCounter = ((Integer)user.properties.get("requestcounter")).intValue();
      int requestWarningsCounter = ((Integer)user.properties.get("requestwarncounter")).intValue();
      int repeatedRequestCounter = ((Integer)user.properties.get("requestrepeatedcounter")).intValue();
      String lastRequest = (String)user.properties.get("requestlast");
      if(!user.isBeingKicked) {
         byte var10;
         if(lastRequestTime > System.currentTimeMillis()) {
            if(!ConfigData.ANTI_REQUESTFLOOD_GUARDED.contains(request)) {
               ++requestCounter;
               user.properties.put("requestcounter", Integer.valueOf(requestCounter));
               if(requestCounter >= ConfigData.ANTI_REQUESTFLOOD_TOLERANCE) {
                  this.world.send(new String[]{"warning", "Action taken too quickly, try again in a moment."}, user);

                  ++requestWarningsCounter;
                  var10 = 0;
                  user.properties.put("requestwarncounter", Integer.valueOf(requestWarningsCounter));
                  user.properties.put("requestcounter", Integer.valueOf(var10));
                  filtered = true;
               }
            }
         } else {
            var10 = 0;
            user.properties.put("requestcounter", Integer.valueOf(var10));
         }

         if(ConfigData.ANTI_REQUESTFLOOD_REPEAT_ENABLED) {
            byte var11;
            if(request.equals(lastRequest) && !ConfigData.ANTI_REQUESTFLOOD_GUARDED.contains(request)) {
               ++repeatedRequestCounter;
               user.properties.put("requestrepeatedcounter", Integer.valueOf(repeatedRequestCounter));
               if(repeatedRequestCounter >= ConfigData.ANTI_REQUESTFLOOD_MAX_REPEATED) {
                  ++requestWarningsCounter;
                  var11 = 0;
                  user.properties.put("requestwarncounter", Integer.valueOf(requestWarningsCounter));
                  user.properties.put("requestrepeatedcounter", Integer.valueOf(var11));
                  filtered = true;
               }
            } else {
               var11 = 0;
               user.properties.put("requestrepeatedcounter", Integer.valueOf(var11));
               user.properties.put("requestlast", request);
            }
         }

         if(requestWarningsCounter >= ConfigData.ANTI_REQUESTFLOOD_WARNINGS) {
            filtered = true;
            SmartFoxServer.log.warning("Too many requests for user:  " + user.properties.get("username"));
            user.isBeingKicked = true;
            SmartFoxServer.getInstance().addKickedUser(user, 1);
            this.world.users.kick(user);
         }
      }

      user.properties.put("requestlastmili", Long.valueOf(System.currentTimeMillis()));
      return filtered;
   }

   @Override
   public void handleRequest(String cmd, String[] params, User user, int fromRoom) {
//      SmartFoxServer.log.fine("Recieved request: " + cmd);
      if (user == null) return;
      if (isRequestFiltered(user, cmd)) return;

      if (this.requests.containsKey(cmd)) {
         SmartFoxServer.log.fine("Processing request from: " + user.properties.get(Users.USERNAME) + " - " + cmd);
//         try {
//
//            webhook.setContent("**[FINE]** Processing request from: **" + user.properties.get(Users.USERNAME) + "** - **" + cmd + "**");
//            webhook.setTts(false);
//            webhook.execute();
//         } catch (IOException e) {
//            SmartFoxServer.log.severe(ConfigData.DISCORD_SERVER_NAME + " is unable to send Webhook: " + e);
//         }
         int access = (Integer) user.properties.get(Users.ACCESS);
         if (access <= 0 && !this.allowedRequestsForBannedUsers.contains(cmd)) {
            this.world.send(new String[]{"warning", "Your account is currently disabled. Actions in-game are limited."}, user);
            return;
         }

         try {
            Class ex = Class.forName((String)this.requests.get(cmd));
            IRequest request = (IRequest)ex.newInstance();
            Room room;
            if(fromRoom != 1 && fromRoom != 32123 && fromRoom > 0) {
               room = this.world.zone.getRoom(fromRoom);
               if(room != null) {
                  request.process(params, user, this.world, room);
               } else {
                  this.world.users.kick(user);
               }
            } else {
               room = this.world.zone.getRoom(user.getRoom());
               request.process(params, user, this.world, room);
            }
         } catch (ClassNotFoundException var9) {
            SmartFoxServer.log.severe("Class not found:" + var9.getMessage());
         } catch (InstantiationException var10) {
            SmartFoxServer.log.severe("Instantiation error:" + var10.getMessage());
         } catch (IllegalAccessException var11) {
            SmartFoxServer.log.severe("Illegal access error:" + var11.getMessage());
         } catch (NullPointerException var12) {
            SmartFoxServer.log.severe("Null error on " + cmd + " request on line " + var12.getStackTrace()[0].getLineNumber() + ": " + var12.getMessage());
         } catch (RequestException var13) {
            this.world.send(new String[]{var13.getType(), var13.getMessage()}, user);
         }
      } else {
         this.world.send(new String[]{"server", "The action you are trying to execute is not yet implemented. Please contact the development staff if you want it available."}, user);

         SmartFoxServer.log.warning("Unknown request: " + cmd);
      }

   }

   @Override
   public void handleInternalEvent(InternalEventObject ieo)
   {
      String event = ieo.getEventName();
      SmartFoxServer.log.fine("System event: " + ieo.getEventName());
//      try {
//         webhook.setContent("**[FINE]** System event: **" + ieo.getEventName() + "**");
//         webhook.setTts(false);
//         webhook.execute();
//      } catch (IOException e) {
//         SmartFoxServer.log.severe(ConfigData.DISCORD_SERVER_NAME + " is unable to send Webhook: " + e);
//      }

      if (event.equals(InternalEventObject.EVENT_SERVER_READY)) {
         this.console.start();
      } else if (event.equals(InternalEventObject.EVENT_LOGIN)) {
         String nick = ieo.getParam("nick").split("~")[1];
         String pass = ieo.getParam("pass");
         SocketChannel chan = (SocketChannel) ieo.getObject("chan");
         String address = chan.socket().getInetAddress().getHostAddress();
         User user = this.world.zone.getUserByName(nick.toLowerCase());
         if (user != null) {
            this.world.send(new String[]{"multiLoginWarning"}, chan);
            SmartFoxServer.log.fine("Multilogin detected: " + user.getName());
            this.world.users.kick(user);
         } else {
            this.world.users.login(nick.toLowerCase(), pass, chan);
         }
      } else if (event.equals(InternalEventObject.EVENT_NEW_ROOM)) {
         Room room = (Room) ieo.getObject("room");
//         try {
//            webhook.setContent("**[FINE]** New room created: **" + room.getName() + "**");
//            webhook.setTts(false);
//            webhook.execute();
//         } catch (IOException e) {
//            SmartFoxServer.log.severe(ConfigData.DISCORD_SERVER_NAME + " is unable to send Webhook: " + e);
//         }
         SmartFoxServer.log.fine("New room created: " + room.getName());
      } else if (event.equals(InternalEventObject.EVENT_JOIN)) {
         Room room = (Room) ieo.getObject("room");
         User user = (User) ieo.getObject("user");

         JSONObject userObj = this.world.users.getProperties(user, room);
         JSONObject uJoin = new JSONObject();
         uJoin.put("cmd", "uotls");
         uJoin.put("o", userObj);
         uJoin.put("unm", user.getName());

         this.world.sendToRoomButOne(uJoin, user, room);
      } else if (event.equals(InternalEventObject.EVENT_USER_LOST)) {
         User user = (User) ieo.getObject("user");
         Room room = this.world.zone.getRoom(user.getRoom());
         if (room != null) {
            room.removeUser(user, true, true);
            this.world.rooms.exit(room, user);
            if (room.getUserCount() <= 0) this.helper.destroyRoom(this.world.zone, room.getId());
         }
         this.world.users.lost(user);
         this.world.db.jdbc.run("UPDATE servers SET Count = ? WHERE Name = ?", this.world.zone.getUserCount(), ConfigData.SERVER_NAME);
      } else if (event.equals(InternalEventObject.EVENT_USER_EXIT)) {
         Room room = (Room) ieo.getObject("room");
         User user = (User) ieo.getObject("user");
         room.removeUser(user, true, true);
         this.world.rooms.exit(room, user);
         if (room.getUserCount() <= 0) this.helper.destroyRoom(this.world.zone, room.getId());
      }
   }

   public void kickMaliciousClient(SocketChannel chan) throws IOException {
      DataInputStream dataIn = new DataInputStream(chan.socket().getInputStream());
      DataOutputStream dataOut = new DataOutputStream(chan.socket().getOutputStream());
      chan.close();
      chan.socket().close();
      dataIn.close();
      dataOut.flush();
      dataOut.close();
   }

   public void destroy()
   {
    this.console.stop();
      this.world.db.jdbc.run("UPDATE servers SET Online = 0 WHERE Name = ?", new Object[]{ConfigData.SERVER_NAME});
      this.world.destroy();
      SmartFoxServer.log.info("pie destroyed");
   }
}
