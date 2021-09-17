package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.requests.guild.AddBuilding;
import nereus.requests.guild.AddConnection;
import nereus.requests.guild.AddFrame;
import nereus.requests.guild.BuyPlot;
import nereus.requests.guild.GetInterior;
import nereus.requests.guild.GetInventory;
import nereus.requests.guild.GetShop;
import nereus.requests.guild.GuildAccept;
import nereus.requests.guild.GuildBuyItem;
import nereus.requests.guild.GuildCreate;
import nereus.requests.guild.GuildDeclineInvite;
import nereus.requests.guild.GuildDemote;
import nereus.requests.guild.GuildInvite;
import nereus.requests.guild.GuildMOTD;
import nereus.requests.guild.GuildPromote;
import nereus.requests.guild.GuildRemove;
import nereus.requests.guild.GuildRename;
import nereus.requests.guild.GuildSellItem;
import nereus.requests.guild.GuildSlots;
import nereus.requests.guild.RemoveBuilding;
import nereus.requests.guild.RemoveConnection;
import nereus.requests.guild.SaveInterior;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class GuildCommand implements IRequest {
   public GuildCommand() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      if(params[0].equals("gc")) {
         (new GuildCreate()).process(params, user, world, room);
      } else if(params[0].equals("gi")) {
         (new GuildInvite()).process(params, user, world, room);
      } else if(params[0].equals("ga")) {
         (new GuildAccept()).process(params, user, world, room);
      } else if(params[0].equals("gr")) {
         (new GuildRemove()).process(params, user, world, room);
      } else if(params[0].equals("gdi")) {
         (new GuildDeclineInvite()).process(params, user, world, room);
      } else if(params[0].equals("rename")) {
         (new GuildRename()).process(params, user, world, room);
      } else if(params[0].equals("gp")) {
         (new GuildPromote()).process(params, user, world, room);
      } else if(params[0].equals("gd")) {
         (new GuildDemote()).process(params, user, world, room);
      } else if(params[0].equals("motd")) {
         (new GuildMOTD()).process(params, user, world, room);
      } else if(params[0].equals("slots")) {
         (new GuildSlots()).process(params, user, world, room);
      } else if(params[0].equals("getInterior")) {
         (new GetInterior()).process(params, user, world, room);
      } else if(params[0].equals("buyplot")) {
         (new BuyPlot()).process(params, user, world, room);
      } else if(params[0].equals("getInv")) {
         (new GetInventory()).process(params, user, world, room);
      } else if(params[0].equals("getShop")) {
         (new GetShop()).process(params, user, world, room);
      } else if(params[0].equals("saveInt")) {
         (new SaveInterior()).process(params, user, world, room);
      } else if(params[0].equals("addFrame")) {
         (new AddFrame()).process(params, user, world, room);
      } else if(params[0].equals("addBuilding")) {
         (new AddBuilding()).process(params, user, world, room);
      } else if(params[0].equals("removeBuilding")) {
         (new RemoveBuilding()).process(params, user, world, room);
      } else if(params[0].equals("buyItem")) {
         (new GuildBuyItem()).process(params, user, world, room);
      } else if(params[0].equals("sellItem")) {
         (new GuildSellItem()).process(params, user, world, room);
      } else if(params[0].equals("addConnection")) {
         (new AddConnection()).process(params, user, world, room);
      } else if(params[0].equals("removeConnection")) {
         (new RemoveConnection()).process(params, user, world, room);
      }

   }
}
