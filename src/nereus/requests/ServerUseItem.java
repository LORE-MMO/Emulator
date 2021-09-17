package nereus.requests;

import nereus.db.objects.Item;
import nereus.db.objects.Title;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

public class ServerUseItem implements IRequest {
   public ServerUseItem() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      String option = params[0];
      int minutes;
      if (option.equals("+")) {
         int itemid = Integer.parseInt(params[1]);
         Item boost = (Item) world.items.get(Integer.valueOf(itemid));
         if (boost != null && boost.getType().equals("ServerUse")) {
            if (boost.getLink().contains("::")) {
               String[] itemParams = boost.getLink().split("::");
               String type = itemParams[0];
               switch (type) {
                  case "title":
                     Title title = world.titles.get(Integer.valueOf(itemParams[1]));

                     int titleCount = world.db.getJdbc().queryForInt("SELECT COUNT(*) AS rowcount FROM users_titles WHERE TitleID = ? AND UserID = ?", title.getId(), user.properties.get(Users.DATABASE_ID));
                     if (titleCount >= 1) throw new RequestException("You already have this title.");

                     if (world.users.turnInItem(user, itemid, 1)) {
                        if (title != null) {
                           world.db.getJdbc().run("INSERT INTO users_titles (UserID, TitleID) VALUES (?, ?)", new Object[]{user.properties.get(Users.DATABASE_ID), Integer.valueOf(title.getId())});
                           JSONObject umsg = new JSONObject();
                           umsg.put("cmd", "popupmsg");
                           umsg.put("strMsg", "You just received \"" + title.getName() + "\" title!");
                           umsg.put("strGlow", "green,medium");
                           world.send(umsg, user);
                        }
                     }
                     break;
                  default:
                     minutes = Integer.parseInt(itemParams[1]);
                     boolean showShop = Boolean.parseBoolean(itemParams[2]);
                     if (world.users.turnInItem(user, itemid, 1)) {
                        JSONObject boost1 = new JSONObject();
                        boost1.put("cmd", type);
                        boost1.put("bShowShop", Boolean.valueOf(showShop));
                        boost1.put("op", option);
                        QueryResult boosts = world.db.getJdbc().query("SELECT ExpBoostExpire, CpBoostExpire, GoldBoostExpire, RepBoostExpire FROM users WHERE id = ?", new Object[]{user.properties.get(Users.DATABASE_ID)});
                        if (boosts.next()) {
                           int repMinLeft;
                           if (type.equals("xpboost")) {
                              repMinLeft = world.db.getJdbc().queryForInt("SELECT TIMESTAMPDIFF(MINUTE, NOW(), ?)", new Object[]{boosts.getString("ExpBoostExpire")});
                              repMinLeft = repMinLeft >= 0 ? repMinLeft : 0;
                              world.db.getJdbc().run("UPDATE users SET ExpBoostExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", new Object[]{Integer.valueOf(minutes + repMinLeft), user.properties.get(Users.DATABASE_ID)});
                              user.properties.put(Users.BOOST_XP, Boolean.valueOf(true));
                              boost1.put("iSecsLeft", Integer.valueOf((minutes + repMinLeft) * 60));
                           } else if (type.equals("gboost")) {
                              repMinLeft = world.db.getJdbc().queryForInt("SELECT TIMESTAMPDIFF(MINUTE,NOW(),?)", new Object[]{boosts.getString("GoldBoostExpire")});
                              repMinLeft = repMinLeft >= 0 ? repMinLeft : 0;
                              world.db.getJdbc().run("UPDATE users SET GoldBoostExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", new Object[]{Integer.valueOf(minutes + repMinLeft), user.properties.get(Users.DATABASE_ID)});
                              user.properties.put(Users.BOOST_GOLD, Boolean.valueOf(true));
                              boost1.put("iSecsLeft", Integer.valueOf((minutes + repMinLeft) * 60));
                           } else if (type.equals("cpboost")) {
                              repMinLeft = world.db.getJdbc().queryForInt("SELECT TIMESTAMPDIFF(MINUTE, NOW(), ?)", new Object[]{boosts.getString("CpBoostExpire")});
                              repMinLeft = repMinLeft >= 0 ? repMinLeft : 0;
                              world.db.getJdbc().run("UPDATE users SET CpBoostExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", new Object[]{Integer.valueOf(minutes + repMinLeft), user.properties.get(Users.DATABASE_ID)});
                              user.properties.put(Users.BOOST_CP, Boolean.valueOf(true));
                              boost1.put("iSecsLeft", Integer.valueOf((minutes + repMinLeft) * 60));
                           } else if (type.equals("repboost")) {
                              repMinLeft = world.db.getJdbc().queryForInt("SELECT TIMESTAMPDIFF(MINUTE, NOW() , ?)", new Object[]{boosts.getString("RepBoostExpire")});
                              repMinLeft = repMinLeft >= 0 ? repMinLeft : 0;
                              world.db.getJdbc().run("UPDATE users SET RepBoostExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", new Object[]{Integer.valueOf(minutes + repMinLeft), user.properties.get(Users.DATABASE_ID)});
                              user.properties.put(Users.BOOST_REP, Boolean.valueOf(true));
                              boost1.put("iSecsLeft", Integer.valueOf((minutes + repMinLeft) * 60));
                           }

                           world.send(boost1, user);
                        }

                        boosts.close();
                     } else {
                        world.users.log(user, "Suspicious Request [ServerUseItem]", "Failed to pass turn-in validation, might be a duplicate request.");
                     }
               }
            } else {
               throw new RequestException("This feature is not yet available.", "server");
            }

         }
      } else {
         String type2 = params[1];
         JSONObject boost2 = new JSONObject();
         boost2.put("cmd", type2);
         boost2.put("op", option);
         if(type2.equals("xpboost")) {
            user.properties.put(Users.BOOST_XP, Boolean.valueOf(false));
         } else if(type2.equals("gboost")) {
            user.properties.put(Users.BOOST_GOLD, Boolean.valueOf(false));
         } else if(type2.equals("cpboost")) {
            user.properties.put(Users.BOOST_CP, Boolean.valueOf(false));
         } else if(type2.equals("repboost")) {
            user.properties.put(Users.BOOST_REP, Boolean.valueOf(false));
         }

         world.send(boost2, user);
      }

   }
}
