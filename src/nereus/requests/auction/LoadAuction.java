/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package nereus.requests.auction;

import nereus.db.objects.Enhancement;
import nereus.db.objects.Item;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Arrays;
import java.util.List;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Rin
 */
public class LoadAuction implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        List<String> types = Arrays.asList(params);
        JSONObject lb = new JSONObject();
        JSONArray items = new JSONArray();
        //SmartFoxServer.log.fine("Request received! " + params + " TYPES: " + types);
        QueryResult result = world.db.jdbc.query("SELECT *, CONCAT(HOUR(TIMEDIFF(NOW(), DATETIME)), ':', MINUTE(TIMEDIFF(NOW(), DATETIME)), ':', SECOND(TIMEDIFF(NOW(), DATETIME))) AS TIMEDIFF FROM users_markets WHERE BuyerID IS NULL AND DATETIME >= NOW()-INTERVAL 1 DAY AND Status = 0");

        while (true) {
            int userId;
            int hour;
            int minute;
            int second;
            Item item;
            Enhancement enhancement;
            do {
                if (!result.next()) {
                    result.close();
                    lb.put("cmd", "loadAuction"); //loadAuction
                    lb.put("items", items);
                    lb.put("bitSuccess", 1);
                    //SmartFoxServer.log.fine(lb.toString());
                    world.send(lb, user);
                    return;
                }

                userId = result.getInt("UserID");
                String dateFormat = result.getString("TIMEDIFF");
                String[] hhmmss = dateFormat.split(":");
                hour = Integer.parseInt(hhmmss[0]);
                minute = Integer.parseInt(hhmmss[1]);
                second = Integer.parseInt(hhmmss[2]);
                item = world.items.get(result.getInt("ItemID"));
                enhancement = world.enhancements.get(result.getInt("EnhID"));
            } while (!types.contains(item.getType()) && !types.contains("All"));

            JSONObject itemObj = Item.getItemJSON(item, enhancement);
            itemObj.put("AuctionID", result.getString("id"));
            itemObj.put("iQty", result.getInt("Quantity"));
            itemObj.put("iReqCP", item.getReqClassPoints());
            itemObj.put("iReqRep", item.getReqReputation());
            itemObj.put("FactionID", item.getFactionId());
            itemObj.put("sFaction", world.factions.get(item.getFactionId()));
            itemObj.put("Player", world.db.jdbc.queryForString("SELECT username FROM users WHERE id = ?", userId));
            itemObj.put("Duration", String.format("%02d:%02d:%02d", hour, minute, second));
            itemObj.put("Gold", result.getInt("Gold"));
            itemObj.put("Coins", result.getInt("Coins"));
            items.add(itemObj);
        }
    }
}