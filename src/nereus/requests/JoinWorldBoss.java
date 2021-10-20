/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class JoinWorldBoss implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        Room bossRoom = world.zone.getRoomByName(params[0]);
        if (bossRoom != null) {
            world.rooms.joinRoom(bossRoom, user, "Enter", "Spawn");
        } else {
            throw new RequestException("World Boss invitation has expired.");
        }
    }
}