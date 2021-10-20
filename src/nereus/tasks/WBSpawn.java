//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nereus.tasks;

import nereus.ai.MonsterAI;
import nereus.config.ConfigData;
import nereus.db.objects.Monster;
import nereus.db.objects.WorldBoss;
import nereus.discord.Webhook;
import nereus.discord.Webhook.EmbedObject;
import nereus.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.awt.Color;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.sf.json.JSONObject;

public class WBSpawn implements Runnable {
    private World world;
    public HashMap<Integer, Long> deathTimes = new HashMap();
    public HashMap<Integer, Long> spawnTimes = new HashMap();

    public WBSpawn(World world) {
        this.world = world;
        Iterator var2 = world.worldbosses.entrySet().iterator();

        while(var2.hasNext()) {
            Entry<Integer, WorldBoss> entry = (Entry)var2.next();
            this.deathTimes.put(((WorldBoss)entry.getValue()).monsterId, System.currentTimeMillis());
            this.spawnTimes.put(((WorldBoss)entry.getValue()).monsterId, System.currentTimeMillis());
        }

        world.db.jdbc.run("UPDATE monsters_bosses SET DeathTime = NOW()", new Object[0]);
        SmartFoxServer.log.info("WBSpawn intialized.");
    }

    public void run() {
        try {
            Iterator var1 = this.world.worldbosses.entrySet().iterator();

            while(var1.hasNext()) {
                Entry<Integer, WorldBoss> entry = (Entry)var1.next();
                new SimpleDateFormat("MMMM dd, yyyy (hh:mm:ss a)");
                this.world.now = Calendar.getInstance();
                this.world.now.add(14, -((int)(System.currentTimeMillis() - (Long)this.deathTimes.get(((WorldBoss)entry.getValue()).monsterId))));
                this.world.now.add(14, (int)((WorldBoss)entry.getValue()).spawnInterval);
                if (TimeUnit.MILLISECONDS.toMinutes((Long)this.deathTimes.get(((WorldBoss)entry.getValue()).monsterId) + ((WorldBoss)entry.getValue()).spawnInterval - System.currentTimeMillis()) == 4L && !((WorldBoss)entry.getValue()).isNotified) {
                    ((WorldBoss)entry.getValue()).isNotified = true;
                }

                if (System.currentTimeMillis() - (Long)this.deathTimes.get(((WorldBoss)entry.getValue()).monsterId) > ((WorldBoss)entry.getValue()).spawnInterval && !((WorldBoss)entry.getValue()).isActive) {
                    ((WorldBoss)entry.getValue()).isNotified = false;
                    ((WorldBoss)entry.getValue()).isActive = true;
                    ((WorldBoss)entry.getValue()).roomNumber = 1;
                    String room = ((String)this.world.worldBossMaps.get(((WorldBoss)entry.getValue()).mapId)).toLowerCase() + "-" + ((WorldBoss)entry.getValue()).roomNumber;
                    Room bossRoom = this.world.zone.getRoomByName(room);
                    this.spawnTimes.put(((WorldBoss)entry.getValue()).monsterId, System.currentTimeMillis());
                    if (bossRoom == null) {
                        bossRoom = this.world.rooms.createRoom(room);
                        SmartFoxServer.log.info("Created room");
                    } else {
                        SmartFoxServer.log.info("Room existed");
                    }

                    Map<Integer, MonsterAI> monsters = (ConcurrentHashMap)bossRoom.properties.get("monsters");
                    Iterator var7 = monsters.values().iterator();
                    if (var7.hasNext()) {
                        MonsterAI actMon = (MonsterAI)var7.next();
                        JSONObject umsg = new JSONObject();
                        umsg.put("cmd", "WorldBossInvite");
                        umsg.put("room", room);
                        umsg.put("monName", ((Monster)this.world.monsters.get(((WorldBoss)entry.getValue()).monsterId)).getName());
                        umsg.put("monLevel", ((Monster)this.world.monsters.get(((WorldBoss)entry.getValue()).monsterId)).getLevel());
                        umsg.put("monFile", ((Monster)this.world.monsters.get(((WorldBoss)entry.getValue()).monsterId)).getFile());
                        umsg.put("monLink", ((Monster)this.world.monsters.get(((WorldBoss)entry.getValue()).monsterId)).getLinkage());
                        umsg.put("spawnTime", this.spawnTimes.get(((WorldBoss)entry.getValue()).monsterId));
                        umsg.put("timeLimit", ((WorldBoss)entry.getValue()).timeLimit);
                        umsg.put("timestamp", System.currentTimeMillis());
                        LinkedList listOfChannels = this.world.zone.getAllUsersInZone();
                        Iterator var11 = listOfChannels.iterator();

                        while(var11.hasNext()) {
                            Object temp = var11.next();
                            User tgt = ExtensionHelper.instance().getUserByChannel((SocketChannel)temp);
                            if (tgt != null) {
                                this.world.send(umsg, tgt);
                            }
                        }

                        this.world.scheduleTask(new MonsterRespawn(this.world, actMon), 3L, TimeUnit.SECONDS);
                    }

                    this.world.db.jdbc.run("UPDATE monsters_bosses SET SpawnTime = NOW() WHERE MonsterID = ? AND MapID = ?", new Object[]{((WorldBoss)entry.getValue()).monsterId, ((WorldBoss)entry.getValue()).mapId});
                }
            }
        } catch (Exception var15) {
        }

    }
}
