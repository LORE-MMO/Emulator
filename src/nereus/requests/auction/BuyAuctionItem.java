//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nereus.requests.auction;

import nereus.config.ConfigData;
import nereus.db.objects.Item;
import nereus.discord.Webhook;
import nereus.discord.Webhook.EmbedObject;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.awt.Color;
import java.io.IOException;
import java.util.Map;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

public class BuyAuctionItem implements IRequest {
    public BuyAuctionItem() {
    }

    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject buy = new JSONObject();
        buy.put("cmd", "buyAuctionItem");
        buy.put("bitSuccess", 0);
        buy.put("CharItemID", -1);
        int auctionId = Integer.parseInt(params[0]);
        world.db.jdbc.beginTransaction();

        try {
            QueryResult auctionResult = world.db.jdbc.query("SELECT *, CONCAT(HOUR(TIMEDIFF(NOW(), DATETIME)), ':', MINUTE(TIMEDIFF(NOW(), DATETIME)), ':', SECOND(TIMEDIFF(NOW(), DATETIME))) AS TIMEDIFF  FROM users_auctions WHERE id = ?", new Object[]{auctionId});
            if (auctionResult.next()) {
                Item item = (Item)world.items.get(auctionResult.getInt("ItemID"));
                String UserName = world.db.jdbc.queryForString("SELECT Name FROM users WHERE id = ?", new Object[]{auctionResult.getInt("CharacterID")});
                world.zone.getUserByName(UserName);
                if (item.getFactionId() > 1) {
                    Map<Integer, Integer> factions = (Map)user.properties.get("factions");
                    if (!factions.containsKey(item.getFactionId())) {
                        world.db.jdbc.rollbackTransaction();
                        buy.put("strMessage", "Reputation requirement not met! (" + item.getFactionId() + "/" + item.getReqReputation() + ")");
                        world.send(buy, user);
                        auctionResult.close();
                        return;
                    }

                    if ((Integer)factions.get(item.getFactionId()) < item.getReqReputation()) {
                        world.db.jdbc.rollbackTransaction();
                        buy.put("strMessage", "Reputation requirement not met! (" + item.getFactionId() + "/" + item.getReqReputation() + ")");
                        world.send(buy, user);
                        auctionResult.close();
                        return;
                    }
                }

                int inventoryCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment NOT IN ('ho','hi') AND Bank = 0 AND UserID = ? AND ItemID NOT IN (?)", new Object[]{user.properties.get("dbId"), item.getId()});
                int sellerAccess = world.db.jdbc.queryForInt("SELECT Access FROM users WHERE id = ?", new Object[]{auctionResult.getInt("CharacterID")});
                if (item.isUpgrade() && (Integer)user.properties.get("upgdays") < 0) {
                    buy.put("strMessage", "This item is member only!");
                } else if (item.getLevel() > (Integer)user.properties.get("level")) {
                    buy.put("strMessage", "Level requirement not met!");
                } else if ((!item.isStaff() || user.isAdmin() || user.isModerator()) && (sellerAccess < 40 || user.isAdmin() || user.isModerator())) {
                    if ((user.isAdmin() || user.isModerator()) && sellerAccess < 40) {
                        buy.put("strMessage", "Staff restriction: unable to buy player items!");
                    } else if (inventoryCount >= (Integer)user.properties.get("bagslots")) {
                        buy.put("strMessage", "Inventory Full!");
                    } else if (auctionResult.getInt("CharacterID") == (Integer)user.properties.get("dbId")) {
                        buy.put("strMessage", "You cannot buy your own item!");
                    } else if (auctionResult.getInt("BuyerID") <= 0 && auctionResult.getInt("Status") != 1 && Integer.parseInt(auctionResult.getString("TIMEDIFF").split(":")[0]) < 24) {
                        QueryResult userResult = world.db.jdbc.query("SELECT Gold, Coins FROM users WHERE id = ? FOR UPDATE", new Object[]{(Integer)user.properties.get("dbId")});
                        if (userResult.next()) {
                            int coins = userResult.getInt("Coins");
                            int gold = userResult.getInt("Gold");
                            userResult.close();
                            if (gold >= auctionResult.getInt("Gold") && coins >= auctionResult.getInt("Coins")) {
                                QueryResult itemResult = world.db.jdbc.query("SELECT id FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0", new Object[]{item.getId(), (Integer)user.properties.get("dbId")});
                                int charItemId;
                                int quantity;
                                if (itemResult.next()) {
                                    charItemId = itemResult.getInt("id");
                                    itemResult.close();
                                    if (item.getStack() > 1) {
                                        quantity = world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE id = ? FOR UPDATE", new Object[]{charItemId});
                                        if (quantity + item.getQuantity() > item.getStack()) {
                                            world.db.jdbc.rollbackTransaction();
                                            buy.put("strMessage", "You cannot have more than " + item.getStack() + " of that item!");
                                            world.send(buy, user);
                                            itemResult.close();
                                            auctionResult.close();
                                            userResult.close();
                                            return;
                                        }

                                        world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ?", new Object[]{quantity + auctionResult.getInt("Quantity"), charItemId});
                                    } else if (item.getStack() == 1) {
                                        world.db.jdbc.rollbackTransaction();
                                        buy.put("strMessage", "You cannot have more than " + item.getStack() + " of that item!");
                                        world.send(buy, user);
                                        itemResult.close();
                                        auctionResult.close();
                                        userResult.close();
                                        return;
                                    }
                                } else {
                                    itemResult.close();
                                    world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, '0', ?, '0', NOW())", new Object[]{(Integer)user.properties.get("dbId"), item.getId(), auctionResult.getInt("Quantity")});
                                    charItemId = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
                                }

                                if (charItemId > 0) {
                                    quantity = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ?", new Object[]{auctionResult.getInt("CharacterID")});
                                    int sellergolds = world.db.jdbc.queryForInt("SELECT Gold FROM users WHERE id = ?", new Object[]{auctionResult.getInt("CharacterID")});
                                    world.db.jdbc.run("UPDATE users SET Gold = ?, Coins = ? WHERE id = ?", new Object[]{gold - auctionResult.getInt("Gold"), coins - auctionResult.getInt("Coins"), user.properties.get("dbId")});
                                    world.db.jdbc.run("UPDATE users SET Gold = ?, Coins = ? WHERE id = ?", new Object[]{sellergolds + auctionResult.getInt("Gold"), quantity + auctionResult.getInt("Coins"), auctionResult.getInt("CharacterID")});
                                    world.db.jdbc.run("UPDATE users_auctions SET BuyerID = ?, Status = 0 WHERE id = ?", new Object[]{user.properties.get("dbId"), auctionId});
                                    JSONObject itemObj = Item.getItemJSON(item);
                                    itemObj.put("CharItemID", charItemId);
                                    itemObj.put("AuctionID", auctionResult.getInt("id"));
                                    itemObj.put("iQty", auctionResult.getInt("Quantity"));
                                    itemObj.put("iReqCP", item.getReqClassPoints());
                                    itemObj.put("iReqRep", item.getReqReputation());
                                    itemObj.put("FactionID", item.getFactionId());
                                    itemObj.put("sFaction", world.factions.get(item.getFactionId()));
                                    buy.put("bitSuccess", 1);
                                    buy.put("CharItemID", charItemId);
                                    buy.put("item", itemObj);
                                    JSONObject tr = new JSONObject();
                                    tr.put("cmd", "updateGoldCoins");
                                    tr.put("gold", gold - auctionResult.getInt("Gold"));
                                    tr.put("coins", coins - auctionResult.getInt("Coins"));
                                    world.send(tr, user);
                                } else {
                                    world.db.jdbc.rollbackTransaction();
                                    buy.put("strMessage", "An error occured while purchasing the item!");
                                }

                                itemResult.close();
                            } else {
                                buy.put("strMessage", "Insufficient funds!");
                            }
                        } else {
                            buy.put("strMessage", "ERROR: USER ID COULD NOT BE FOUND!");
                        }

                        userResult.close();
                    } else {
                        buy.put("strMessage", item.getName() + " is no longer in stock.");
                    }
                } else {
                    buy.put("strMessage", "Test Item: Cannot be purchased yet!");
                }
            } else {
                buy.put("strMessage", "Vending listing could not be found.");
            }

            auctionResult.close();
        } catch (JdbcException var28) {
            if (world.db.jdbc.isInTransaction()) {
                world.db.jdbc.rollbackTransaction();
            }

            SmartFoxServer.log.severe("Error in buy item transaction: " + var28.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction()) {
                world.db.jdbc.commitTransaction();
            }

        }

        world.send(buy, user);
        SmartFoxServer.log.fine(buy.toString());
    }
}
