package nereus.requests;

import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import nereus.config.ConfigData;
import nereus.db.objects.Area;
//import Nereus.Interface;
import nereus.discord.Webhook;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.PartyInfo;
import nereus.world.World;
import nereus.db.objects.Enhancement;
import nereus.db.objects.Item;

import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdbchelper.QueryResult;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringEscapeUtils;

public class Message implements IRequest
{
   @Override
   public void process(String[] params, User user, World world, Room room) throws RequestException
   {
      if ((Integer) user.properties.get("permamute") > 0) {
         throw new RequestException("You are muted! Chat privileges have been permanently revoked.");
      } else if (world.users.isMute(user)) {
         int seconds = world.users.getMuteTimeInSeconds(user);
         throw new RequestException(world.users.getMuteMessage(seconds));
      } else {
         String channel = params[1];
         String message = params[0];
         for (String word : world.chatFilters.keySet()) {
            if (message.toLowerCase().contains(word.toLowerCase())) {
               world.users.mute(user, ((Integer)world.chatFilters.get(word)).intValue(), 13);
               int seconds = world.users.getMuteTimeInSeconds(user);
               throw new RequestException(world.users.getMuteMessage(seconds));
            }
         }

         Pattern pattern = Pattern.compile("\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov|mil|biz|info|mobi|name|aero|jobs|museum|travel|[a-z]{2}))(:[\\d]{1,5})?(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");
         String[] parts = message.split("\\s");

         for (String item : parts) {
            Matcher matcher = pattern.matcher(item.toLowerCase());
            if (matcher.matches()) {
               int muteTimeInMinutes = 5;
               world.users.mute(user, muteTimeInMinutes, 12);
               int seconds = world.users.getMuteTimeInSeconds(user);
               world.send(new String[] { "warning", world.users.getMuteMessage(seconds) }, user);
               throw new RequestException("Please refrain from posting this kind of message.", "warning");
            }
         }

         if (message.replaceAll("\\<[^>]*>", "").length() > 150) {
            throw new RequestException("Unable to send. Your message is too long.", "warning");
         }

         Pattern p = Pattern.compile("HREF=\"(.*?)\"");
         Matcher m = p.matcher(message);
         ArrayList<Integer> linkedItems = new ArrayList<Integer>();
         while (m.find()) {
            String url = m.group(1);
            if (!url.contains("loadItem"))
               continue;
            String loadItem = url.split("loadItem:")[1];
            Pattern checkInt = Pattern.compile("-?[0-9]+");
            Matcher matcher = checkInt.matcher(loadItem);
            if (!matcher.matches())
               continue;
            linkedItems.add(Integer.parseInt(loadItem));
         }
         JSONObject linkedObj = new JSONObject();
         JSONArray items = new JSONArray();
         linkedObj.put("cmd", "sendLinkedItems");
         linkedObj.put("bitSuccess", Integer.valueOf(1));
         for (Iterator<Integer> iterator = linkedItems.iterator(); iterator.hasNext(); ) {
            int charItemId = (Integer) iterator.next();
            QueryResult result = world.db.jdbc.query("SELECT * FROM users_items WHERE id = ?", charItemId);
            if (result.next()) {
               int itemId = result.getInt("ItemID");
               int quantity = result.getInt("Quantity");
               int enhId = result.getInt("EnhID");
//                    int bind = result.getInt("Bind");
               Date startDate = result.getDate("DatePurchased");
               String dPurchase = result.getString("DatePurchased");
               result.close();
               Item itemObj = (Item)world.items.get(Integer.valueOf(itemId));
               Enhancement enhancement = (Enhancement)world.enhancements.get(Integer.valueOf(enhId));
               JSONObject item = Item.getItemJSON(itemObj, enhancement);
               item.put("bBank", "0");
               item.put("CharItemID", Integer.valueOf(charItemId));
               item.put("iQty", Integer.valueOf(quantity));
               item.put("iReqCP", Integer.valueOf(itemObj.getReqClassPoints()));
               item.put("iReqRep", Integer.valueOf(itemObj.getReqReputation()));
               item.put("FactionID", Integer.valueOf(itemObj.getFactionId()));
               item.put("sFaction", world.factions.get(Integer.valueOf(itemObj.getFactionId())));
//                    item.put("bBound", Integer.valueOf(bind));
               if (((Item)world.items.get(Integer.valueOf(itemId))).isCoins()) {
                  Date endDate = new Date();
                  long diff = endDate.getTime() - startDate.getTime();
                  long diffHours = diff / 3600000L;
                  item.put("iHrs", Long.valueOf(diffHours));
                  item.put("dPurchase", dPurchase.replaceAll(" ", "T"));
               }
               items.add(item);
            }
            result.close();
         }
         linkedObj.put("items", items);

         if (channel.equals("world")) {
            if ((Integer) user.properties.get("access") <= 0) {
               throw new RequestException("Your account is currently disabled. Actions in-game are limited.", "warning");
            }

            LinkedList listOfChannels = world.zone.getAllUsersInZone();
            Iterator var52 = listOfChannels.iterator();

            while (var52.hasNext()) {
               Object temp = var52.next();
               User tgt = ExtensionHelper.instance().getUserByChannel((SocketChannel) temp);
               if (linkedItems.size() > 0) {
                  world.send(linkedObj, tgt);
               }
               world.send(new String[]{"chatm", "world~" + message, user.getName(), String.valueOf(1)}, tgt);
            }

//                for (Object temp : listOfChannels) {
//                    User tgt = ExtensionHelper.instance().getUserByChannel((SocketChannel)temp);
//                    if (tgt == null) continue;
//                    if (linkedItems.size() > 0) world.send(linkedObj, tgt);
//                    world.send(new String[] { "chatm", "world~" + message, user.getName(), String.valueOf(1) }, tgt);
//                }
         } else if (channel.equals("party")) {
            int partyId = (Integer) user.properties.get("partyId");
            if (partyId < 0) throw new RequestException("You are not in a party.", "server");
            PartyInfo pi = world.parties.getPartyInfo(partyId);
            if (linkedItems.size() > 0) world.send(linkedObj, pi.getChannelList());
            world.send(new String[] { "chatm", "party~" + message, user.getName(), String.valueOf(1) }, pi.getChannelList());
         } else if (channel.equals("guild")) {
            if ((Integer) user.properties.get("guildid") > 0) {
               JSONObject guildData = (JSONObject)user.properties.get("guildobj");
               JSONArray members = (JSONArray)guildData.get("ul");
               if (members != null && members.size() > 0) {
                  for (Iterator<JSONObject> it = members.iterator(); it.hasNext(); ) {
                     JSONObject member = it.next();
                     User client = world.zone.getUserByName(member.get("userName").toString().toLowerCase());
                     if (client != null) {
                        if (linkedItems.size() > 0) world.send(linkedObj, client);
                        world.send(new String[] { "chatm", "guild~" + message, user.getName(), String.valueOf(1) }, client);
                     }
                  }
               }
            } else {
               throw new RequestException("You are not in a guild.", "server");
            }
         } else {
            int access = (Integer) user.properties.get("access");
            switch (access) {
               case 40:
                  channel = "mod";
                  break;
               case 60:
                  channel = "admin";
                  break;
               default:
                  channel = "zone";
            }
//                channel = "zone";
            if (linkedItems.size() > 0) {
               world.sendToRoom(linkedObj, user, room);
            }

            HashMap<String, String> log = new HashMap<String, String>();
            if (user.properties.get("Chat") != null) {
               log.putAll((HashMap)user.properties.get("Chat"));
            }

            log.put(channel, message);
            user.properties.put("Chat", log);
            world.sendToRoom(new String[] {"chatm", channel + "~" + message, user.getName(), String.valueOf(room.getId())}, user, room);
         }
         world.applyFloodFilter(user, message);

         message = URLDecoder.decode(message);
         message = StringEscapeUtils.unescapeXml(message);
         message = StringEscapeUtils.unescapeHtml4(message);
         message = StringEscapeUtils.unescapeHtml3(message);

         String UpperRoom = room.getName().substring(0, 1).toUpperCase() + room.getName().substring(1);
         String UpperName = user.getName().substring(0, 1).toUpperCase() + user.getName().substring(1);
         Area area = (world.areas.get(room.getName().split("-")[0]));
         try {
            switch (channel) {
               case "world": {
                  Webhook webhook = new Webhook(ConfigData.DISCORD_WORLD_WEBHOOK);
                  if (linkedItems.size() > 0) return;
                  webhook.setContent("**[World]** **[" + UpperName + "](<" + ConfigData.SERVER_PROFILE_LINK + UpperName + ">)**:  " + message);
                  webhook.setTts(false);
                  webhook.execute();
                  break;
               }
               case "party": {
                  Webhook webhook = new Webhook(ConfigData.DISCORD_PARTY_WEBHOOK);
                  webhook.setContent("**[Party]** **[" + UpperName + "](<" + ConfigData.SERVER_PROFILE_LINK + UpperName + ">)**:  " + message);
                  webhook.setTts(false);
                  webhook.execute();
                  break;
               }
               case "guild": {
                  Webhook webhook = new Webhook(ConfigData.DISCORD_GUILD_WEBHOOK);
                  webhook.setContent("**[Guild]** **[" + UpperName + "](<" + ConfigData.SERVER_PROFILE_LINK + UpperName + ">)**:  " + message);
                  webhook.setTts(false);
                  webhook.execute();
                  break;
               }
               default: {
                  Webhook webhook = new Webhook(ConfigData.DISCORD_ZONE_WEBHOOK);
                  if (linkedItems.size() > 0) return;
                  webhook.setContent("**[" + UpperRoom + "]** **[" + UpperName + "](<" + ConfigData.SERVER_PROFILE_LINK + UpperName + ">)**:  " + message);
                  webhook.setTts(false);
                  webhook.execute();
                  break;
               }
            }
         } catch (IOException e) {
            SmartFoxServer.log.severe(ConfigData.DISCORD_PRIVATE_NAME + " is unable to send Webhook: " + e);
         }
      }
   }
}
