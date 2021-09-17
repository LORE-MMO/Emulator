package nereus.tasks;

import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

import java.util.Set;

public class WarpUser implements Runnable {
   private Set<User> users;
   private World world;

   public WarpUser(World world, Set<User> users) {
      this.users = users;
      this.world = world;
   }

   @Override
   public void run() {
      for (User user : users) {
         String lastArea = (String) user.properties.get(Users.LAST_AREA);
         String[] arrLastArea = lastArea.split("\\|");

//         String spawnPoint = (String)user.properties.get(Users.SPAWN_POINT);
//         String[] arrSpawnArea = spawnPoint.split("\\|");

         Room room = world.rooms.lookForRoom(arrLastArea[0], user);
         if (room.contains(user.getName())){
//            this.world.rooms.basicRoomJoin(user, arrSpawnArea[0], arrSpawnArea[1], arrSpawnArea[2]);
            this.world.rooms.basicRoomJoin(user, arrLastArea[0], arrLastArea[1], arrLastArea[2]);
         } else {
            this.world.rooms.basicRoomJoin(user, arrLastArea[0], arrLastArea[1], arrLastArea[2]);
         }
      }
//      Iterator i$ = this.users.iterator();
//
//      while(i$.hasNext()) {
//         User user = (User)i$.next();
//         this.world.rooms.basicRoomJoin(user, "hometree");
//      }
   }
}
