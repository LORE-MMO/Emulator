package nereus.requests;

import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.requests.party.PartyAccept;
import nereus.requests.party.PartyAcceptSummon;
import nereus.requests.party.PartyDecline;
import nereus.requests.party.PartyDeclineSummon;
import nereus.requests.party.PartyInvite;
import nereus.requests.party.PartyKick;
import nereus.requests.party.PartyLeave;
import nereus.requests.party.PartyPromote;
import nereus.requests.party.PartySummon;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class PartyCommand implements IRequest {
   public PartyCommand() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      if(params[0].equals("pi")) {
         (new PartyInvite()).process(params, user, world, room);
      } else if(params[0].equals("pk")) {
         (new PartyKick()).process(params, user, world, room);
      } else if(params[0].equals("pl")) {
         (new PartyLeave()).process(params, user, world, room);
      } else if(params[0].equals("ps")) {
         (new PartySummon()).process(params, user, world, room);
      } else if(params[0].equals("psa")) {
         (new PartyAcceptSummon()).process(params, user, world, room);
      } else if(params[0].equals("psd")) {
         (new PartyDeclineSummon()).process(params, user, world, room);
      } else if(params[0].equals("pp")) {
         (new PartyPromote()).process(params, user, world, room);
      } else if(params[0].equals("pa")) {
         (new PartyAccept()).process(params, user, world, room);
      } else if(params[0].equals("pd")) {
         (new PartyDecline()).process(params, user, world, room);
      }

   }
}
