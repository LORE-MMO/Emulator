
package nereus.requests;

import nereus.ai.MonsterAI;
import nereus.aqw.Rank;
import nereus.config.ConfigData;
import nereus.db.objects.*;
import nereus.discord.Webhook;
import nereus.dispatcher.IRequest;
import nereus.dispatcher.RequestException;
import nereus.tasks.DamageOverTime;
import nereus.tasks.RemoveAura;
import nereus.world.World;
import nereus.world.Users;
import nereus.world.stats.Stats;
import com.google.common.collect.HashMultimap;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import jdbchelper.NoResultException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Action implements IRequest {
   private static final Random rand = new Random();
   public Set<Aura> auraskill = new LinkedHashSet();
   private int LifeSteal;


   public Action() {
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      if ((Integer)user.properties.get("state") != 0) {
         int actId = Integer.parseInt(params[0]);
         String skillReference = this.getSkillRefence(params[1]);
         String tInf = this.parseTargetInfo(params[1]);
         rand.setSeed((long)actId);
         JSONObject ct = new JSONObject();
         JSONArray sarsa = new JSONArray();
         JSONObject sarsaObj = new JSONObject();
         JSONArray anims = new JSONArray();
         JSONObject anim = new JSONObject();
         JSONArray auras = new JSONArray();
         JSONArray a = new JSONArray();
         JSONObject p = new JSONObject();
         JSONObject m = new JSONObject();
         Stats stats = (Stats)user.properties.get("stats");
         String fromTarget = "p:" + user.getUserId();
         Area area = (Area)world.areas.get(room.getName().split("-")[0]);
         Map skills = (Map)user.properties.get("skills");

         Skill skill;
         int userMana;
         int manaIncrease;
         try {
            skill = (Skill)world.skills.get(skills.get(skillReference));
            if (skill == null) {
               return;
            }

            userMana = Rank.getRankFromPoints((Integer)user.properties.get("cp"));
            if (userMana < 2 && skill.getReference().equals("a2") || userMana < 3 && skill.getReference().equals("a3") || userMana < 5 && skill.getReference().equals("a4")) {
               world.users.log(user, "Packet Edit [Action]", "Using a skill when designated rank is not yet achieved.");
               return;
            }

            if (skill.getReference().equals("i1")) {
               manaIncrease = world.db.jdbc.queryForInt("SELECT id FROM items WHERE Meta = ?", new Object[]{skill.getId()});
               if (!world.users.turnInItem(user, manaIncrease, 1)) {
                  world.users.log(user, "Packet Edit [Action]", "TurnIn failed when using potions.");
                  return;
               }
            }

//            if (user.properties.get("perfecttimings") != null && !skill.getReference().equals("aa")) {
//               Long var52 = System.currentTimeMillis();
//               Long maxMana = (Long)user.properties.get("perfecttimings") + (long)skill.getCooldown() - (long)(skill.getCooldown() / 2) - 500L;
//               Integer targets = (Integer)user.properties.get("requestbotcounter");
//               if (targets == null) {
//                  user.properties.put("requestbotcounter", 0);
//               } else if (maxMana > var52) {
//                  user.properties.put("requestbotcounter", 0);
//               } else {
//                  user.properties.put("requestbotcounter", targets + 1);
//               }
//            }

            if (user.properties.get(skill.getReference()) != null) {
               long var53 = System.currentTimeMillis();
               long var55 = (Long)user.properties.get(skill.getReference()) + (long)skill.getCooldown() - (long)(skill.getCooldown() / 2) - 500L;
               int inputSet = (Integer)user.properties.get("requestwarncounter");
               if (var55 > var53) {
                  if (user.properties.get("language").equals("BR")) {
                     world.send(new String[]{"warning", "Medidas tomadas muito rapidamente, tente novamente em um momento."}, user);
                  } else {
                     world.send(new String[]{"warning", "Action taken too quickly, try again in a moment."}, user);
                     world.users.log(user, "Packet Edit [gar]", "Attack packet hack.");
                  }

                  user.properties.put("requestwarncounter", inputSet + 1);
                  return;
               }

               user.properties.put("requestwarncounter", 0);
            }
         } catch (NoResultException var57) {
            throw new UnsupportedOperationException("Unassigned skill ID: " + skillReference);
         }

         userMana = (Integer)user.properties.get("mp") - skill.getMana();
         manaIncrease = (int)(stats.get_INT() + stats.get_INT() / 2.0D);
         userMana += rand.nextInt(Math.abs(manaIncrease));
         int var54 = (Integer)user.properties.get("mpmax");
         userMana = userMana >= var54 ? var54 : userMana;
         user.properties.put("mp", userMana);
         String[] var56 = tInf.split(",");
         List inputList = Arrays.asList(var56);
         HashSet var57 = new HashSet(inputList);
         if (var57.size() < inputList.size()) {
            //world.users.kick(user);
            //world.users.log(user, "Packet Edit [gar]", "Attack packet hack.");
         }

         ConcurrentHashMap monsters = (ConcurrentHashMap)room.properties.get("monsters");
         String[] arr$ = var56;
         int len$ = var56.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String target = arr$[i$];
            String tgtType = target.split(":")[0];
            int tgtId = Integer.parseInt(target.split(":")[1]);
            int damage = this.getRandomDamage(stats, skill);
            int LifeSteal = (int) ((double) damage * skill.getLifeSteal());
            boolean dodge = false;
            boolean crit = Math.random() < stats.get$tcr();
            boolean miss = damage > 0 ? Math.random() > 1.0D - (Double)world.coreValues.get("baseMiss") + stats.get$thi() : false;
            Set userAuras = (Set)user.properties.get("auras");
            Iterator tgtInfo = userAuras.iterator();
            while(tgtInfo.hasNext()) {
               RemoveAura damageResult = (RemoveAura)tgtInfo.next();
               Aura userStats = damageResult.getAura();
               if (!userStats.getCategory().equals("d")) {
                  damage = (int)((double)damage * (1.0D  +userStats.getDamageIncrease()));
               }
            }

            damage = (int)(miss ? 0.0D : (crit ? (double)damage * stats.get$scm() : (double)damage));
            if (damage > 0 && !skill.getReference().equals("i1") && user.getUserId() != tgtId) {
               user.properties.put("state", 2);
            }

            JSONObject var58 = new JSONObject();
            int mainPlayer;

            if (tgtType.equals("m")) {
               MonsterAI var59 = (MonsterAI)monsters.get(tgtId);
               if (var59 == null) {
                  continue;
               }
               Set tgtUserItem = var59.getAuras();
               Iterator var72 = tgtUserItem.iterator();

               while(var72.hasNext()) {
                  RemoveAura userTgtHp = (RemoveAura)var72.next();
                  Aura tgtList = userTgtHp.getAura();
                  if (!tgtList.getCategory().equals("d")) {
                     if (tgtList.canStack()) {
                        damage = (int)((double)damage * (1.0D + tgtList.getDamageIncrease() * (double)userTgtHp.getAuraCount()));
                     } else {
                        damage = (int)((double)damage * (1.0D + tgtList.getDamageIncrease()));
                     }
                  }
               }

               Iterator userEquipments = ((JSONObject) user.properties.get(Users.EQUIPMENT)).values().iterator();
               while (userEquipments.hasNext()) {
                  Item item = world.items.get(((JSONObject) userEquipments.next()).getInt("ItemID"));
                  if (item.passives.iterator().hasNext()) {
                     double increase = 1.0 + item.passives.iterator().next().getDamageincrease();
                     damage *= increase > 0 ? increase : 0;
                  }
               }

               //END ITEM BUFFED

               //MONSTER REDUCTION DAMAGE
               MonsterAI testai = (MonsterAI)monsters.get(tgtId);
               Monster moni = (Monster)world.monsters.get(testai.monsterId);
               if (moni.getDamageReduction() >= 0){
                  damage = (int) ((double) damage * (1.0D - moni.getDamageReduction()));
               }

               var59.setHealth(var59.getHealth() - damage);
               var59.addTarget(user.getUserId());
               Set var74 = var59.getTargets();
               Monster mon1 = (Monster)world.monsters.get(var59.monsterId);

               int aid;
               if (var59.getHealth() <= 0 && var59.getState() != 0) {
                  var59.die();
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

                  if (area.isPvP()) {
                     world.rooms.relayPvPEvent(var59, (Integer)user.properties.get("pvpteam"));
                     ct.put("pvp", world.rooms.getPvPResult(room));
                  }
               } else if (var59.getState() == 0) {
                  a.clear();
               } else if (var59.getState() != 2) {
                  var59.setAttacking(world.scheduleTask(var59, 2500L, TimeUnit.MILLISECONDS, true));
               }

               if (!miss || !dodge) {
                  Skill iniaura = world.skills.get(skills.get(skillReference));
                  Iterator aurasefek = iniaura.auraskill.entries().iterator();
                  HashMultimap auraresult = HashMultimap.create();

                  while(aurasefek.hasNext()){
                     Map.Entry<Integer, SkillAuras> entry = (Map.Entry)aurasefek.next();
                     SkillAuras iniaurainfo = (SkillAuras)entry.getValue();
                     int skillid = iniaurainfo.skillid;
                     int iniauraid = iniaurainfo.auraid;
                     Aura auratest = world.auras.get(iniauraid);
                     double chanceaura = auratest.getApplychance();
                     auraresult.put(iniauraid, chanceaura);
                     if (!skill.isAuraRandom() && !iniaura.auraskill.isEmpty() && Math.random() < chanceaura) {
                        auras.add(this.applyAura(world, var59, iniauraid, fromTarget, damage));
                     }
                  }
                  if (auraresult.size() > 0) {
                     ArrayList keys1 = new ArrayList(auraresult.keySet());
                     int aid1 = (Integer)keys1.get(rand.nextInt(keys1.size()));
                     Object[] rate1 = auraresult.get(aid1).toArray();
                     Double rateValue = (Double)rate1[rand.nextInt(rate1.length)];
                     if (skill.isAuraRandom() && aid1 > 0 && Math.random() < rateValue) {
                        auras.add(this.applyAura(world, var59, aid1, fromTarget, damage));
                     }
                  }
               }

               JSONArray var68 = new JSONArray();
               Iterator var71 = var74.iterator();

               while(var71.hasNext()) {
                  mainPlayer = (Integer)var71.next();
                  User mainExp = ExtensionHelper.instance().getUserById(mainPlayer);
                  if (mainExp != null) {
                     if (var59.getState() == 0) {
                        mainExp.properties.put("state", 1);
                        world.users.regen(mainExp);
                        var68.add(mainExp.getName());
                     }

                     JSONObject plusKill = new JSONObject();
                     plusKill.put("intMP", (Integer)mainExp.properties.get("mp"));
                     plusKill.put("intState", (Integer)mainExp.properties.get("state"));
                     p.put(mainExp.getName(), plusKill);
                  } else {
                     var59.removeTarget(mainPlayer);
                  }
               }
               if (!miss || !dodge){
                  if(skill.getDsrc().equals("Steal")){
                     int stealprecent = (int) (skill.getHpregen() * skill.getHitTargets() * 100000);
                     int hp = (Integer)user.properties.get("hp");
                     hp += rand.nextInt(stealprecent);
                     SmartFoxServer.log.warning("steal : " + rand.nextInt(stealprecent));
                  }
               }

               if (!var68.isEmpty()) {
                  var58.put("targets", var68);
               }

               var58.put("intState", var59.getState());
               var58.put("intHP", var59.getHealth());
               if (var59.getState() == 0) {
                  var58.put("intMP", var59.getMana());
               }

               m.put(String.valueOf(tgtId), var58);
            }

            if (tgtType.equals("p")) {
               User var60 = ExtensionHelper.instance().getUserById(tgtId);
               if (var60 == null || (Integer)var60.properties.get("state") == 0) {
                  continue;
               }

               Stats var63 = (Stats)var60.properties.get("stats");
               dodge = Math.random() < var63.get$tdo();
               if (!user.equals(var60)) {
                  Set var64 = (Set)var60.properties.get("auras");
                  Iterator var66 = var64.iterator();

                  while(var66.hasNext()) {
                     RemoveAura var75 = (RemoveAura)var66.next();
                     Aura aura = var75.getAura();
                     if (!aura.getCategory().equals("d")) {
                        if (aura.canStack()) {
                           damage = (int)((double)damage * (1.0D + aura.getDamageIncrease() * (double)var75.getAuraCount()));
                        } else {
                           damage = (int)((double)damage * (1.0D + aura.getDamageIncrease()));
                        }
                     }
                  }
               }

               if (damage > 0 && !user.equals(var60)) {
                  if (dodge) {
                     damage = 0;
                  }

                  if (area.isPvP() && user.properties.get("pvpteam") == var60.properties.get("pvpteam")) {
                     return;
                  }

                  var60.properties.put("state", 2);
               }
               damage = (int)((double)damage * (1.0D + 0.90));//decrease damage pvp
               int var70 = (Integer)var60.properties.get("hp") - damage;
               var70 = var70 <= 0 ? 0 : var70;
               var70 = var70 >= (Integer)var60.properties.get("hpmax") ? (Integer)var60.properties.get("hpmax") : var70;
               var60.properties.put("hp", var70);
               int auraid;
               if (var70 <= 0 && (Integer)var60.properties.get("state") != 0) {
                  JSONArray var80 = new JSONArray();
                  var80.add(user.getName());
                  var58.put("targets", var80);
                  world.users.die(var60);
                  user.properties.put("state", 1);

//                  world.users.dropItem(user, 222156, 1);
                  if (area.isPvP()) {
                     Iterator var76 = area.items.iterator();

                     int var79;
                     while(var76.hasNext()) {
                        var79 = (Integer)var76.next();
                        world.users.dropItem(user, var79);
                     }

                     if (room.getName().split("-")[0].equals("guildwars")) {
                        world.db.jdbc.run("UPDATE guilds SET TotalKills = (TotalKills + 1) WHERE id = ?", new Object[]{user.properties.get("guildid")});
                        world.db.jdbc.run("UPDATE guilds SET Exp = (Exp + 100) WHERE id = ?", new Object[]{user.properties.get("guildid")});
                        JSONObject var78 = world.users.getGuildObject((Integer)user.properties.get("guildid"));
                        var79 = world.db.jdbc.queryForInt("SELECT Exp FROM guilds WHERE id = ?", new Object[]{user.properties.get("guildid")});
                        mainPlayer = world.db.jdbc.queryForInt("SELECT TotalKills FROM guilds WHERE id = ?", new Object[]{user.properties.get("guildid")});
                        var78.put("TotalKills", mainPlayer + 1);
                        world.sendGuildUpdate(var78);
                        if (var79 >= world.getGuildExpToLevel((Integer)var78.get("Level"))) {
                           world.users.guildLevelUp((Integer)user.properties.get("guildid"), (Integer)var78.get("Level") + 1);
                        } else {
                           var78.put("Exp", var79 + 100);
                           world.sendGuildUpdate(var78);
                        }
                     }

                     if (room.getName().split("-")[0].equals("deadlock")) {
                        world.rooms.addPvPScore(room, 1000, (Integer)user.properties.get("pvpteam"));
                     } else {
                        world.rooms.addPvPScore(room, (Integer)var60.properties.get("level"), (Integer)user.properties.get("pvpteam"));
                     }

                     ct.put("pvp", world.rooms.getPvPResult(room));
                  }
               }


               if (!miss || !dodge || !skill.getReference().equals("i1")) {

                  Skill iniaura = world.skills.get(skills.get(skillReference));
                  Iterator aurasefek = iniaura.auraskill.entries().iterator();
                  HashMultimap auraresult = HashMultimap.create();



                  while(aurasefek.hasNext()){
                     Map.Entry<Integer, SkillAuras> entry = (Map.Entry)aurasefek.next();
                     SkillAuras iniaurainfo = (SkillAuras)entry.getValue();
                     int skillid = iniaurainfo.skillid;
                     int iniauraid = iniaurainfo.auraid;
                     Aura auratest = world.auras.get(iniauraid);
                     double chanceaura = auratest.getApplychance();
                     auraresult.put(iniauraid, chanceaura);
                     if (!skill.isAuraRandom() && !iniaura.auraskill.isEmpty() && Math.random() < chanceaura) {
                        auras.add(this.applyAura(world, var60, iniauraid, fromTarget, damage));
                     }
                  }
                  if (auraresult.size() > 0) {
                     ArrayList keys1 = new ArrayList(auraresult.keySet());
                     int aid = (Integer)keys1.get(rand.nextInt(keys1.size()));
                     Object[] rate1 = auraresult.get(aid).toArray();
                     Double rateValue = (Double)rate1[rand.nextInt(rate1.length)];
                     if (skill.isAuraRandom() && aid > 0 && Math.random() < rateValue) {
                        auras.add(this.applyAura(world, var60, aid, fromTarget, damage));
                     }
                  }
               }

               var58.put("intState", var60.properties.get("state"));
               var58.put("intHP", var60.properties.get("hp"));
               var58.put("intMP", var60.properties.get("mp"));
               p.put(var60.getName(), var58);
               if (!p.containsKey(user.getName())) {
                  var58.clear();
                  var58.put("intMP", user.properties.get("mp"));
                  var58.put("intState", (Integer)user.properties.get("state"));
                  p.put(user.getName(), var58);
               }
            }


            if(skill.getLifeSteal() > 0.00) {
               User var60 = ExtensionHelper.instance().getUserById(user.getUserId());
               int userHealth = ((Integer) user.properties.get(Users.HP)).intValue() + LifeSteal;
               int userHealthMax = ((Integer) user.properties.get(Users.HP_MAX)).intValue();
               userHealth = userHealth >= userHealthMax ? userHealthMax : userHealth;
               user.properties.put(Users.HP, Integer.valueOf(userHealth));
               JSONObject ls = new JSONObject();
               ls.put("intState", var60.properties.get(Users.STATE));
               ls.put("intHP", user.properties.get(Users.HP));
               ls.put("intMP", user.properties.get(Users.MP));
               p.put(user.getName(), ls);
            }

            JSONObject var61 = new JSONObject();
            var61.put("hp", damage);
            var61.put("tInf", target);
            var61.put("type", dodge ? "dodge" : (miss ? "miss" : (crit ? "crit" : "hit")));
            a.add(var61);
         }

         if ((Integer)user.properties.get("state") == 1) {
            world.users.regen(user);
         }

         anim.put("strFrame", user.properties.get("frame"));
         anim.put("cInf", fromTarget);
         anim.put("fx", skill.getEffects());
         anim.put("tInf", tInf);
         anim.put("animStr", skill.getAnimation());
         if (!skill.getStrl().isEmpty()) {
            anim.put("strl", skill.getStrl());
         }
         anims.add(anim);
         sarsaObj.put("cInf", fromTarget);
         sarsaObj.put("a", a);
         sarsaObj.put("actID", actId);
         sarsaObj.put("iRes", 1);
         sarsa.add(sarsaObj);
         if (!m.isEmpty()) {
            ct.put("m", m);
         }

         if (!auras.isEmpty()) {
            ct.put("a", auras);
         }

         ct.put("p", p);
         ct.put("cmd", "ct");
         ct.put("anims", anims);
         if (area.isPvP()) {
            ct.put("sarsa", sarsa);
            world.sendToRoom(ct, user, room);
         } else {
            world.sendToRoomButOne(ct, user, room);
            ct.put("sarsa", sarsa);
            world.send(ct, user);
         }

         user.properties.put(skill.getReference(), System.currentTimeMillis());
         user.properties.put("perfecttimings", System.currentTimeMillis());
      }

   }

   private RemoveAura renewAura(World world, MonsterAI ai, Aura aura, RemoveAura ra, String fromTarget, int damage) {
      ra.cancel();
      if (aura.eatsAura()) {
         int auratoeat = aura.getAuraToEat();
         if (ai.hasAura(auratoeat)) {
            RemoveAura rau = ai.getAura(auratoeat);
            rau.run();
         }
      }

      if (aura.getCategory().equals("d")) {
         DamageOverTime dot = ra.getDot();
         dot.setStack(ra.getAuraCount());
         dot.setMultiplier(aura.getDamageIncrease());
         dot.setRunning(world.scheduleTask(dot, (long)aura.getDotinterval(), TimeUnit.SECONDS, true));
         ra.setDot(dot);
      }

      ra.setRunning(world.scheduleTask(ra, (long)aura.getDuration(), TimeUnit.SECONDS));
      return ra;
   }

   private RemoveAura stackAura(World world, MonsterAI ai, Aura aura, RemoveAura ra, String fromtTarget, int damage) {
      ra.cancel();
      ra.incrementAuraCount();
      if (aura.eatsAura()) {
         int auratoeat = aura.getAuraToEat();
         if (ai.hasAura(auratoeat)) {
            RemoveAura rau = ai.getAura(auratoeat);
            rau.run();
         }
      }

      if (aura.getCategory().equals("d")) {
         DamageOverTime dot = ra.getDot();
         dot.setStack(ra.getAuraCount());
         dot.setMultiplier(aura.getDamageIncrease());
         dot.setRunning(world.scheduleTask(dot, (long)aura.getDotinterval(), TimeUnit.SECONDS, true));
         ra.setDot(dot);
      }

      ra.setRunning(world.scheduleTask(ra, (long)aura.getDuration(), TimeUnit.SECONDS));
      return ra;
   }

   private JSONObject applyAura(World world, MonsterAI ai, int auraId, String fromTarget, int damage) {
      JSONObject aInfo = new JSONObject();
      Aura aura = (Aura)world.auras.get(auraId);
      boolean auraExists = ai.hasAura(aura.getId());
      aInfo.put("cInf", fromTarget);
      aInfo.put("cmd", "aura+");
      aInfo.put("auras", aura.getAuraArray(!auraExists));
      aInfo.put("tInf", "m:" + ai.getMapId());
      RemoveAura ra;
      if (auraExists) {
         if (aura.canStack()) {
            ra = ai.getAura(aura.getId());
            ai.removeAura(ra);
            if (ra.getAuraCount() < aura.getMaxStack()) {
               ai.addAura(this.stackAura(world, ai, aura, ra, fromTarget, damage));
            } else {
               //stopdisaura stack
            }
         } else {
            ra = ai.getAura(aura.getId());
            ai.removeAura(ra);
            ai.addAura(this.renewAura(world, ai, aura, ra, fromTarget, damage));
         }

         return aInfo;
      } else {
         ra = ai.applyAura(aura);
         if (aura.eatsAura()) {
            int auratoeat = aura.getAuraToEat();
            if (ai.hasAura(auratoeat)) {
               RemoveAura rau = ai.getAura(auratoeat);
               rau.run();
            }
         }

         if (aura.getCategory().equals("d")) {
            DamageOverTime dot = new DamageOverTime(world, ai, damage, fromTarget, aura.canStack());
            dot.setRunning(world.scheduleTask(dot, (long)aura.getDotinterval(), TimeUnit.SECONDS, true));
            ra.setDot(dot);
         }

         return aInfo;
      }
   }

   private RemoveAura renewAura(World world, User user, Aura aura, RemoveAura ra, String fromTarget, int damage) {
      ra.cancel();
      if (!aura.effects.isEmpty()) {
         Stats dot = (Stats)user.properties.get("stats");
         dot.applyAuraStackEffects(aura, ra.getAuraCount());
      }

      ra.setRunning(world.scheduleTask(ra, (long)aura.getDuration(), TimeUnit.SECONDS));
      if (aura.getCategory().equals("d")) {
         DamageOverTime dot1 = ra.getDot();
         dot1.setStack(ra.getAuraCount());
         dot1.setMultiplier(aura.getDamageIncrease());
         dot1.setRunning(world.scheduleTask(dot1, 2L, TimeUnit.SECONDS, true));
         ra.setDot(dot1);
      }

      return ra;
   }

   private RemoveAura stackAura(World world, User user, Aura aura, RemoveAura ra, String fromtTarget, int damage) {
      ra.cancel();
      ra.incrementAuraCount();
      if (aura.eatsAura()) {
         int auratoeat = aura.getAuraToEat();
         if (world.users.hasAura(user, aura.getId())) {
            RemoveAura rau = world.users.getAura(user, auratoeat);
            rau.run();
         }
      }

      if (!aura.effects.isEmpty()) {
         Stats stats = (Stats)user.properties.get("stats");
         stats.applyAuraStackEffects(aura, ra.getAuraCount());
      }

      ra.setRunning(world.scheduleTask(ra, (long)aura.getDuration(), TimeUnit.SECONDS));
      if (aura.getCategory().equals("d")) {
         DamageOverTime dot1 = ra.getDot();
         dot1.setStack(ra.getAuraCount());
         dot1.setMultiplier(aura.getDamageIncrease());
         dot1.setRunning(world.scheduleTask(dot1, 2L, TimeUnit.SECONDS, true));
         ra.setDot(dot1);
      }

      return ra;
   }

   private JSONObject applyAura(World world, User user, int auraId, String fromTarget, int damage) {
      JSONObject aInfo = new JSONObject();
      Aura aura = (Aura)world.auras.get(auraId);
      boolean auraExists = world.users.hasAura(user, aura.getId());
      aInfo.put("cInf", fromTarget);
      aInfo.put("cmd", "aura+");
      aInfo.put("auras", aura.getAuraArray(!auraExists));
      aInfo.put("tInf", "p:" + user.getUserId());
      RemoveAura ra;
      if (auraExists) {
         Set auras;
         if (aura.canStack()) {
            ra = world.users.getAura(user, auraId);
            auras = (Set)user.properties.get("auras");
            auras.remove(ra);
            if (ra.getAuraCount() < aura.getMaxStack()) {
               auras.add(this.stackAura(world, user, aura, ra, fromTarget, damage));
            } else {
               //stopdis aura stack
            }
         } else {
            ra = world.users.getAura(user, auraId);
            auras = (Set)user.properties.get("auras");
            auras.remove(ra);
            auras.add(this.renewAura(world, user, aura, ra, fromTarget, damage));
         }

         return aInfo;
      } else {
         ra = world.users.applyAura(user, aura);
         if (!aura.effects.isEmpty()) {
            Stats stats = (Stats)user.properties.get("stats");
            stats.applyAuraStackEffects(aura, ra.getAuraCount());
         }

         if (aura.getCategory().equals("d")) {
            DamageOverTime dot = new DamageOverTime(world, user, damage, fromTarget, aura.canStack());
            dot.setMultiplier(aura.getDamageIncrease());
            dot.setStack(1);
            dot.setRunning(world.scheduleTask(dot, (long)aura.getDotinterval(), TimeUnit.SECONDS, true));
            ra.setDot(dot);
         }

         return aInfo;
      }
   }

   private String getSkillRefence(String str) {
      return str.contains(",") ? str.split(",")[0].split(">")[0] : str.split(">")[0];
   }

   private String parseTargetInfo(String str) {
      StringBuilder tb = new StringBuilder();
      if (str.contains(",")) {
         String[] multi = str.split(",");

         for(int i = 0; i < multi.length; ++i) {
            if (i != 0) {
               tb.append(",");
            }

            tb.append(multi[i].split(">")[1]);
         }
      } else {
         tb.append(str.split(">")[1]);
      }

      return tb.toString();
   }

   private boolean getRandomBoolean(int chance) {
      Random random = new Random();
      int randInt = random.nextInt(chance);
      return randInt == 0 || randInt == 1;
   }

   private int getRandomDamage(Stats stats, Skill skill) {
      return (int)((double)(rand.nextInt(1 + Math.abs(stats.getMaxDmg() - stats.getMinDmg())) + stats.getMinDmg()) * skill.getDamage());
   }
}
