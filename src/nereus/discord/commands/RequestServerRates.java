package nereus.discord.commands;

import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import nereus.config.ConfigData;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class RequestServerRates implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        if (command.equalsIgnoreCase("$setrates")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Set Rates", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$setrates <exp|drop|rep|cp|gold> <value>`");
            embed.setFooter("This command is used to sets the server rates.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
            event.getChannel().sendMessage(embed);
        } else {
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[2])) {
                throw new CommandException("Your second key is not a numeric", event);
            }

            String attribute = event.getMessageContent().split(" ")[1];
            int Multiplier = Integer.parseInt(event.getMessageContent().split(" ")[2]);
            int rate = Multiplier;
//            Double rate = (double)Multiplier / 100.0 + 1.0;

            if (Multiplier > 500 || Multiplier < -100) {
                event.getChannel().sendMessage("Rates cannot be higher than 500% or less than -100%.");
                return;
            }

            event.getChannel().sendMessage("Setting " + attribute + " rate(s) to " + rate + "x in the server " + ConfigData.SERVER_NAME + ".");
            this.setRates(attribute, Multiplier, world);
            new ServerRates().process(world, event);
        }
    }

    private void setRates(String Type, int Multiplier, World world)
    {
//        Double rate = (double)Multiplier / 100.0 + 1.0;
        int rate = Multiplier;
        try {
            switch (Type) {
                case "exp":
                    world.EXP_RATE = rate;
                    if (Multiplier == 1) {
                        world.sendServerMessage("<font color=\"#ffffff\">Gold</font> rate has been set to normal.");
                    } else {
                        world.sendServerMessage("<font color=\"#ffffff\">Gold</font> rate has been " + (Multiplier > 0 ? "<font color=\"#00FF00\">increased</font>" : "<font color=\"#FF0000\">reduced</font>") + " by " + Multiplier + "%.");
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
                    break;
                case "drop":
                    world.DROP_RATE = rate;
                    if (Multiplier == 1) {
                        world.sendServerMessage("<font color=\"#ffffff\">Drop</font> rate has been set to normal.");
                    } else {
                        world.sendServerMessage("<font color=\"#ffffff\">Drop</font> rate has been " + (Multiplier > 0 ? "<font color=\"#00FF00\">increased</font>" : "<font color=\"#FF0000\">reduced</font>") + " by " + Multiplier + "%.");
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
                    break;
                case "rep":
                    world.REP_RATE = rate;
                    if (Multiplier == 1) {
                        world.sendServerMessage("<font color=\"#ffffff\">Reputation</font> rate has been set to normal.");
                    } else {
                        world.sendServerMessage("<font color=\"#ffffff\">Reputation</font> rate has been " + (Multiplier > 0 ? "<font color=\"#00FF00\">increased</font>" : "<font color=\"#FF0000\">reduced</font>") + " by " + Multiplier + "%.");
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
                    break;
                case "cp":
                    world.CP_RATE = rate;
                    if (Multiplier == 1) {
                        world.sendServerMessage("<font color=\"#ffffff\">Class Point</font> rate has been set to normal.");
                    } else {
                        world.sendServerMessage("<font color=\"#ffffff\">Class Point</font> rate has been " + (Multiplier > 0 ? "<font color=\"#00FF00\">increased</font>" : "<font color=\"#FF0000\">reduced</font>") + " by " + Multiplier + "%.");
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
                    break;
                case "gold":
                    world.GOLD_RATE = rate;
                    if (Multiplier == 1) {
                        world.sendServerMessage("<font color=\"#ffffff\">Gold</font> rate has been set to normal.");
                    } else {
                        world.sendServerMessage("<font color=\"#ffffff\">Gold</font> rate has been " + (Multiplier > 0 ? "<font color=\"#00FF00\">increased</font>" : "<font color=\"#FF0000\">reduced</font>") + " by " + Multiplier + "%.");
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1L));;
                    break;
                case "all":
                    this.setRates("exp", Multiplier, world);
                    this.setRates("drop", Multiplier, world);
                    this.setRates("rep", Multiplier, world);
                    this.setRates("cp", Multiplier, world);
                    this.setRates("gold", Multiplier, world);
                    break;
            }
            LinkedList listOfChannels = world.zone.getAllUsersInZone();
            Iterator var52 = listOfChannels.iterator();
            while (var52.hasNext()) {
                Object temp = var52.next();
                User tgt = ExtensionHelper.instance().getUserByChannel((SocketChannel) temp);
                if (tgt == null) continue;
                new nereus.requests.rates.ServerRates().process(tgt, world);
//                new ServerRates().process(tgt, world);
            }

//            for (Channel chan : world.zone.getChannelList()) {
//                User user = (User)chan.attr(User.PLAYER_KEY).get();
//                if (user == null) continue;
//                new alter.requests.rates.ServerRates().process(user, world);
//            }
        } catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }
}
