package nereus.requests.monster;

import nereus.db.objects.Enhancement;
import nereus.db.objects.Item;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Map;
import java.util.Queue;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

public class DenyDrop implements IRequest
{
    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException
    {
        Map<Integer, Integer> drops = (Map<Integer, Integer>) user.properties.get(Users.DROPS);

        int itemId = Integer.parseInt(params[0]);

        JSONObject gd = new JSONObject();
        gd.put("cmd", "ruwqopasldkdj"); //denyDrop
        gd.put("ItemID", itemId);
        gd.put("bSuccess", "1");
        world.send(gd, user);

        drops.remove(itemId);
    }
}
