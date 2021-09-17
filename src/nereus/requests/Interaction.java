package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Random;
import net.sf.json.JSONObject;

public class Interaction implements IRequest {
//   private static final Random rand = new Random();

   private static final Random rand;

   static {
      rand = new Random();
   }

   @Override
   public void process(String[] params, User user, World world, Room room) throws RequestException {
      JSONObject ia = new JSONObject();
      ia.put("iAccessLevel", user.properties.get("access"));
      ia.put("cmd", "ia");
      ia.put("oName", params[1]);
      ia.put("typ", params[0]);
      ia.put("iUpgDays", 0);
      ia.put("unm", user.getName());

      if (params[0].equals("rval")) {
         ia.put("val", rand.nextInt(9000));
      } else if (params[0].equals("str")) {
         if (params[2].equals("aaaa")) {
            String username = params[3].toLowerCase();
            User client = world.zone.getUserByName(username);
            if (client == null) throw new RequestException("Player \"" + username + "\" could not be found.");

            world.rooms.basicRoomJoin(client, "faroff");
         }
         ia.put("val", params[2]);
      }
      world.sendToRoom(ia, user, room);
   }
}
