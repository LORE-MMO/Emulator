package nereus.discord.commands;

import nereus.config.ConfigData;
import nereus.discord.events.ReloadAccess;
import nereus.discord.events.ReloadCommands;
import nereus.discord.events.ReloadSettings;
import nereus.discord.events.ReloadUsers;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

public class ClearServer implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {

        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();
        if (command.equalsIgnoreCase("$clear")) {
            embed.setAuthor("Clear", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$clear <map|shop|quest|item|settings|discord|all>`");
            embed.setFooter("This command is used to clear data server.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
            event.getChannel().sendMessage(embed);
        } else {
            switch (command.split(" ")[1]) {
                case "master": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("master");
                    event.getChannel().sendMessage("Monster data cleared.");
                    break;
                }
                case "enhshop": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("enhshop");
                    event.getChannel().sendMessage("Server data cleared.");
                    break;
                }
                case "worldboss": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("worldboss");
                    event.getChannel().sendMessage("Server data cleared.");
                    break;
                }
                case "item": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("item");
                    event.getChannel().sendMessage("Item data cleared.");
                    break;
                }
                case "achievement": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("achievement");
                    event.getChannel().sendMessage("Server data cleared.");
                    break;
                }
                case "map": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("map");
                    event.getChannel().sendMessage("Map data cleared.");
                    break;
                }
                case "quest": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("quest");
                    event.getChannel().sendMessage("Quest data cleared.");
                    break;
                }
                case "shop": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("shop");
                    event.getChannel().sendMessage("Shop data cleared.");
                    break;
                }
                case "settings": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("settings");
                    event.getChannel().sendMessage("Setting data cleared.");
                    break;
                }
                case "discord": {
                    new ReloadSettings(world);
                    new ReloadAccess(world);
                    new ReloadCommands(world);
                    new ReloadUsers(world);
                    event.getChannel().sendMessage("Discord data cleared.");
                    break;
                }
                case "all": {
//                event.getChannel().sendMessage("Retrieving database objects..");
                    world.retrieveDatabaseObject("item");
//                    world.retrieveDatabaseObject("achievement");
                    world.retrieveDatabaseObject("map");
                    world.retrieveDatabaseObject("quest");
                    world.retrieveDatabaseObject("shop");
                    world.retrieveDatabaseObject("settings");
                    event.getChannel().sendMessage("Server data cleared.");
                    break;
                }
            }
        }
    }
}

