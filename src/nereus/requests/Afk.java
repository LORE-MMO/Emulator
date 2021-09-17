package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class Afk implements IRequest {
   public Afk() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      boolean afk = Boolean.parseBoolean(params[0]);
      if(afk != ((Boolean)user.properties.get("afk")).booleanValue()) {
         if(!afk) {
            world.send(new String[]{"server", "You are no longer Away From Keyboard (AFK)."}, user);
         } else {
            world.send(new String[]{"server", "You are now Away From Keyboard (AFK)."}, user);
         }

         user.properties.put("afk", Boolean.valueOf(afk));
         world.send(new String[]{"uotls", user.getName(), "afk:" + Boolean.parseBoolean(params[0])}, room.getChannellList());
      }

   }
}
