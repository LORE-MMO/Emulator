package nereus.requests;

import nereus.db.objects.Area;
import nereus.db.objects.Quest;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;

public class AcceptQuest implements IRequest {
   public AcceptQuest() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      Set quests = (Set)user.properties.get("quests");
      int questId = Integer.parseInt(params[0]);
      quests.add(Integer.valueOf(questId));
      Quest quest = (Quest)world.quests.get(Integer.valueOf(questId));
      if(!quest.locations.isEmpty()) {
         int mapId = ((Area)world.areas.get(room.getName().split("-")[0])).getId();
         if(!quest.locations.contains(Integer.valueOf(mapId))) {
            world.users.log(user, "Invalid Quest Accept", "Quest accept triggered at different location.");
         }
      }

   }
}
