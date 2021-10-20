package nereus.requests;

import nereus.db.objects.Enhancement;
import nereus.db.objects.House;
import nereus.db.objects.Item;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Map;
import jdbchelper.JdbcException;
import jdbchelper.NoResultException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

public class EquipItem implements IRequest {
   public EquipItem() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      int itemId = Integer.parseInt(params[0]);
      JSONObject cac;
      Item item = (Item)world.items.get(Integer.valueOf(itemId));
      if(item.isStaff() && !user.isAdmin() && !user.isModerator()) {
         world.db.jdbc.run("UPDATE users SET Access = 0, PermamuteFlag = 0 WHERE id = ?", new Object[]{user.properties.get(Users.DATABASE_ID)});
         world.users.kick(user);
         world.users.log(user, "Packet Edit [EquipItem]", "Banned for item id exploit.");
      }

      if(item.isUpgrade() && ((Integer)user.properties.get(Users.UPGRADE_DAYS)).intValue() < 0) {
         world.users.log(user, "Packet Edit [EquipItem]", "Attempted to equip member-only item.");
         throw new RequestException("Upgrade is required!");
      } else {
         if(item.getFactionId() > 1) {
            Map je = (Map)user.properties.get("factions");
            if(!je.containsKey(Integer.valueOf(item.getFactionId()))) {
               cac = new JSONObject();
               cac.put("cmd", "popupmsg"); //popupmsg
               cac.put("strMsg", "Reputation requirement not met!");
               cac.put("strGlow", "red,medium");
               cac.put("bitSuccess", 0);
               world.send(cac, user);
               return;
            }
            if(((Integer)je.get(Integer.valueOf(item.getFactionId()))).intValue() < item.getReqReputation()) {
               cac = new JSONObject();
               cac.put("cmd", "popupmsg"); //popupmsg
               cac.put("strMsg", "Reputation requirement not met!");
               cac.put("strGlow", "red,medium");
               cac.put("bitSuccess", 0);
               world.send(cac, user);
               return;
            }
         }

         world.db.jdbc.beginTransaction();

         try {
            QueryResult var28 = world.db.jdbc.query("SELECT EnhID, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", new Object[]{Integer.valueOf(itemId), user.properties.get(Users.DATABASE_ID)});
            if(var28.next()) {
               int quantity = var28.getInt("Quantity");
               Enhancement enhancement = world.enhancements.get(Integer.valueOf(var28.getInt("EnhID")));
               JSONObject eqp = (JSONObject)user.properties.get(Users.EQUIPMENT);
               JSONObject eqpObj;
               if(eqp.has(item.getEquipment())) {
                  eqpObj = eqp.getJSONObject(item.getEquipment());
                  int ei = eqpObj.getInt("ItemID");
                  world.db.jdbc.run("UPDATE users_items SET Equipped = 0 WHERE ItemID = ? AND UserID = ?", new Object[]{Integer.valueOf(ei), user.properties.get(Users.DATABASE_ID)});
               }

               world.db.jdbc.run("UPDATE users_items SET Equipped = 1 WHERE ItemID = ? AND UserID = ?", new Object[]{Integer.valueOf(itemId), user.properties.get(Users.DATABASE_ID)});
               eqpObj = new JSONObject();
               JSONObject var29 = new JSONObject();
               var29.put("uid", Integer.valueOf(user.getUserId()));
               var29.put("cmd", "equipItem");
               var29.put("ItemID", Integer.valueOf(itemId));
               var29.put("strES", item.getEquipment());
               var29.put("sFile", item.getFile());
               var29.put("sLink", item.getLink());
               var29.put("sMeta", item.getMeta());
               eqpObj.put("ItemID", Integer.valueOf(itemId));
               eqpObj.put("sFile", item.getFile());
               eqpObj.put("sLink", item.getLink());
               if(item.getEquipment().equals("Weapon")) {
                  var29.put("sType", item.getType());
                  eqpObj.put("sType", item.getType());
                  user.properties.put("weaponitem", item);
                  user.properties.put("weaponitemenhancement", enhancement);
                  JSONObject houseId = eqp.getJSONObject("ar");
                  world.users.loadSkills(user, (Item)world.items.get(houseId.getInt("ItemID")), (Integer) user.properties.get(Users.CLASS_POINTS));
               }

               eqp.put(item.getEquipment(), eqpObj);
               world.sendToRoom(var29, user, room);
               if(item.getEquipment().equals("ar") || item.getEquipment().equals("ba") || item.getEquipment().equals("he") || item.getEquipment().equals("Weapon")) {
                  if(item.getEquipment().equals("ar")) {
                     world.users.updateClass(user, item, quantity);
                  }

                  world.users.updateStats(user, enhancement, item.getEquipment());
                  world.users.sendStats(user);
               }

               if(item.getEquipment().equals("ho")) {
                  world.db.jdbc.run("UPDATE users SET HouseInfo = \'\' WHERE id = ?", new Object[]{user.properties.get(Users.DATABASE_ID)});
                  String var30 = "house-" + user.properties.get(Users.DATABASE_ID);
                  Room house = world.zone.getRoomByName(var30);
                  if(house != null) {
                     House houseObj = (House)world.areas.get("house-" + user.properties.get(Users.DATABASE_ID));
                     houseObj.setHouseInfo("");
                     houseObj.setFile(item.getFile());
                     User[] arrUsers = house.getAllUsers();
                     User[] arr$ = arrUsers;
                     int len$ = arrUsers.length;

                     for(int i$ = 0; i$ < len$; ++i$) {
                        User playerInRoom = arr$[i$];
                        world.send(new String[]{"server", "The map \"house-" + user.properties.get(Users.DATABASE_ID) + "\" is being rebuilt. You may join again in a few moments."}, playerInRoom);
                        world.rooms.basicRoomJoin(user, "faroff");
                     }
                  }
               }
            }

            var28.close();
         } catch (NoResultException var25) {
            if(world.db.jdbc.isInTransaction()) {
               world.db.jdbc.rollbackTransaction();
            }

            SmartFoxServer.log.severe("Error in equip item: " + var25.getMessage());
         } catch (JdbcException var26) {
            if(world.db.jdbc.isInTransaction()) {
               world.db.jdbc.rollbackTransaction();
            }

            SmartFoxServer.log.severe("Error in equip item transaction: " + var26.getMessage());
         } finally {
            if(world.db.jdbc.isInTransaction()) {
               world.db.jdbc.commitTransaction();
            }

         }

      }
   }
}
