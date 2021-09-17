package nereus.requests.player;

import nereus.config.ConfigData;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.sf.json.JSONObject;

public class FirstJoin implements IRequest
{
    private static final List<String> exceptions = Arrays.asList("house", "deadlock");

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException
    {
        sendCoreValues(user, world);
        String lastArea = (String)user.properties.get(Users.LAST_AREA);
        String roomName = "faroff";
        String roomFrame = "Enter";
        String roomPad = "Spawn";

        if (!lastArea.isEmpty()) {
            boolean gotoLastArea = true;
            String[] arrLastArea = lastArea.split("\\|");
            String lastAreaName = arrLastArea[0];

            if (!world.areas.containsKey(lastAreaName) || (world.areas.get(lastAreaName).isStaff()) || !(world.areas.get(lastAreaName).isFirstJoin())) {
                gotoLastArea = false;
            }

            if (gotoLastArea) {
                roomName = arrLastArea[0];
                roomFrame = arrLastArea[1];
                roomPad = arrLastArea[2];
            }
        }

        world.rooms.basicRoomJoin(user, roomName, roomFrame, roomPad);
        world.db.jdbc.run("UPDATE servers SET Count = ? WHERE Name = ?", world.zone.getUserCount(), ConfigData.SERVER_NAME);
    }

    private void sendCoreValues(User user, World world)
    {
        JSONObject cvu = new JSONObject();
        JSONObject o = new JSONObject();
        if(world.coreValues == null) {
            throw new RuntimeException("CVU is null!");
        } else {
            Iterator i$ = world.coreValues.entrySet().iterator();

            while(i$.hasNext()) {
                Entry e = (Entry)i$.next();
                o.put(e.getKey(), e.getValue());
            }

            cvu.put("cmd", "cvu");
            cvu.put("o", o);
            world.send(cvu, user);
        }
    }
}
