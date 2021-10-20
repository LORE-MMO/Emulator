package nereus.requests.customfunctions;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import nereus.world.World;
import java.text.SimpleDateFormat;
import java.util.Date;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

public class RedeemCode implements IRequest
{
   @Override
   public void process(String[] params, User user, World world, Room room) throws RequestException {
      String code = params[0].trim().toLowerCase();
      QueryResult rs = world.db.jdbc.query("SELECT * FROM redeems WHERE Code = ? AND Enabled = 1", code);
      String Earned = " ";
      Boolean valid = false;
      if (rs.next()) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date Now = new Date();
         Date DateExpiry = new Date(dateFormat.format(rs.getTimestamp("DateExpiry")));
         int RedeemID = rs.getInt("id");
         int Coins = rs.getInt("Coins");
         int Gold = rs.getInt("Gold");
         int Exp = rs.getInt("Exp");
         int ClassPoints = rs.getInt("ClassPoints");
         int ItemID = rs.getInt("ItemID");
         int Quantity = rs.getInt("Quantity");
         int QuantityLeft = rs.getInt("QuantityLeft");
         boolean Limited = rs.getBoolean("Limited");
         boolean Expires = rs.getBoolean("Expires");
         rs.close();
         int redeemCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_redeems WHERE RedeemID = ? AND UserID = ?", RedeemID, user.properties.get("dbId"));
         if (redeemCount >= 1) {
            throw new RequestException("The code you are trying to redeemed is already redeemed on this character.");
         }

         if (Expires) {
            if (DateExpiry.getDate() <= Now.getDate()) {
               throw new RequestException("The code you're trying to redeem is already expired.");
            }
//            valid = true;
         }

         if (Limited) {
            if (QuantityLeft <= 0) {
               throw new RequestException("The code you're trying to redeem is out of stock.");
            }
            world.db.jdbc.run("UPDATE redeems SET QuantityLeft = (QuantityLeft - 1) WHERE id = ?", RedeemID);
         }

         valid = true;
         QueryResult itemResult = world.db.jdbc.query("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0", ItemID, user.properties.get("dbId"));
         if (itemResult.next()) {
            int quantityInInventory = itemResult.getInt("Quantity");
            itemResult.close();
            if (quantityInInventory >= world.items.get(ItemID).getStack()) {
               throw new RequestException("Item could not be redeemed! Please make sure your inventory is not full or the item is already present in your inventory/bank.");
            }
         }
         itemResult.close();
         if (!valid.booleanValue()) throw new RequestException("Invalid Redeem Code.");
         if (Coins > 0) {
            JSONObject KeKTopZ = new JSONObject();
            KeKTopZ.put("cmd", "sellItem");
            KeKTopZ.put("intAmount", Integer.valueOf(Coins));
            KeKTopZ.put("CharItemID", Integer.valueOf(user.hashCode()));
            KeKTopZ.put("bCoins", Integer.valueOf(1));
            world.send(KeKTopZ, user);
            world.db.jdbc.run("UPDATE users SET Coins = (Coins + ?) WHERE id=?", new Object[]{Integer.valueOf(Coins), user.properties.get("dbId")});
            Earned = Earned + Coins + " Coins ";
         }
         if (Exp > 0) {
            Earned = Earned + Exp + " XP ";
         }
         if (Gold > 0) {
            Earned = Earned + Gold + " Gold ";
         }
         if (ClassPoints > 0) {
            Earned = Earned + ClassPoints + " Class Points ";
         }
         if (ItemID > 1 && Quantity > 0) {
            world.users.dropItem(user, ItemID, Quantity);
            Earned = Earned + world.items.get(ItemID).getName() + " ";
         }
         world.users.giveRewards(user, Exp, Gold, 0, ClassPoints, 0, -1, user.getUserId(), "p");
         world.db.jdbc.beginTransaction();
         try {
            world.db.jdbc.run("INSERT INTO users_redeems (RedeemID, UserID) VALUES (?, ?)", RedeemID, user.properties.get("dbId"));
         }
         catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction()) {
               world.db.jdbc.rollbackTransaction();
            }
//            this.log.error("Error in redeem transaction: " + je.getMessage());
         }
         finally {
            if (world.db.jdbc.isInTransaction()) {
               world.db.jdbc.commitTransaction();
            }
         }
      } else {
         rs.close();
         throw new RequestException("Invalid Redeem Code.");
      }
      world.send(new String[]{"server", "You successfully earned " + Earned + "from the redeem code!"}, user);
      rs.close();
   }
}

