package nereus.requests.trade;

import nereus.db.objects.Enhancement;
import nereus.db.objects.Item;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import jdbchelper.JdbcException;
import jdbchelper.NoResultException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

public class TradeDeal implements IRequest
{
   private World world;

   @Override
   public void process(String[] params, User user, World world, Room room) throws RequestException
   {
      this.world = world;
      User client = SmartFoxServer.getInstance().getUserById(Integer.parseInt(params[0]));
      if (client == null) {
         (new TradeCancel()).process(new String[]{Integer.toString(-1)}, user, world, room);
         throw new RequestException("Trade has been canceled due to other player can\'t be found!");
      } else if (client.getUserId() != (Integer)user.properties.get(Users.TRADE_TARGET)) {
         (new TradeCancel()).process(new String[]{Integer.toString(-1)}, user, world, room);
         throw new RequestException(client.getName() + " has canceled the trade.");
      } else if (user.getUserId() != (Integer)client.properties.get(Users.TRADE_TARGET)) {
         (new TradeCancel()).process(new String[]{Integer.toString(-1)}, user, world, room);
         throw new RequestException(client.getName() + " has canceled the trade.");
      } else if (user.getName().equals(client.getName())) {
         (new TradeCancel()).process(new String[]{Integer.toString(-1)}, user, world, room);
         throw new RequestException(client.getName() + " has canceled the trade.");
      } else {
         JSONObject tr = new JSONObject();
         tr.element("cmd", "tradeDeal");
         tr.element("bitSuccess", 0);
         user.properties.put(Users.TRADE_DEAL, true);
         if (!(Boolean)client.properties.get(Users.TRADE_DEAL)) {
            tr.element("bitSuccess", 1);
            tr.element("onHold", 1);
            world.send(tr, user);
         } else if ((Boolean)client.properties.get(Users.TRADE_LOCK) && (Boolean)user.properties.get(Users.TRADE_LOCK)) {
            Map<Integer, Integer> offers1 = (Map)user.properties.get(Users.ITEM_OFFER);
            Map<Integer, Integer> offers2 = (Map)client.properties.get(Users.ITEM_OFFER);
            boolean currencyCheck1 = false;
            boolean currencyCheck2 = false;
            boolean stackCheck1 = true;
            boolean stackCheck2 = true;
            Item item1 = null;
            Item item2 = null;
            int coins1 = 0;
            int gold1 = 0;
            int coins2 = 0;
            int gold2 = 0;
            String excludeItemUser = "";
            Iterator var20 = offers2.entrySet().iterator();

            int inventoryCount2;
            while(var20.hasNext()) {
               Entry<Integer, Integer> entry = (Entry)var20.next();
               inventoryCount2 = (Integer)entry.getKey();
               if (excludeItemUser.isEmpty()) {
                  excludeItemUser = Integer.toString(inventoryCount2);
               } else {
                  excludeItemUser = excludeItemUser + "," + Integer.toString(inventoryCount2);
               }
            }

            if (!excludeItemUser.isEmpty()) {
               excludeItemUser = " AND ItemID NOT IN (" + excludeItemUser + ")";
            }

            String excludeItemClient = "";
            Iterator var37 = offers1.entrySet().iterator();

            while(var37.hasNext()) {
               Entry<Integer, Integer> entry = (Entry)var37.next();
               int itemId = (Integer)entry.getKey();
               if (excludeItemClient.isEmpty()) {
                  excludeItemClient = Integer.toString(itemId);
               } else {
                  excludeItemClient = excludeItemClient + "," + Integer.toString(itemId);
               }
            }

            if (!excludeItemClient.isEmpty()) {
               excludeItemClient = " AND ItemID NOT IN (" + excludeItemClient + ")";
            }

            int inventoryCount1 = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment NOT IN ('ho','hi') AND Bank = 0 AND UserID = ?" + excludeItemUser, user.properties.get(Users.DATABASE_ID));
            inventoryCount2 = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment NOT IN ('ho','hi') AND Bank = 0 AND UserID = ?" + excludeItemClient, client.properties.get(Users.DATABASE_ID));
            QueryResult userResult = world.db.jdbc.query("SELECT Coins, Gold FROM users WHERE id = ?", user.properties.get("dbId"));
            if (userResult.next()) {
               gold1 = userResult.getInt("Gold");
               coins1 = userResult.getInt("Coins");
               if (gold1 >= (Integer)user.properties.get(Users.TRADE_GOLD) && coins1 >= (Integer)user.properties.get(Users.TRADE_COINS)) {
                  currencyCheck1 = true;
               }
            }

            userResult.close();
            userResult = world.db.jdbc.query("SELECT Coins, Gold FROM users WHERE id = ?", client.properties.get(Users.DATABASE_ID));
            if (userResult.next()) {
               gold2 = userResult.getInt("Gold");
               coins2 = userResult.getInt("Coins");
               if (gold2 >= (Integer)client.properties.get(Users.TRADE_GOLD) && coins2 >= (Integer)client.properties.get(Users.TRADE_COINS)) {
                  currencyCheck2 = true;
               }
            }

            userResult.close();
//            Iterator i$ = offers1.entrySet().iterator();
            Iterator var24 = offers1.entrySet().iterator();

            Entry entry;
            int finalClientCopper;
            int finalClientGold;
            int itemQty;
            while (var24.hasNext()) {
               entry = (Entry)var24.next();
               finalClientCopper = (Integer)entry.getKey();
               finalClientGold = (Integer)entry.getValue();
               item1 = (Item)world.items.get(finalClientCopper);

               try {
                  itemQty = world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ?  AND Bank = 0", finalClientCopper, client.properties.get(Users.DATABASE_ID));
                  if (item1.getStack() <= 1) {
                     stackCheck1 = false;
                     break;
                  }

                  if (itemQty + finalClientGold > item1.getStack()) {
                     stackCheck1 = false;
                     break;
                  }
               } catch (NoResultException var35) {
                  ;
               }
            }

            var24 = offers2.entrySet().iterator();

            while (var24.hasNext()) {
               entry = (Entry)var24.next();
               finalClientCopper = (Integer)entry.getKey();
               finalClientGold = (Integer)entry.getValue();
               item2 = (Item)world.items.get(finalClientCopper);

               try {
                  itemQty = world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ?  AND Bank = 0", finalClientCopper, user.properties.get(Users.DATABASE_ID));
                  if (item2.getStack() <= 1) {
                     stackCheck2 = false;
                     break;
                  }

                  if (itemQty + finalClientGold > item2.getStack()) {
                     stackCheck2 = false;
                     break;
                  }
               } catch (NoResultException var34) {
                  ;
               }
            }

            int finalUserCopper = coins1 - (Integer)user.properties.get(Users.TRADE_COINS) + (Integer)client.properties.get(Users.TRADE_COINS);
            int finalUserGold = gold1 - (Integer)user.properties.get(Users.TRADE_GOLD) + (Integer)client.properties.get(Users.TRADE_GOLD);
            finalClientCopper = coins2 - (Integer)client.properties.get(Users.TRADE_COINS) + (Integer)user.properties.get(Users.TRADE_COINS);
            finalClientGold = gold2 - (Integer)client.properties.get(Users.TRADE_GOLD) + (Integer)user.properties.get(Users.TRADE_GOLD);

            if (inventoryCount1 + offers2.size() > (Integer)user.properties.get(Users.SLOTS_BAG)) {
               tr.element("msg", "Your inventory is full!");
               world.send(tr, user);
               tr.element("msg", user.getName() + "\'s inventory is full!");
               world.send(tr, client);
            } else if (inventoryCount2 + offers1.size() > (Integer)client.properties.get(Users.SLOTS_BAG)) {
               tr.element("msg", "Your inventory is full!");
               world.send(tr, client);
               tr.element("msg", client.getName() + "\'s inventory is full!");
               world.send(tr, user);
            } else if(!currencyCheck1) {
               tr.element("msg", "You do not have enough gold/coins!");
               world.send(tr, user);
               tr.element("msg", user.getName() + " does not have enough gold/coins!");
               world.send(tr, client);
            } else if(!currencyCheck2) {
               tr.element("msg", "You do not have enough gold/coins!");
               world.send(tr, client);
               tr.element("msg", client.getName() + " does not have enough gold/coins!");
               world.send(tr, user);
            } else if(!stackCheck1) {
               tr.element("msg", "You cannot have more than " + item1.getStack() + " of " + item1.getName() + "!");
               world.send(tr, client);
               tr.element("msg", client.getName() + " cannot have more than " + item1.getStack() + " of " + item1.getName() + "!");
               world.send(tr, user);
            } else if(!stackCheck2) {
               tr.element("msg", "You cannot have more than " + item2.getStack() + " of " + item2.getName() + "!");
               world.send(tr, user);
               tr.element("msg", user.getName() + " cannot have more than " + item2.getStack() + " of " + item2.getName() + "!");
               world.send(tr, client);
            } else if (finalUserGold > 999999) {
               tr.put("msg", "Maximum amount of coins limit reached!");
               world.send(tr, client);
               tr.put("msg", client.getName() + " has reached maximum amount of currency limit!");
               world.send(tr, user);
            } else if (finalUserCopper > 999999) {
               tr.put("msg", "Maximum amount of gold limit reached!");
               world.send(tr, client);
               tr.put("msg", client.getName() + " has reached maximum amount of currency limit!");
               world.send(tr, user);
            } else {
               this.world.db.jdbc.beginTransaction();
               try {
                  if (turnInItems(user, offers1, client, offers2)) {
                     user.properties.put(Users.TRADE_TARGET, -1);
                     user.properties.put(Users.TRADE_LOCK, false);
                     user.properties.put(Users.TRADE_DEAL, false);

                     client.properties.put(Users.TRADE_TARGET, -1);
                     client.properties.put(Users.TRADE_LOCK, false);
                     client.properties.put(Users.TRADE_DEAL, false);
                     SmartFoxServer.log.fine("Passed security Check!");

                     int itemId;
                     int quantity;
                     Item itemObj;
                     Map enhances;

                     SmartFoxServer.log.fine("Iterate offer1 begin >");
                     Iterator var43 = offers1.entrySet().iterator();

                     while (var43.hasNext()) {
                        entry = (Entry)var43.next();
                        itemId = (Integer)entry.getKey();
                        quantity = (Integer)entry.getValue();
                        itemObj = world.items.get(itemId);
                        if (itemObj != null) {
                           enhances = (Map)user.properties.get(Users.ITEM_OFFER_ENHANCEMENT);
                           if (!sendItem(client, user, itemObj, quantity, (Integer)enhances.get(itemId))) {
                              tr.put("msg", "An error occurred while trying to send the item " + itemObj.getName() + ".");
                              world.send(tr, user);
                              return;
                           }
                        }
                     }

                     SmartFoxServer.log.fine("Iterate offer2 begin >");
                     var43 = offers2.entrySet().iterator();

                     while (var43.hasNext()) {
                        entry = (Entry)var43.next();
                        itemId = (Integer)entry.getKey();
                        quantity = (Integer)entry.getValue();
                        itemObj = world.items.get(itemId);
                        if (itemObj != null) {
                           enhances = (Map)client.properties.get(Users.ITEM_OFFER_ENHANCEMENT);
                           if (!sendItem(user, client, itemObj, quantity, (Integer)enhances.get(itemId))) {
                              tr.put("msg", "An error occurred while trying to send the item " + itemObj.getName() + ".");
                              world.send(tr, user);
                              return;
                           }
                        }
                     }

                     this.updateGoldCoins(user, client, finalUserCopper, finalUserGold, (Integer)user.properties.get(Users.TRADE_COINS), (Integer)user.properties.get(Users.TRADE_GOLD));
                     this.updateGoldCoins(client, user, finalClientCopper, finalClientGold,  (Integer)client.properties.get(Users.TRADE_COINS), (Integer)client.properties.get(Users.TRADE_GOLD));
                  } else {
                     tr.put("msg", "Items verification check failed! Please re-log and try again. If you still keep seeing this message, please report to Game Administrators.");
                     world.send(tr, client);
                     world.send(tr, user);
                  }
               } catch (JdbcException var17) {
                  if (this.world.db.jdbc.isInTransaction()) {
                     this.world.db.jdbc.rollbackTransaction();
                  }
                  SmartFoxServer.log.severe("Error in send item transaction: " + var17.getMessage());
               } finally {
                  if (this.world.db.jdbc.isInTransaction()) {
                     this.world.db.jdbc.commitTransaction();
                  }
               }
               user.properties.put(Users.ITEM_OFFER, new HashMap());
               user.properties.put(Users.ITEM_OFFER_ENHANCEMENT, new HashMap());
               client.properties.put(Users.ITEM_OFFER, new HashMap());
               client.properties.put(Users.ITEM_OFFER_ENHANCEMENT, new HashMap());
               user.properties.put(Users.TRADE_COINS, 0);
               user.properties.put(Users.TRADE_GOLD, 0);
               client.properties.put(Users.TRADE_COINS, 0);
               client.properties.put(Users.TRADE_GOLD, 0);
               tr.put("bitSuccess", 1);
               world.send(tr, user);
               world.send(tr, client);
               SmartFoxServer.log.fine("Deal success");
            }
         } else {
            tr.element("msg", "Your/His offer/s is not yet confirmed!");
            world.send(tr, user);
            world.send(tr, client);
         }
      }
   }

   private void updateGoldCoins(User user, User client, int coins, int gold, int tradedCoins, int tradedGold) {
//   private void updateGoldCoins(User user, int gold, int coins) {
      JSONObject tr = new JSONObject();
      tr.element("cmd", "updateGoldCoins");
      tr.element("gold", gold);
      tr.element("coins", coins);
      this.world.send(tr, user);
      this.world.db.jdbc.run("UPDATE users SET Coins = ?, Gold = ? WHERE id = ?", coins, gold, user.properties.get("dbId"));
      this.world.db.jdbc.run("INSERT INTO users_trades (FromUserID, ToUserID, Coins, Gold) VALUES (?, ?, ?, ?)", user.properties.get(Users.DATABASE_ID), client.properties.get(Users.DATABASE_ID), tradedCoins, tradedGold);

   }

   private boolean sendItem(User user, User client, Item itemObj, int quantity, int enhId)  {
//   private void sendItem(User user, Item itemObj, int quantity, int enhId) throws RequestException {
      int itemId = itemObj.getId();
      SmartFoxServer.log.fine(itemId + " - " + quantity + " to " + user.getName());
      JSONObject di = new JSONObject();
      JSONObject arrItems = new JSONObject();
      Enhancement enhancement = (Enhancement)this.world.enhancements.get(enhId);

      JSONObject item = Item.getItemJSON(itemObj, enhancement);
      item.element("iQty", quantity);
      item.element("iReqCP", itemObj.getReqClassPoints());
      item.element("iReqRep", itemObj.getReqReputation());
      item.element("FactionID", itemObj.getFactionId());
      item.element("sFaction", this.world.factions.get(itemObj.getFactionId()));

      arrItems.put(Integer.valueOf(itemId), item);

      di.element("items", arrItems);
      di.element("addItem", 1);
      di.element("cmd", "dropItem");
      this.world.send(di, user);

      JSONObject gd = new JSONObject();
      gd.element("cmd", "getDrop");
      gd.element("ItemID", itemId);
      gd.element("bSuccess", "0");
//      this.world.db.jdbc.beginTransaction();
      QueryResult itemResult = this.world.db.jdbc.query("SELECT id, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0", itemId, user.properties.get("dbId"));
      int charItemId;

      if (itemResult.next()) {
         charItemId = itemResult.getInt("id");
         int itemQty = itemResult.getInt("Quantity");
         itemResult.close();
         if (itemObj.getStack() > 1) {
            if (itemQty >= itemObj.getStack()) {
               this.world.db.jdbc.rollbackTransaction();
               this.world.send(gd, user);
               return false;
            }

            this.world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ? AND Bank = 0", itemQty + quantity, charItemId);
            this.world.db.jdbc.run("INSERT INTO users_trades_items (FromUserID, ToUserID, ItemID, EnhID, Quantity) VALUES (?, ?, ?, ?, ?)", user.properties.get(Users.DATABASE_ID), client.properties.get(Users.DATABASE_ID), itemId, enhId, quantity);
         } else if (itemObj.getStack() == 1) {
            this.world.db.jdbc.rollbackTransaction();
            this.world.send(gd, user);
            return false;
         }
      } else {
         itemResult.close();
         this.world.db.jdbc.run("INSERT INTO users_trades_items (FromUserID, ToUserID, ItemID, EnhID, Quantity) VALUES (?, ?, ?, ?, ?)", user.properties.get(Users.DATABASE_ID), client.properties.get(Users.DATABASE_ID), itemId, enhId, quantity);
         this.world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, Now())", user.properties.get("dbId"), itemId, enhId, quantity);
         charItemId = Long.valueOf(this.world.db.jdbc.getLastInsertId()).intValue();
      }
      itemResult.close();
      if (charItemId > 0) {
         gd.put("CharItemID", charItemId);
         gd.put("bBank", false);
         gd.put("iQty", quantity);
         gd.put("bSuccess", "1");
         if (enhancement.getId() > 0) {
            gd.put("EnhID", enhancement.getId());
            gd.put("EnhLvl", enhancement.getLevel());
            gd.put("EnhPatternID", enhancement.getPatternId());
            gd.put("EnhRty", enhancement.getRarity());
            gd.put("iRng", itemObj.getRange());
            gd.put("EnhRng", itemObj.getRange());
            gd.put("InvEnhPatternID", enhancement.getPatternId());
            gd.put("EnhDPS", enhancement.getDPS());
         }

         this.world.send(gd, user);
         return true;
      } else {
         this.world.db.jdbc.rollbackTransaction();
         return false;
      }
   }

   private boolean turnInItems(User user, Map<Integer, Integer> items, User user2, Map<Integer, Integer> items2) {
      Iterator var6;
      Entry entry;
      int itemId;
      int quantityRequirement;
      QueryResult itemResult;
      int charItemId;
      int quantity;
      int quantityLeft;
      for(var6 = items.entrySet().iterator(); var6.hasNext(); itemResult.close()) {
         entry = (Entry)var6.next();
         itemId = (Integer)entry.getKey();
         quantityRequirement = (Integer)entry.getValue();
         itemResult = this.world.db.jdbc.query("SELECT id, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0 FOR UPDATE", itemId, user.properties.get("dbId"));
         if (!itemResult.next()) {
            itemResult.close();
            this.world.db.jdbc.rollbackTransaction();
            this.world.users.log(user, "Suspicous TurnIn [TradeDeal]", "Item " + world.items.get(itemId).getName() +" to turn in not found in database.");
            return false;
         }
         charItemId = itemResult.getInt("id");
         quantity = itemResult.getInt("Quantity");
         quantityLeft = quantity - quantityRequirement;
         itemResult.close();
         SmartFoxServer.log.fine("Item " + itemId + ": " + quantity + " - " + quantityRequirement + " = " + quantityLeft + " for user: " + user.properties.get("dbId"));
         if (quantityLeft > 0) {
            this.world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ?", quantityLeft, charItemId);
         } else {
            if (quantityLeft < 0) {
               this.world.db.jdbc.rollbackTransaction();
               this.world.users.log(user, "Suspicous TurnIn [TradeDeal]", "Quantity requirement for turning in item is lacking.");
               return false;
            }
            this.world.db.jdbc.run("DELETE FROM users_items WHERE id = ?", charItemId);
            SmartFoxServer.log.fine("Delete item " + itemId + " for user: " + user.properties.get("dbId"));
         }
      }

      for(var6 = items2.entrySet().iterator(); var6.hasNext(); itemResult.close()) {
         entry = (Entry)var6.next();
         itemId = (Integer)entry.getKey();
         quantityRequirement = (Integer)entry.getValue();
         itemResult = this.world.db.jdbc.query("SELECT id, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0 FOR UPDATE", itemId, user2.properties.get("dbId"));
         if (!itemResult.next()) {
            itemResult.close();
            this.world.db.jdbc.rollbackTransaction();
            this.world.users.log(user, "Suspicous TurnIn [TradeDeal]", "Item " + world.items.get(itemId).getName() +" to turn in not found in database.");
            return false;
         }
         charItemId = itemResult.getInt("id");
         quantity = itemResult.getInt("Quantity");
         quantityLeft = quantity - quantityRequirement;
         itemResult.close();
         SmartFoxServer.log.fine("Item " + itemId + ": " + quantity + " - " + quantityRequirement + " = " + quantityLeft + " for user: " + user2.properties.get("dbId"));
         if (quantityLeft > 0) {
            this.world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ?", quantityLeft, charItemId);
         } else {
            if (quantityLeft < 0) {
               this.world.db.jdbc.rollbackTransaction();
               this.world.users.log(user, "Suspicous TurnIn  [TradeDeal]", "Quantity requirement for turning in item is lacking.");
               return false;
            }
            this.world.db.jdbc.run("DELETE FROM users_items WHERE id = ?", charItemId);
            SmartFoxServer.log.fine("Delete item " + itemId + " for user: " + user2.properties.get("dbId"));
         }
      }
      return true;
   }
}
