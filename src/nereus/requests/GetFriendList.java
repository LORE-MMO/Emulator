package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

public class GetFriendList implements IRequest {
   public GetFriendList() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      JSONObject friends = new JSONObject();
      friends.put("cmd", "friends");
      friends.put("friends", world.users.getFriends(user));
      world.send(friends, user);
   }
}
