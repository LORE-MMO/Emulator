package nereus.discord;

import nereus.discord.events.FetchCommand;
import nereus.discord.events.ReloadAccess;
import nereus.discord.events.ReloadCommands;
import nereus.discord.events.ReloadSettings;
import nereus.discord.events.ReloadUsers;
import nereus.world.World;

import java.util.HashMap;
import java.util.Map;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

public class Bot
{
    public DiscordApi api;
    public World world;
    public static HashMap<String, Map.Entry<String, Integer>> commands = new HashMap();
    public static HashMap<String, Map.Entry<String, Integer>> users = new HashMap();
    public static HashMap<String, String> settings = new HashMap();
    public static HashMap<Integer, String> accesses = new HashMap();

    public Bot(String token, World world) {
        this.world = world;
        new ReloadSettings(world);
        new ReloadAccess(world);
        new ReloadCommands(world);
        new ReloadUsers(world);
        this.init(token);
    }

    private void init(String token) {
        FallbackLoggerConfiguration.setDebug(false);
        api = new DiscordApiBuilder().setToken(token).login().join();
        api.updateActivity(ActivityType.LISTENING, "$help");
        api.addMessageCreateListener(new FetchCommand(this.world));
    }
}