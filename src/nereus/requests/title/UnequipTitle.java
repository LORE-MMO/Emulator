package nereus.requests.title;

import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;
import nereus.db.objects.Title;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;

public class UnequipTitle implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int titleId = Integer.parseInt(params[0]);

        Title title = world.titles.get(titleId);
        if (title == null) throw new RequestException("Invalid Title.");

        int titleCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_titles WHERE TitleID = ? AND UserID = ?", titleId, user.properties.get(Users.DATABASE_ID));
        if (titleCount <= 0) throw new RequestException("Selected title is currently locked.");

        world.db.jdbc.run("UPDATE users SET Title = NULL WHERE id = ?", user.properties.get(Users.DATABASE_ID));

        JSONObject utl = new JSONObject();
        utl.put("cmd", "unequipTitle");
        utl.put("uid", user.getUserId());
        utl.put("title", title);
        utl.put("titleId", title.getId());
        utl.put("titleName", title.getName());
        utl.put("titleColor", title.getColor());
        utl.put("titleDescription", title.getDescription());
        utl.put("bitSuccess", 1);
        world.sendToRoom(utl, user, room);
    }
}