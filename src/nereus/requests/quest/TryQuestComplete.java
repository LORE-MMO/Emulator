package nereus.requests;

import com.google.common.collect.Multimap;
import nereus.db.objects.Area;
import nereus.db.objects.Item;
import nereus.db.objects.Quest;
import nereus.db.objects.QuestReward;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import com.google.common.collect.HashMultimap;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

import java.util.*;
import java.util.Map.Entry;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

public class TryQuestComplete implements IRequest
{
   private static final Random rand = new Random();
   private static final List<Integer> doom = Arrays.asList(3073, 3074, 3075, 3076);
   private static final List<Integer> destiny = Arrays.asList(3128, 3129, 3130, 3131);
   private static final int boost = 19189;
   private static final int potion = 18927;

   @Override
   public void process(String[] params, User user, World world, Room room) throws RequestException
   {
      int questId = Integer.parseInt(params[0]);
      int itemId = Integer.parseInt(params[1]);

//      Set userQuests = (Set)user.properties.get("quests");
      Map<Integer, Integer> factions = (Map)user.properties.get(Users.FACTIONS);
      Set<Integer> userQuests = (Set)user.properties.get(Users.QUESTS);

      Quest quest = world.quests.get(questId);
      JSONObject ccqr = new JSONObject();

      ccqr.put("cmd", "poqwjeijgfas"); // ccqr
      ccqr.put("QuestID", questId);
      ccqr.put("bSuccess", 0);

      if (user.properties.get(Users.LOAD) != null && (Boolean)user.properties.get(Users.LOAD)) {
         if (!quest.locations.isEmpty()) {
            int mapId = (world.areas.get(room.getName().split("-")[0])).getId();
            if (!quest.locations.contains(mapId)) {
               world.send(ccqr, user);
               world.users.log(user, "Invalid Quest Complete", "Quest complete for #" + questId + "  triggered at different location: " + room.getName() + " (" + mapId + ")");
               return;
            }
         }

//         JSONObject rewardObj = new JSONObject();
         JSONObject rewardObj;

         if (quest.isUpgrade() && (Integer)user.properties.get(Users.UPGRADE_DAYS) < 0) {
            world.send(ccqr, user);
            world.users.log(user, "Packet Edit [TryQuestComplete]", "Attempted to complete member-only quest.");
         } else if (quest.getFactionId() > 1 && factions.containsKey(quest.getFactionId()) && (Integer)factions.get(quest.getFactionId()) < quest.getReqReputation()) {
            world.send(ccqr, user);
            world.users.log(user, "Packet Edit [TryQuestComplete]", "Attempted to complete a quest without required reputation.");
         } else {
            if (!userQuests.contains(questId) && !doom.contains(questId) && !destiny.contains(questId)) {
               world.users.log(user, "Packet Edit [TryQuestComplete]", "Attempted to complete an unaccepted quest: " + quest.getName());
            } else {
               if (!quest.getField().isEmpty() && world.users.getAchievement(quest.getField(), quest.getIndex(), user) != 0) {
                  world.send(ccqr, user);
                  world.users.log(user, "Packet Edit [TryQuestComplete]", "Failed to pass achievement validation while attempting to complete quest: " + quest.getName());
                  world.send(new String[]{"server", "Quest daily/monthly limit has been reached. Please try again later."}, user);
                  return;
               }

               int randomKey;
               if ((doom.contains(questId) || destiny.contains(questId)) && (Integer)user.properties.get(Users.LEVEL) < quest.getLevel()) {
                  rewardObj = new JSONObject();
                  rewardObj.put("cmd", "aowkkqklwkass"); //popupmsg
                  rewardObj.put("strMsg", "You need to be atleast level " + quest.getLevel() + " to spin the wheel!");
                  rewardObj.put("strGlow", "red,medium");
                  rewardObj.put("bitSuccess", 0);
                  world.send(rewardObj, user);
                  return;
               }

               if (world.users.turnInItems(user, quest.requirements)) {
                  if (!doom.contains(questId) && !destiny.contains(questId)) {
                     if (quest.rewards.size() > 0) {
                        Multimap<Integer, Integer> randomItems = HashMultimap.create();
                        Multimap<Integer, Integer> chooseItems = HashMultimap.create();
                        Iterator var27 = quest.rewards.entries().iterator();
                        Iterator<Map.Entry<Integer, QuestReward>> iterate = quest.rewards.entries().iterator();

                        while (var27.hasNext()) {
                           Entry<Integer, QuestReward> entry = (Entry)var27.next();
                           QuestReward info = (QuestReward)entry.getValue();
                           String var36 = info.type;
                           byte var38 = -1;
                           switch(var36.hashCode()) {
                              case 67:
                                 if (var36.equals("C")) {
                                    var38 = 0;
                                 }
                                 break;
                              case 82:
                                 if (var36.equals("R")) {
                                    var38 = 1;
                                 }
                                 break;
                              case 83:
                                 if (var36.equals("S")) {
                                    var38 = 3;
                                 }
                                 break;
                              case 3492901:
                                 if (var36.equals("rand")) {
                                    var38 = 2;
                                 }
                           }

                           switch(var38) {
                              case 0:
                                 if (itemId == info.itemId) {
                                    world.users.dropItem(user, info.itemId, info.quantity);
                                 }
                                 break;
                              case 1:
                              case 2:
                                 randomItems.put(info.itemId, info.quantity);
                                 break;
                              case 3:
                              default:
                                 world.users.dropItem(user, info.itemId, info.quantity);
                           }
                        }

                        if (randomItems.size() > 0) {
                           List<Integer> keys = new ArrayList(randomItems.keySet());
                           randomKey = (Integer)keys.get(rand.nextInt(keys.size()));
                           Object[] qty = randomItems.get(randomKey).toArray();
                           int randomValue = (Integer)qty[rand.nextInt(qty.length)];
                           world.users.dropItem(user, randomKey, randomValue);
                        }
                     }
                  } else {
                     if (doom.contains(quest.getId())) this.doWheel(user, world, "Doom");
                     if (destiny.contains(quest.getId())) this.doWheel(user, world, "Destiny");
                  }

                  world.users.giveRewards(user, quest.getExperience(), quest.getGold(), quest.getClassPoints(), quest.getReputation(), quest.getFactionId(), user.getUserId(), "p");
                  rewardObj = new JSONObject();
                  rewardObj.put("intGold", quest.getGold());
                  rewardObj.put("intExp", quest.getExperience());
                  rewardObj.put("iCP", quest.getClassPoints());

                  if (quest.getFactionId() > 0) {
                     rewardObj.put("iRep", quest.getReputation());
                  }

//                  if (userQuests.contains(questId)) {
//                     world.db.getJdbc().run("DELETE FROM users_quests WHERE UserID = ? AND QuestID = ?", user.properties.get(Users.DATABASE_ID), questId);
//                     userQuests.remove(questId);
//                  }

//                  if (quest.getAchievementId() > 0) {
//                     Achievement achievement = world.achievements.get(quest.getAchievementId());
//                     world.users.addAchievement(user, achievement);
//                  }

                  ccqr.put("rewardObj", rewardObj);
                  ccqr.put("sName", quest.getName());
                  ccqr.put("bSuccess", 1);

                  if (quest.getSlot() > 0 && world.users.getQuestValue(user, quest.getSlot()) < quest.getValue()) {
                     world.users.setQuestValue(user, quest.getSlot(), quest.getValue());
                  }

                  if (!quest.getField().isEmpty()) {
                     world.users.setAchievement(quest.getField(), quest.getIndex(), 1, user);
                  }

                  userQuests.remove(questId);
               }
            }
            world.send(ccqr, user);
         }
      } else {
         throw new RequestException("Character is still being loaded!");
      }
   }

   private void doWheel(User user, World world, String wheelType) throws RequestException {
      JSONObject wheel = new JSONObject();
      ArrayList keys = new ArrayList(world.wheels.keySet());
      int itemId = ((Integer)keys.get(rand.nextInt(keys.size()))).intValue();
      Double chance = (Double)world.wheels.get(Integer.valueOf(itemId));
      Item item = (Item)world.items.get(Integer.valueOf(itemId));
      if(Math.random() > chance.doubleValue()) {
         this.doWheel(user, world, wheelType);
      } else {
         int charItemId = -1;
         int quantity1 = 0;
         int quantity2 = 0;
         int charItemId1 = -1;
         int charItemId2 = -1;
         world.db.jdbc.beginTransaction();

         try {
            QueryResult itemJSON = world.db.jdbc.query("SELECT * FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", new Object[]{Integer.valueOf(itemId), user.properties.get("dbId")});
            itemJSON.setAutoClose(true);
            if(!itemJSON.next()) {
               world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", new Object[]{user.properties.get("dbId"), Integer.valueOf(itemId), Integer.valueOf(item.getEnhId()), Integer.valueOf(item.getQuantity())});
               charItemId = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
            }

            itemJSON.close();
            QueryResult dropItems = world.db.jdbc.query("SELECT id, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", new Object[]{Integer.valueOf(19189), user.properties.get("dbId")});
            if(dropItems.next()) {
               charItemId1 = dropItems.getInt("id");
               quantity1 = dropItems.getInt("Quantity");
               world.db.jdbc.run("UPDATE users_items SET Quantity = (Quantity + 1) WHERE id = ?", new Object[]{Integer.valueOf(charItemId1)});
            } else {
               world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", new Object[]{user.properties.get("dbId"), Integer.valueOf(19189), Integer.valueOf(0), Integer.valueOf(1)});
               charItemId1 = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
               quantity1 = 1;
            }

            dropItems.close();
            QueryResult potionResult = world.db.jdbc.query("SELECT id, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", new Object[]{Integer.valueOf(18927), user.properties.get("dbId")});
            if(potionResult.next()) {
               charItemId2 = potionResult.getInt("id");
               quantity2 = potionResult.getInt("Quantity");
               world.db.jdbc.run("UPDATE users_items SET Quantity = (Quantity + 1) WHERE id = ?", new Object[]{Integer.valueOf(charItemId2)});
            } else {
               world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", new Object[]{user.properties.get("dbId"), Integer.valueOf(18927), Integer.valueOf(0), Integer.valueOf(1)});
               charItemId2 = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
               quantity2 = 1;
            }

            potionResult.close();
         } catch (JdbcException var20) {
            if(world.db.jdbc.isInTransaction()) {
               world.db.jdbc.rollbackTransaction();
            }

            SmartFoxServer.log.severe("Error in wheel transaction: " + var20.getMessage());
         } finally {
            if(world.db.jdbc.isInTransaction()) {
               world.db.jdbc.commitTransaction();
            }

         }

         JSONObject itemJSON1 = Item.getItemJSON(item);
         itemJSON1.put("iQty", Integer.valueOf(item.getQuantity()));
         wheel.put("cmd", "Wheel");
         if(charItemId > 0) {
            wheel.put("Item", itemJSON1);
            world.send(new String[]{"wheel", "You won " + item.getName()}, user);
            if(item.getRarity() >= 30) {
               world.sendToUsers(new String[]{"wheel", "Player <font color=\"#ffffff\">" + user.properties.get("username") + "</font> has received " + item.getName() + " from the wheel of " + wheelType});
            }
         } else {
            world.send(new String[]{"wheel", "You have already won \'" + item.getName() + "\' before. Try your luck next time."}, user);
         }

         JSONObject dropItems1 = new JSONObject();
         dropItems1.put(String.valueOf(19189), Item.getItemJSON((Item)world.items.get(Integer.valueOf(19189))));
         dropItems1.put(String.valueOf(18927), Item.getItemJSON((Item)world.items.get(Integer.valueOf(18927))));
         wheel.put("dropItems", dropItems1);
         wheel.put("CharItemID", Integer.valueOf(charItemId));
         wheel.put("charItem1", Integer.valueOf(charItemId1));
         wheel.put("charItem2", Integer.valueOf(charItemId2));
         wheel.put("iQty1", Integer.valueOf(quantity1));
         wheel.put("iQty2", Integer.valueOf(quantity2));
         world.send(wheel, user);
      }
   }
}
