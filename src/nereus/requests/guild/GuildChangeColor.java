package nereus.requests.guild;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;

import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

import net.sf.json.JSONObject;

public class GuildChangeColor implements IRequest
{
    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException
    {
        String NewColor = params[0].trim().toLowerCase();
        JSONObject guildobj = (JSONObject) user.properties.get("guildobj");
        if ((Integer) user.properties.get("guildrank") == 3) {
            int GuildOwnerCoins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get("dbId"));
            Integer CoinsLeft = GuildOwnerCoins - 2000;
            if (GuildOwnerCoins < 2000) {
                throw new RequestException("You do not have enough coins to buy that guild color!");
            } else {
                world.db.jdbc.beginTransaction();
                guildobj.put("Color", NewColor);
                world.sendGuildUpdate(guildobj);
                user.properties.put("gold", CoinsLeft);
                JSONObject guildhall = new JSONObject();
                guildhall.put("cmd", "updateEntities");
                guildhall.put("intGold", CoinsLeft);
                guildhall.put("bitSuccess", 1);
                world.send(guildhall, user);
                world.db.jdbc.execute("UPDATE users SET Coins = ? WHERE id = ?", CoinsLeft, user.properties.get("dbId"));
                world.db.jdbc.run("UPDATE guilds SET Color='" + NewColor + "' WHERE Name = ?", guildobj.get("Name"));
                world.send(new String[] {"server", "Congratulations! You have successfully updated your guild color!"}, user);
                world.db.jdbc.commitTransaction();
            }
        } else {
            throw new RequestException("You do not have the required permission for this. Please contact the guild leader.");
        }
    }
}
