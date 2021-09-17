package nereus.requests;

import nereus.aqw.Settings;
import nereus.db.objects.Area;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;
import net.sf.json.JSONObject;

public class DuelInvite implements IRequest {
   public DuelInvite() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      String username = params[0].toLowerCase();
      User client = world.zone.getUserByName(username);
      if(client == null) {
         throw new RequestException("Player \"" + username + "\" could not be found.");
      } else if(client.equals(user)) {
         throw new RequestException("You cannot challenge yourself to a duel!");
      } else if(!Settings.isAllowed("bDuel", user, client)) {
         throw new RequestException("Player \"" + username + "\" is not accepting duel invites.");
      } else if(((Integer)user.properties.get(Users.STATE)).intValue() == 2) {
         throw new RequestException(client.getName() + " is currently busy.");
      } else if((((Integer)client.properties.get(Users.LEVEL)).intValue() < 10) || (((Integer)user.properties.get(Users.LEVEL)).intValue() < 10)){
          throw new RequestException("You and "+client.getName()+" have to be higher than level 10 to duel.");
      } else {
         Room clientRoom = world.zone.getRoom(client.getRoom());
         Area area = world.areas.get(room.getName().split("-")[0]);
         Area clientArea = world.areas.get(clientRoom.getName().split("-")[0]);
         if(area != null && area.isPvP()) {
            throw new RequestException("Cannot initiate duel while in battlefield.");
         } else if(clientArea != null && clientArea.isPvP()) {
            throw new RequestException(client.getName() + " is currently busy.");
         } else {
            Set requestedDuel = (Set)client.properties.get(Users.REQUESTED_DUEL);
            requestedDuel.add(Integer.valueOf(user.getUserId()));
            JSONObject di = new JSONObject();
            di.put("owner", user.getName());
            di.put("cmd", "di");
            world.send(di, client);
            world.send(new String[]{"server", "You have challenged " + username + " to a duel."}, user);
         }
      }
   }
}
