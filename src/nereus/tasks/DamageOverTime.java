//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nereus.tasks;

import nereus.ai.MonsterAI;
import nereus.config.ConfigData;
import nereus.db.objects.Area;
import nereus.db.objects.Monster;
import nereus.db.objects.WorldBoss;
import nereus.discord.Webhook;
import nereus.world.Users;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DamageOverTime implements Runnable, CancellableTask {
   private static final Random rand = new Random();
   private String fromTarget;
   private World world;
   private ScheduledFuture<?> running;
   private User user;
   private MonsterAI ai;
   private int damage;
   private boolean stackable;
   private int stack;
   private double multiplier;

   public DamageOverTime(World world, User user, int damage, String cInf, boolean stackable) {
      this.fromTarget = cInf;
      this.world = world;
      this.user = user;
      this.damage = damage;
      this.stackable = stackable;
   }

   public DamageOverTime(World world, MonsterAI ai, int damage, String cInf, boolean stackable) {
      this.fromTarget = cInf;
      this.world = world;
      this.ai = ai;
      this.damage = damage;
      this.stackable = stackable;
   }

   public void run() {

      //damage = (int)((double)damage * (1.0D + 0.90));
      if (this.damage == 0) {
         throw new RuntimeException("damage is 0, pointless to continue.");
      } else {
         boolean Hot = this.damage < 0;
         int dotDamage = rand.nextInt(Math.abs(this.damage));
         if (this.stackable) {
            double rmultiplier = this.multiplier * (double)this.stack;
            dotDamage = (int)((double)dotDamage * (1.0D + rmultiplier));
         }

         if (Hot) {
            dotDamage *= -1;
         }

         JSONObject ct = new JSONObject();
         JSONArray sara = new JSONArray();
         JSONObject saraObj = new JSONObject();
         JSONObject actionResult = new JSONObject();
         JSONObject tgtInfo = new JSONObject();
         actionResult.put("hp", dotDamage);
         actionResult.put("cInf", this.fromTarget);
         actionResult.put("typ", "d");
         if (this.user != null) {
            actionResult.put("tInf", "p:" + this.user.getUserId());
         } else if (this.ai != null) {
            actionResult.put("tInf", "m:" + this.ai.getMapId());
         }

         saraObj.put("actionResult", actionResult);
         saraObj.put("iRes", 1);
         sara.add(saraObj);
         ct.put("cmd", "ct");
         ct.put("sara", sara);
         JSONObject p;
         int monTargetsList;
         if (this.user != null) {
            Room m = this.world.zone.getRoom(this.user.getRoom());
            p = new JSONObject();
            monTargetsList = (Integer)this.user.properties.get("hp") - dotDamage;
            monTargetsList = monTargetsList <= 0 ? 0 : monTargetsList;
            monTargetsList = monTargetsList >= (Integer)this.user.properties.get("hpmax") ? (Integer)this.user.properties.get("hpmax") : monTargetsList;
            this.user.properties.put("hp", monTargetsList);
            if ((Integer)this.user.properties.get("state") == 0) {
               this.running.cancel(true);
               return;
            }

            tgtInfo.put("intHP", monTargetsList);
            if (monTargetsList <= 0 && (Integer)this.user.properties.get("state") != 0) {
               this.running.cancel(false);
               this.world.users.die(this.user);
               tgtInfo.put("intState", (Integer)this.user.properties.get("state"));
               tgtInfo.put("intMP", 0);
               if (((Area)this.world.areas.get(m.getName().split("-")[0])).isPvP()) {
                  int monTargets = (Integer)this.user.properties.get("pvpteam") == 0 ? 1 : 0;
                  if (m.getName().split("-")[0].equals("deadlock")) {
                     this.world.rooms.addPvPScore(m, 1000, monTargets);
                  } else {
                     this.world.rooms.addPvPScore(m, (Integer)this.user.properties.get("level"), monTargets);
                  }

                  ct.put("pvp", this.world.rooms.getPvPResult(m));
               }
            }

            p.put(this.user.getName(), tgtInfo);
            ct.put("p", p);
            this.world.sendToRoom(ct, this.user, m);
         }

         if (this.ai != null) {
            JSONObject m1 = new JSONObject();
            p = new JSONObject();
            this.ai.setHealth(this.ai.getHealth() - dotDamage);
            if (this.ai.getState() == 0) {
               this.running.cancel(true);
               return;
            }

            if (this.ai.getHealth() <= 0 && this.ai.getState() != 0) {
               this.running.cancel(false);
               this.ai.die();
               Monster mon1 = (Monster)world.monsters.get(this.ai.monsterId);
               if (((Area)this.world.areas.get(this.ai.getRoom().getName().split("-")[0])).isPvP()) {
                  monTargetsList = ((Monster)this.world.monsters.get(this.ai.getMonsterId())).getTeamId() == 1 ? 0 : 1;
                  this.world.rooms.relayPvPEvent(this.ai, monTargetsList);
                  ct.put("pvp", this.world.rooms.getPvPResult(this.ai.getRoom()));
               }
               if (world.worldbosses.containsKey(mon1.getId())) {
                  world.wbSpawn.deathTimes.put(mon1.getId(), System.currentTimeMillis());
                  ((WorldBoss)world.worldbosses.get(mon1.getId())).isActive = false;
                  ++((WorldBoss)world.worldbosses.get(mon1.getId())).deaths;
               }

               if (mon1.isWorldBoss()) {
                  world.sendServerMessage("Congratulations! "+ user.getName() +" has dealt the most damage to the World Boss "+ mon1.getName() +".");
                  world.db.jdbc.run("UPDATE monsters_bosses SET Deaths = Deaths + 1 WHERE MonsterID = ?", new Object[]{mon1.getId()});
                  world.db.jdbc.run("UPDATE monsters_bosses SET DeathTime = NOW() WHERE MonsterID = ?", new Object[]{mon1.getId()});
                  Webhook.EmbedObject embed = new Webhook.EmbedObject();
                  embed.setTitle("**" + mon1.getName() + "** is defeated.");
                  embed.setDescription("Congratulations! "+ user.properties.get(Users.USERNAME) +"has dealt the most damage to the World Boss "+ mon1.getName() +".");
                  embed.setColor(Color.YELLOW);
                  embed.setUrl("https://nereus.world/Wiki/monster/info/");


               }

               JSONArray monTargetsList1 = new JSONArray();
               Set monTargets1 = this.ai.getTargets();
               Iterator i$ = monTargets1.iterator();

               while(i$.hasNext()) {
                  int userId = (Integer)i$.next();
                  User userTgt = ExtensionHelper.instance().getUserById(userId);
                  if (userTgt != null) {
                     userTgt.properties.put("state", 1);
                     this.world.users.regen(this.user);
                     monTargetsList1.add(userTgt.getName());
                     JSONObject userData = new JSONObject();
                     userData.put("intState", (Integer)userTgt.properties.get("state"));
                     p.put(userTgt.getName(), userData);
                  }
               }

               tgtInfo.put("targets", monTargetsList1);
            }

            tgtInfo.put("intHP", this.ai.getHealth());
            if (this.ai.getHealth() <= 0 || this.ai.getState() == 0) {
               tgtInfo.put("intMP", 0);
               tgtInfo.put("intState", 0);
            }

            m1.put(String.valueOf(this.ai.getMapId()), tgtInfo);
            ct.put("m", m1);
            if (!p.isEmpty()) {
               ct.put("p", p);
            }

            this.world.send(ct, this.ai.getRoom().getChannellList());
         }

      }
   }

   public void cancel() {
      this.running.cancel(false);
   }

   public void setRunning(ScheduledFuture<?> running) {
      this.running = running;
   }

   public void exetend() {
   }

   public void incrementStack() {
      ++this.stack;
   }

   public int getStackCount() {
      return this.stack;
   }

   public void setStack(int stack) {
      this.stack = stack;
   }

   public double getMultiplier() {
      return this.multiplier;
   }

   public void setMultiplier(double damageIncrease) {
      this.multiplier = damageIncrease;
   }
}
