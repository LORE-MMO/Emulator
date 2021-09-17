/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package nereus.requests.auction;

import nereus.db.objects.Enhancement;
import nereus.db.objects.Item;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Rin
 */
public class RetrieveAuctionItems implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int userId = (Integer) user.properties.get(Users.DATABASE_ID);
        int gold = world.db.jdbc.queryForInt("SELECT Gold FROM users WHERE id = ?", userId);
        int coins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ?", userId);
        int count = world.db.jdbc.queryForInt("SELECT COUNT(*) FROM users_markets WHERE UserID = ? AND Status = 0 AND Type = 'Auction'", userId);
        world.db.jdbc.beginTransaction();

        try {
            int inventoryCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment NOT IN ('ho','hi') AND Bank = 0 AND UserID = ? AND ItemID NOT IN (SELECT ItemID FROM users_markets WHERE UserID = ? AND Status = 0)", user.properties.get(Users.DATABASE_ID), user.properties.get(Users.DATABASE_ID));
            if (inventoryCount + count < (Integer) user.properties.get(Users.SLOTS_BAG)) {
                QueryResult auctionResult = world.db.jdbc.query("SELECT * FROM users_markets WHERE UserID = ? AND Status = 0 AND Type = 'Auction'", userId);
                JSONObject sell = new JSONObject();
                JSONArray items = new JSONArray();
                sell.put("cmd", "retrieveAuctionItems"); //retrieveAuctionItems
                sell.put("bitSuccess", 1);

                while (true) {
                    while (auctionResult.next()) {
                        Enhancement enhancement = (Enhancement) world.enhancements.get(auctionResult.getInt("EnhID"));
                        Item ItemObj = (Item) world.items.get(auctionResult.getInt("ItemID"));
                        JSONObject item = Item.getItemJSON(ItemObj, enhancement);
                        item.put("AuctionID", auctionResult.getInt("id"));
                        item.put("iQty", auctionResult.getInt("Quantity"));
                        item.put("iReqCP", ItemObj.getReqClassPoints());
                        item.put("iReqRep", ItemObj.getReqReputation());
                        item.put("FactionID", ItemObj.getFactionId());
                        item.put("sFaction", world.factions.get(ItemObj.getFactionId()));
                        item.put("bHouse", ItemObj.isHouse());
                        item.put("bBank", 0);
                        if (auctionResult.getInt("BuyerID") > 0) {
                            JSONObject warning;
                            if (auctionResult.getInt("Gold") > 0 && gold + auctionResult.getInt("Gold") > Users.GOLD_MAX) {
                                warning = new JSONObject();
                                warning.put("cmd", "retrieveAuctionItems"); //retrieveAuctionItems
                                warning.put("bitSuccess", 0);
                                warning.put("strMessage", "Maximum amount of gold limit reached!");
                                world.send(warning, user);
                                continue;
                            }

                            if (auctionResult.getInt("Coins") > 0 && coins + auctionResult.getInt("Coins") > Users.COINS_MAX) {
                                warning = new JSONObject();
                                warning.put("cmd", "retrieveAuctionItems"); //retrieveAuctionItems
                                warning.put("bitSuccess", 0);
                                warning.put("strMessage", "Maximum amount of coins limit reached!");
                                world.send(warning, user);
                                continue;
                            }

                            gold += auctionResult.getInt("Gold");
                            coins += auctionResult.getInt("Coins");
                            item.put("bSold", 1);
                        } else {
                            QueryResult UserItem = world.db.jdbc.query("SELECT id FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0", ItemObj.getId(), (Integer) user.properties.get(Users.DATABASE_ID));
                            if (UserItem.next()) {
                                int charItemId = UserItem.getInt("id");
                                item.put("CharItemID", charItemId);
                                UserItem.close();
                                if (ItemObj.getStack() > 1) {
                                    int quantity = world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE id = ? FOR UPDATE", charItemId);
                                    if (quantity >= ItemObj.getStack()) {
                                        JSONObject warning = new JSONObject();
                                        warning.put("cmd", "retrieveAuctionItems"); //retrieveAuctionItems
                                        warning.put("bitSuccess", 0);
                                        warning.put("strMessage", "You cannot have more than " + ItemObj.getStack() + " of " + ItemObj.getName() + "!");
                                        world.send(warning, user);
                                        continue;
                                    }

                                    world.db.jdbc.run("INSERT INTO users_markets_logs (OwnerID, BuyerID, Coins, Gold, ItemID, EnhID, Quantity, Type) VALUES (?, NULL, ?, ?, ?, ?, ?, 'Retrieve')", (Integer) user.properties.get(Users.DATABASE_ID), coins, gold, ItemObj.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                    world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ?", quantity + auctionResult.getInt("Quantity"), charItemId);
                                } else if (ItemObj.getStack() == 1) {
                                    JSONObject warning = new JSONObject();
                                    warning.put("cmd", "retrieveAuctionItems"); //retrieveAuctionItems
                                    warning.put("bitSuccess", 0);
                                    warning.put("strMessage", "You cannot have more than " + ItemObj.getStack() + " of " + ItemObj.getName() + "!");
                                    world.send(warning, user);
                                    continue;
                                }
                            } else {
                                world.db.jdbc.run("INSERT INTO users_markets_logs (OwnerID, BuyerID, Coins, Gold, ItemID, EnhID, Quantity, Type) VALUES (?, NULL, ?, ?, ?, ?, ?, 'Retrieve')", (Integer) user.properties.get(Users.DATABASE_ID), coins, gold, ItemObj.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, '1990-01-01 01:00:00')", (Integer) user.properties.get(Users.DATABASE_ID), ItemObj.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                item.put("CharItemID", Long.valueOf(world.db.jdbc.getLastInsertId()).intValue());
                            }

                            UserItem.close();
                        }

                        items.add(item);
                        world.db.jdbc.run("UPDATE users_markets SET Status = 1 WHERE id = ? AND Type = 'Auction'", auctionResult.getInt("id"));
                    }

                    auctionResult.close();
                    JSONObject tr = new JSONObject();
                    tr.put("cmd", "updateGoldCoins"); //updateGoldCoins
                    tr.put("intGold", gold);
                    tr.put("intCoins", coins);
//                    tr.put("intAmount", Integer.valueOf(var15));
//                    tr.put("CharItemID", Integer.valueOf(user.hashCode()));
                    tr.put("bCoins", Integer.valueOf(1));
                    world.send(tr, user);

                    sell.put("items", items);
                    world.send(sell, user);
                    //SmartFoxServer.log.fine(sell.toString());
                    world.db.jdbc.run("UPDATE users SET Coins = ?, Gold = ? WHERE id = ?", coins, gold, user.properties.get(Users.DATABASE_ID));
                    break;
                }
            } else {
                JSONObject warning = new JSONObject();
                warning.put("cmd", "retrieveAuctionItems"); //retrieveAuctionItems
                warning.put("bitSuccess", 0);
                warning.put("strMessage", "Inventory Full!");
                world.send(warning, user);
            }
        } catch (JdbcException var23) {
            if (world.db.jdbc.isInTransaction()) {
                world.db.jdbc.rollbackTransaction();
            }

            //SmartFoxServer.log.severe("Error in retrieve item transaction: " + var23.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction()) {
                world.db.jdbc.commitTransaction();
            }

        }

    }
}
