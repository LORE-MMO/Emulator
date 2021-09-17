package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class DragonBuff implements IRequest {
   public DragonBuff() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      world.send(new String[]{"Dragon Buff"}, user);
   }
}
