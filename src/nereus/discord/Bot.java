package nereus.discord;

import com.github.kaktushose.jda.commands.entities.JDACommandsBuilder;
import nereus.config.ConfigData;
import nereus.world.World;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Bot {
    public static JDA jda;

    public Bot(String token, World world)  {
        try {
            jda = JDABuilder.createDefault(token).build();
            JDACommandsBuilder.start(jda, "?", true, true, true);
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
