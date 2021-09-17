package nereus.requests;

import nereus.aqw.Settings;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class Whisper implements IRequest {
   public Whisper() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      if(((Integer)user.properties.get("permamute")).intValue() > 0) {
         throw new RequestException("You are muted! Chat privileges have been permanently revoked.");
      } else if(world.users.isMute(user)) {
         int message1 = world.users.getMuteTimeInSeconds(user);
         throw new RequestException(world.users.getMuteMessage((double)message1));
      } else {
         String message = params[0];
         String username = params[1].toLowerCase();
         User client = world.zone.getUserByName(username);
         if(client == null) {
            throw new RequestException("Player \"" + username + "\" could not be found.");
         } else if(!Settings.isAllowed("bWhisper", user, client)) {
            throw new RequestException("Player " + client.getName() + " is not accepting PMs at this time.");
         } else {
            String[] msg = new String[]{"whisper", message, user.getName(), username, "0"};
            world.send(msg, user);
            world.send(msg, client);
            world.applyFloodFilter(user, message);
         }
      }
   }
}
