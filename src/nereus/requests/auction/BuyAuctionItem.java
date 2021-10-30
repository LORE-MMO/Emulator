/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package nereus.requests.auction;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import nereus.aqw.Rank;
import nereus.config.ConfigData;
import nereus.db.objects.Enhancement;
import nereus.db.objects.Item;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

import java.awt.*;
import java.util.Map;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Rin
 */
public class BuyAuctionItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject buy = new JSONObject();
        buy.put("cmd", "buyAuctionItem"); //buyAuctionItem
        buy.put("bitSuccess", 0);
        buy.put("CharItemID", -1);
        int auctionId = Integer.parseInt(params[0]);
        world.db.getJdbc().beginTransaction();

        try {
            QueryResult auctionResult = world.db.getJdbc().query("SELECT users_markets.*, users.username AS OWNER, CONCAT(HOUR(TIMEDIFF(NOW(), DATETIME)), ':', MINUTE(TIMEDIFF(NOW(), DATETIME)), ':', SECOND(TIMEDIFF(NOW(), DATETIME))) AS TIMEDIFF FROM users_markets JOIN users ON users.id = users_markets.UserID WHERE users_markets.id = ? FOR UPDATE", auctionId);
            if (auctionResult.next()) {
                int SellerID = auctionResult.getInt("UserID");
                Item item = world.items.get(auctionResult.getInt("ItemID"));
                Enhancement enhancement = world.enhancements.get(auctionResult.getInt("EnhID"));
                if (item.getFactionId() > 0) {
                    Map<Integer, Integer> factions = (Map) user.properties.get(Users.FACTIONS);
                    if (!factions.containsKey(item.getFactionId())) {
                        world.db.getJdbc().rollbackTransaction();
                        buy.put("strMessage", "Reputation requirement not met! (" + item.getFactionId() + "/" + item.getReqReputation() + ")");
                        world.send(buy, user);
                        auctionResult.close();
                        return;
                    }

                    if (factions.get(item.getFactionId()) < item.getReqReputation()) {
                        world.db.getJdbc().rollbackTransaction();
                        buy.put("strMessage", "Reputation requirement not met! (" + item.getFactionId() + "/" + item.getReqReputation() + ")");
                        world.send(buy, user);
                        auctionResult.close();
                        return;
                    }
                }

                int inventoryCount = world.db.getJdbc().queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment NOT IN ('ho','hi') AND Bank = 0 AND UserID = ? AND ItemID NOT IN (?)", user.properties.get(Users.DATABASE_ID), item.getId());
                int sellerAccess = world.db.getJdbc().queryForInt("SELECT Access FROM users WHERE id = ?", auctionResult.getInt("UserID"));
                if (item.isUpgrade() && (Integer) user.properties.get(Users.UPGRADE_DAYS) < 0) {
                    buy.put("strMessage", "This item is member only!");
                } else if (item.getLevel() > (Integer) user.properties.get(Users.LEVEL)) {
                    buy.put("strMessage", "Level requirement not met!");
                } else if (item.isStaff() && !user.isAdmin() && !user.isModerator() || sellerAccess >= 40 && !user.isAdmin() && !user.isModerator()) {
                    buy.put("strMessage", "Test Item: Cannot be purchased yet!");
                } else if ((user.isAdmin() || user.isModerator()) && sellerAccess < 40) {
                    buy.put("strMessage", "Staff restriction: unable to buy player items!");
                } else if (inventoryCount >= (Integer) user.properties.get(Users.SLOTS_BAG)) {
                    buy.put("strMessage", "Inventory Full!");
                } else if (auctionResult.getInt("UserID") == (Integer) user.properties.get(Users.DATABASE_ID)) {
                    buy.put("strMessage", "You cannot buy your own item! If you want to reclaim it, do checkout in `Retrieve` tab.");
                } else if (auctionResult.getInt("BuyerID") <= 0 && auctionResult.getInt("Status") != 1 && Integer.parseInt(auctionResult.getString("TIMEDIFF").split(":")[0]) < 24) {
                    QueryResult userResult = world.db.getJdbc().query("SELECT Gold, Coins FROM users WHERE id = ? FOR UPDATE", (Integer) user.properties.get(Users.DATABASE_ID));
                    if (userResult.next()) {
                        int coins = userResult.getInt("Coins");
                        int gold = userResult.getInt("Gold");
                        userResult.close();
                        if (gold >= auctionResult.getInt("Gold") && coins >= auctionResult.getInt("Coins")) {
                            QueryResult itemResult = world.db.getJdbc().query("SELECT id FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0", item.getId(), (Integer) user.properties.get(Users.DATABASE_ID));
                            //int charItemId = false;
                            int charItemId;
                            if (itemResult.next()) {
                                charItemId = itemResult.getInt("id");
                                itemResult.close();
                                if (item.getStack() > 1) {
                                    int quantity = world.db.getJdbc().queryForInt("SELECT Quantity FROM users_items WHERE id = ? FOR UPDATE", charItemId);
                                    if (quantity + item.getQuantity() > item.getStack()) {
                                        world.db.getJdbc().rollbackTransaction();
                                        buy.put("strMessage", "You cannot have more than " + item.getStack() + " of that item!");
                                        world.send(buy, user);
                                        auctionResult.close();
                                        return;
                                    }

                                    world.db.getJdbc().run("INSERT INTO users_markets_logs (OwnerID, BuyerID, Coins, Gold, ItemID, EnhID, Quantity, Type) VALUES (?, ?, ?, ?, ?, ?, ?, 'Buy')", SellerID, (Integer) user.properties.get(Users.DATABASE_ID), auctionResult.getInt("Coins"), auctionResult.getInt("Gold"), item.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                    world.db.getJdbc().run("UPDATE users_items SET Quantity = ? WHERE id = ?", quantity + auctionResult.getInt("Quantity"), charItemId);
                                } else if (item.getStack() == 1) {
                                    world.db.getJdbc().rollbackTransaction();
                                    buy.put("strMessage", "You cannot have more than " + item.getStack() + " of that item!");
                                    world.send(buy, user);
                                    auctionResult.close();
                                    return;
                                }
                            } else {
                                itemResult.close();
                                world.db.getJdbc().run("INSERT INTO users_markets_logs (OwnerID, BuyerID, Coins, Gold, ItemID, EnhID, Quantity, Type) VALUES (?, ?, ?, ?, ?, ?, ?, 'Buy')", SellerID, (Integer) user.properties.get(Users.DATABASE_ID), auctionResult.getInt("Coins"), auctionResult.getInt("Gold"), item.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                world.db.getJdbc().run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank) VALUES (?, ?, ?, 0, ?, 0)", (Integer) user.properties.get(Users.DATABASE_ID), item.getId(), auctionResult.getInt("EnhID"), auctionResult.getInt("Quantity"));
                                charItemId = Long.valueOf(world.db.getJdbc().getLastInsertId()).intValue();
                            }

                            if (charItemId > 0) {
                                world.db.getJdbc().run("UPDATE users SET Gold = ?, Coins = ? WHERE id = ?", gold - auctionResult.getInt("Gold"), coins - auctionResult.getInt("Coins"), user.properties.get(Users.DATABASE_ID));
                                world.db.getJdbc().run("UPDATE users_markets SET BuyerID = ?, Status = 0 WHERE id = ?", user.properties.get(Users.DATABASE_ID), auctionId);
                                JSONObject itemObj = Item.getItemJSON(item, enhancement);
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
                                tr.put("cmd", "updateGoldCoins"); //updateGoldCoins
                                tr.put("intGold", gold - auctionResult.getInt("Gold"));
                                tr.put("intCoins", coins - auctionResult.getInt("Coins"));
                                user.properties.put(Users.GOLD, gold - auctionResult.getInt("Gold"));
                                user.properties.put(Users.COINS, coins - auctionResult.getInt("Coins"));
                                world.send(tr, user);

                            } else {
                                world.db.getJdbc().rollbackTransaction();
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
                buy.put("strMessage", "Auction listing could not be found.");
            }

            auctionResult.close();
        } catch (JdbcException var22) {
            if (world.db.getJdbc().isInTransaction()) {
                world.db.getJdbc().rollbackTransaction();
            }

            SmartFoxServer.log.severe("Error in buy item transaction: " + var22.getMessage());
        } finally {
            if (world.db.getJdbc().isInTransaction()) {
                world.db.getJdbc().commitTransaction();
            }

        }

        world.send(buy, user);
        SmartFoxServer.log.fine(buy.toString());
    }
}
