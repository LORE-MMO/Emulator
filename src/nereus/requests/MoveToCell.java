package nereus.requests;

import it.gotoandplay.smartfoxserver.SmartFoxServer;
import nereus.db.objects.Area;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

public class MoveToCell implements IRequest
{
   @Override
   public void process(String[] params, User user, World world, Room room) throws RequestException
   {
      String frame = params[0];
      String pad = params[1];

      user.properties.put(Users.FRAME, frame);
      user.properties.put(Users.PAD, pad);
      user.properties.put(Users.TX, 0);
      user.properties.put(Users.TY, 0);

      if ((Integer) user.properties.get("state") != 1) {
         world.users.regen(user);
         user.properties.put("state", 1);
         JSONObject sb = new JSONObject();
         JSONObject p = new JSONObject();
         JSONObject pInfo = new JSONObject();
         pInfo.put("intState", (Integer)user.properties.get("state"));
         p.put(user.getName(), pInfo);
         sb.put("cmd", "ct");
         sb.put("p", p);
         world.sendToRoom(sb, user, room);
      }

      StringBuilder sb1 = new StringBuilder();
      sb1.append("strPad:");
      sb1.append(user.properties.get("pad"));
      sb1.append(",tx:");
      sb1.append(user.properties.get("tx"));
      sb1.append(",strFrame:");
      sb1.append(user.properties.get("frame"));
      sb1.append(",ty:");
      sb1.append(user.properties.get("tx"));
      world.sendToRoomButOne(new String[]{"uotls", user.getName(), sb1.toString()}, user, room);

      Area area = world.areas.get(room.getName().split("-")[0]);
      if (area != null) {
         if (area.isFirstJoin()) user.properties.put(Users.LAST_AREA, room.getName().split("-")[0] + "|" + frame + "|" + pad);
      }
   }
}
