package nereus.requests;

import nereus.ai.MonsterAI;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Rooms;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AggroMonster implements IRequest {
   @Override
   public void process(String[] params, User user, World world, Room room) throws RequestException {
      if (user == null) return;

      int aggroMon = 0;

      Map<Integer, MonsterAI> monsters = (ConcurrentHashMap<Integer, MonsterAI>) room.properties.get(Rooms.MONSTERS);
      while(aggroMon < params.length) {
         MonsterAI ai = monsters.get(Integer.parseInt(params[aggroMon]));

         if (ai == null) return;

         ai.addTarget(user.getUserId());
         if (ai.getState() == 1)
            ai.setAttacking(world.scheduleTask(ai, 2500, TimeUnit.MILLISECONDS, true));

         aggroMon++;
      }
   }
//   public void process(String[] params, User user, World world, Room room) throws RequestException {
//      if(user != null) {
//         ConcurrentHashMap monsters = (ConcurrentHashMap)room.properties.get("monsters");
//         MonsterAI ai = (MonsterAI)monsters.get(Integer.valueOf(Integer.parseInt(params[0])));
//         if(ai != null) {
//            ai.addTarget(user.getUserId());
//            if(ai.getState() == 1) {
//               ai.setAttacking(world.scheduleTask(ai, 2500L, TimeUnit.MILLISECONDS, true));
//            }
//
//         }
//      }
//   }
}
