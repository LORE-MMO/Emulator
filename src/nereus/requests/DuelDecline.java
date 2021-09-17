package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;

public class DuelDecline implements IRequest {
   public DuelDecline() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      User client = world.zone.getUserByName(params[0].toLowerCase());
      if(client != null) {
         Set requestedDuel = (Set) user.properties.get(Users.REQUESTED_DUEL);
         requestedDuel.remove(Integer.valueOf(client.getUserId()));
         world.send(new String[]{"server", user.getName() + " declined your duel challenge."}, client);
      }
   }
}
