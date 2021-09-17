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
import net.sf.json.JSONObject;

/**
 *
 * @author Rin
 */
public class RetrieveAuctionItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int userId = (Integer) user.properties.get(Users.DATABASE_ID);
        String auctionId = params[0];
        JSONObject sell = new JSONObject();
        sell.put("cmd", "retrieveAuctionItem"); //retrieveAuctionItem
        sell.put("bitSuccess", 0);
        sell.put("CharItemID", -1);
        world.db.jdbc.beginTransaction();

        try {
            QueryResult auctionResult = world.db.jdbc.query("SELECT * FROM users_markets WHERE id = ?", auctionId);
            if (auctionResult.next()) {
                int inventoryCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment NOT IN ('ho','hi') AND Bank = 0 AND UserID = ? AND ItemID NOT IN (" + auctionResult.getInt("ItemID") + ")", user.properties.get(Users.DATABASE_ID));
                if (inventoryCount < (Integer) user.properties.get(Users.SLOTS_BAG)) {
                    if (userId == auctionResult.getInt("UserID")) {
                        if (auctionResult.getInt("Status") == 0) {
                            sell.put("AuctionID", auctionResult.getInt("id"));
                            JSONObject tr;
                            if (auctionResult.getInt("BuyerID") > 0) {
                                int gold = auctionResult.getInt("Gold") + world.db.jdbc.queryForInt("SELECT Gold FROM users WHERE id = ?", userId);
                                int coins = auctionResult.getInt("Coins") + world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ?", userId);
                                if (auctionResult.getInt("Gold") > 0 && gold > Users.GOLD_MAX) {
                                    sell.put("strMessage", "You have already reached maximum amount of gold!");
                                } else if (auctionResult.getInt("Coins") > 0 && coins > Users.COINS_MAX) {
                                    sell.put("strMessage", "You have already reached maximum amount of coins!");
                                } else {
                                    tr = new JSONObject();
                                    tr.put("cmd", "updateGoldCoins"); //updateGoldCoins
                                    tr.put("intGold", gold);
                                    tr.put("intCoins", coins);
                                    world.send(tr, user);
                                    world.db.jdbc.run("UPDATE users SET Coins = ?, Gold = ? WHERE id = ?", coins, gold, user.properties.get(Users.DATABASE_ID));
                                    world.db.jdbc.run("UPDATE users_markets SET Status = 1 WHERE id = ?", auctionId);
                                    sell.put("bitSuccess", 1);
                                }
                            } else {
                                Enhancement enhancement = (Enhancement) world.enhancements.get(auctionResult.getInt("EnhID"));
                                Item ItemObj = (Item) world.items.get(auctionResult.getInt("ItemID"));
                                tr = Item.getItemJSON(ItemObj, enhancement);
                                tr.put("iQty", auctionResult.getInt("Quantity"));
                                tr.put("iReqCP", ItemObj.getReqClassPoints());
                                tr.put("iReqRep", ItemObj.getReqReputation());
                                tr.put("FactionID", ItemObj.getFactionId());
                                tr.put("sFaction", world.factions.get(ItemObj.getFactionId()));
                                QueryResult UserItem = world.db.jdbc.query("SELECT id FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0", ItemObj.getId(), (Integer) user.properties.get(Users.DATABASE_ID));
                                if (UserItem.next()) {
                                    sell.put("CharItemID", UserItem.getInt("id"));
                                    if (ItemObj.getStack() > 1) {
                                        int quantity = world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE id = ? FOR UPDATE", UserItem.getInt("id"));
                                        if (quantity >= ItemObj.getStack()) {
                                            world.db.jdbc.rollbackTransaction();
                                            sell.put("strMessage", "You cannot have more than " + ItemObj.getStack() + " of that item!");
                                            world.send(sell, user);
                                            auctionResult.close();
                                            UserItem.close();
                                            return;
                                        }

                                        world.db.jdbc.run("INSERT INTO users_markets_logs (OwnerID, BuyerID, Coins, Gold, ItemID, EnhID, Quantity, Type) VALUES (?, NULL, ?, ?, ?, ?, ?, 'Retrieve')", (Integer) user.properties.get(Users.DATABASE_ID), auctionResult.getInt("Coins"), auctionResult.getInt("Gold"), ItemObj.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                        world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ?", quantity + auctionResult.getInt("Quantity"), UserItem.getInt("id"));
                                    } else if (ItemObj.getStack() == 1) {
                                        world.db.jdbc.rollbackTransaction();
                                        sell.put("strMessage", "You cannot have more than " + ItemObj.getStack() + " of that item!");
                                        world.send(sell, user);
                                        auctionResult.close();
                                        UserItem.close();
                                        return;
                                    }
                                } else {
                                    world.db.jdbc.run("INSERT INTO users_markets_logs (OwnerID, BuyerID, Coins, Gold, ItemID, EnhID, Quantity, Type) VALUES (?, NULL, ?, ?, ?, ?, ?, 'Retrieve')", (Integer) user.properties.get(Users.DATABASE_ID), auctionResult.getInt("Coins"), auctionResult.getInt("Gold"), ItemObj.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                    world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank) VALUES (?, ?, ?, 0, ?, 0)", (Integer) user.properties.get(Users.DATABASE_ID), ItemObj.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                    sell.put("CharItemID", Long.valueOf(world.db.jdbc.getLastInsertId()).intValue());
                                }

                                sell.put("item", tr);
                                sell.put("bitSuccess", 1);
                                auctionResult.close();
                                UserItem.close();
                                world.db.jdbc.run("UPDATE users_markets SET Status = 1 WHERE id = ?", auctionId);
                            }
                        } else {
                            sell.put("strMessage", "You have already claimed this auction item or reward.");
                        }
                    } else {
                        sell.put("strMessage", "Error[2]: Invalid Auction ID!");
                    }
                } else {
                    sell.put("strMessage", "Inventory Full!");
                }
            } else {
                sell.put("strMessage", "Error[1]: Invalid Auction ID!");
            }

            auctionResult.close();
        } catch (JdbcException var18) {
            if (world.db.jdbc.isInTransaction()) {
                world.db.jdbc.rollbackTransaction();
            }

            //SmartFoxServer.log.severe("Error in retrieve item transaction: " + var18.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction()) {
                world.db.jdbc.commitTransaction();
            }

        }

        world.send(sell, user);
        //SmartFoxServer.log.fine(sell.toString());
    }
}