package nereus.requests.title;

import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import nereus.db.objects.Title;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.Users;
import nereus.world.World;

import java.util.ArrayList;
import java.util.Map;

public class TitleList implements IRequest {

    ArrayList<Integer> titles = new ArrayList<Integer>();

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {

        QueryResult rs = world.db.getJdbc().query("SELECT TitleID FROM users_titles WHERE UserID = ?", new Object[]{user.properties.get(Users.DATABASE_ID)});

        while(rs.next()) {
            this.titles.add(rs.getInt("TitleID"));
        }
        rs.close();

        JSONArray titleList = new JSONArray();
        JSONObject ttl = new JSONObject();
        ttl.put("cmd", "loadTitle");

        for (Map.Entry<Integer, Title> entry : world.titles.entrySet()) {
            JSONObject title = new JSONObject();
            title.put("id", entry.getValue().getId());
            title.put("Name", (entry.getValue()).getName());
            title.put("Description", (entry.getValue()).getDescription());
            title.put("Strength", 0);
            title.put("Intellect", 0);
            title.put("Endurance", 0);
            title.put("Dexterity", 0);
            title.put("Wisdom", 0);
            title.put("Luck", 0);
            title.put("Access", 1);
            title.put("List", 0);
            title.put("Lock", this.titles.contains((entry.getValue()).getId()) ? 1 : 0);
            titleList.add(title);
        }

        ttl.put("lists", titleList);
        ttl.put("bitSuccess", 1);
        world.send(ttl, user);
    }
}