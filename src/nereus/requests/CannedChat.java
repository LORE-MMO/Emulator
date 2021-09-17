package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class CannedChat implements IRequest {
   public CannedChat() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      world.sendToRoom(new String[]{"cc", params[0], user.getName()}, user, room);
   }
}
