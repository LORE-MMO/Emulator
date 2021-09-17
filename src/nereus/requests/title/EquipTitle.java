package nereus.requests.title;

import nereus.db.objects.Title;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

public class EquipTitle implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int titleId = Integer.parseInt(params[0]);

        if (titleId == (Integer) user.properties.get(Users.TITLE)) throw new RequestException("You already have this title equipped.");
        Title title = world.titles.get(titleId);
        if (title == null) throw new RequestException("Invalid Title.");

        int titleCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_titles WHERE TitleID = ? AND UserID = ?", titleId, user.properties.get(Users.DATABASE_ID));
        if (titleCount <= 0) throw new RequestException("Selected title is currently locked.");

        world.db.jdbc.run("UPDATE users SET Title = ? WHERE id = ?", titleId, user.properties.get(Users.DATABASE_ID));

        JSONObject utl = new JSONObject();
        utl.put("cmd", "equipTitle");
        utl.put("uid", user.getUserId());
        utl.put("title", title);
        utl.put("titleId", title.getId());
        utl.put("titleName", title.getName());
        utl.put("titleColor", title.getColor());
        utl.put("titleDescription", title.getDescription());
        utl.put("bitSuccess", 1);
        world.sendToRoom(utl, user, room);

        user.properties.put(Users.TITLE, titleId);
    }
}
