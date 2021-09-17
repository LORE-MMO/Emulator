package nereus.discord.commands;

import it.gotoandplay.smartfoxserver.data.User;
import nereus.config.ConfigData;
import nereus.db.objects.Item;
import nereus.dispatcher.CommandException;
import nereus.dispatcher.IDiscord;
import nereus.world.World;

import java.awt.Color;
import jdbchelper.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class RequestReward implements IDiscord
{
    @Override
    public void process(World world, MessageCreateEvent event) throws CommandException
    {
        String command = event.getMessageContent();
        EmbedBuilder embed = new EmbedBuilder();

        if (command.equalsIgnoreCase("$reward")) {
            embed.setAuthor("Reward", null, ConfigData.DISCORD_BOT_AVATAR);
            embed.addField("How to use?", "`$reward <user id> <item id> <qauantity>`");
            embed.setFooter("This command is used to give item for someone.");
            embed.setColor(Color.BLACK);
            embed.setThumbnail(event.getMessageAuthor().getAvatar());
        } else {
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[2])) {
                throw new CommandException("Your first key is not a numeric", event);
            }
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[2])) {
                throw new CommandException("Your second key is not a numeric", event);
            }
            if (!StringUtils.isNumeric((CharSequence)event.getMessageContent().split(" ")[3])) {
                throw new CommandException("Your second key is not a numeric", event);
            }

            int CharacterID = Integer.parseInt(command.split(" ")[1]);
            int ItemID = Integer.parseInt(command.split(" ")[2]);
            int Quantity = Integer.parseInt(command.split(" ")[3]);

            Item item = world.items.get(ItemID);
            if (item == null) {
                event.getChannel().sendMessage("There is no such item as Item ID: " + ItemID);
                return;
            }

            String username = world.db.jdbc.queryForString("SELECT username FROM users WHERE id = ?", CharacterID);
            User user = world.zone.getUserByName(username.toLowerCase());

            if (user == null) {
                QueryResult itemResult = world.db.jdbc.query("SELECT id FROM users_items WHERE ItemID = ? AND UserID = ? AND Bank = 0", item.getId(), CharacterID);
                if (itemResult.next()) {
                    int charItemId = itemResult.getInt("id");
                    itemResult.close();
                    if (item.getStack() > 1) {
                        int quantity = world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE id = ? FOR UPDATE", charItemId);
                        if (quantity < item.getStack()) {
                            world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ?", quantity + Quantity, CharacterID);
                        }
                    }
                } else {
                    world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", CharacterID, item.getId(), item.getEnhId(), Quantity);
                }
                itemResult.close();
            } else {
                world.send(new String[] {"server", "An in game staff has rewarded you the item " + item.getName() + "."}, user);
                world.users.dropItem(user, ItemID, Quantity);
            }
            embed.setDescription("The item **[" + world.clearHTMLTags(item.getName()) + "](" + ConfigData.SERVER_GAME_LINK + item.getId() + ")** has been rewarded to the user **[" + username + "](" + ConfigData.SERVER_PROFILE_LINK + username + ")**.");
            embed.setAuthor("Item Reward", null, event.getMessageAuthor().getAvatar());
            embed.setFooter("" + event.getMessageAuthor().getId());
            embed.setColor(Color.BLACK);
            embed.setThumbnail(ConfigData.DISCORD_BOT_AVATAR);
        }
        event.getChannel().sendMessage(embed);
    }
}
