package nereus.requests.rates;

import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.world.World;
import net.sf.json.JSONObject;

public class ServerRates implements IRequest {
    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject serverRates = new JSONObject();
        serverRates.put((Object)"cmd", (Object)"opqwjkmtgmasd");
        serverRates.put((Object)"rateGold", (Object)world.GOLD_RATE);
        serverRates.put((Object)"rateDrop", (Object)world.DROP_RATE);
        serverRates.put((Object)"rateExp", (Object)world.EXP_RATE);
        serverRates.put((Object)"rateRep", (Object)world.REP_RATE);
        serverRates.put((Object)"rateCP", (Object)world.CP_RATE);
        world.send(serverRates, user);
    }

    public void process(User user, World world) {
        JSONObject serverRates = new JSONObject();
        serverRates.put((Object)"cmd", (Object)"opqwjkmtgmasd");
        serverRates.put((Object)"rateGold", (Object)world.GOLD_RATE);
        serverRates.put((Object)"rateDrop", (Object)world.DROP_RATE);
        serverRates.put((Object)"rateExp", (Object)world.EXP_RATE);
        serverRates.put((Object)"rateRep", (Object)world.REP_RATE);
        serverRates.put((Object)"rateCP", (Object)world.CP_RATE);
        world.send(serverRates, user);
    }
}
