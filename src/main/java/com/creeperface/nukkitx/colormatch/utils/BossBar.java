package com.creeperface.nukkitx.colormatch.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.*;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Utils;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CreeperFace
 */
public class BossBar {

    public static final int WITHER_ID = 52;

    private final Plugin plugin;
    @Getter
    private final Map<String, Player> players = new HashMap<>();
    public final long id;
    private int health = 1;
    private int maxHealth = 600;

    private final EntityMetadata metadata;
    private final BossEventPacket permanentPacket = new BossEventPacket();
    private final UpdateAttributesPacket attributesPacket = new UpdateAttributesPacket();

    public BossBar(Plugin plugin) {
        this.plugin = plugin;
        this.id = 2197383491L;
        this.metadata = (new EntityMetadata()).putString(Entity.DATA_NAMETAG, "").putLong(0, 196640L).putLong(38, -1L).putFloat(54, 0.0F).putFloat(55, 0.0F).putFloat(39, 0.0F);
        this.permanentPacket.bossEid = this.id;
        this.permanentPacket.type = 0;

        /*BossEventPacket permanentPacketUpdate = new BossEventPacket();
        permanentPacketUpdate.type = 1;
        permanentPacketUpdate.color = 0x4286f4;
        permanentPacketUpdate.overlay = 0x4286f4;*/

        this.attributesPacket.entityId = this.id;
        this.attributesPacket.entries = new Attribute[]{Attribute.getAttribute(4).setMaxValue(this.maxHealth).setValue(this.getHealth())};
        //this.attributesPacket.tryEncode();

        plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin, BossBar.this::update, 10, 10);
    }

    public void addPlayer(Player p) {
        this.players.put(p.getName(), p);
        Location pos = p.add(this.getDirectionVector(p).normalize().multiply(-15));
        AddEntityPacket pk = new AddEntityPacket();

        pk.type = WITHER_ID;
        pk.entityRuntimeId = this.id;
        pk.entityUniqueId = this.id;
        pk.x = (float) pos.x;
        pk.y = (float) (pos.y - 7);
        pk.z = (float) pos.z;
        pk.speedX = 0;
        pk.speedY = 0;
        pk.speedZ = 0;
        pk.yaw = 0;
        pk.pitch = 0;
        pk.metadata = this.metadata;

        UpdateAttributesPacket pk1 = new UpdateAttributesPacket();
        pk1.entityId = this.id;
        pk1.entries = new Attribute[]{Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(this.maxHealth).setValue(this.getHealth())};
        p.dataPacket(pk);
        p.dataPacket(pk1);
        p.dataPacket(this.attributesPacket);
        p.dataPacket(this.permanentPacket);
    }

    public void removePlayer(Player p) {
        this.removePlayer(p.getName());
        if (p.isOnline()) {
            RemoveEntityPacket pk = new RemoveEntityPacket();
            pk.eid = this.id;
            p.dataPacket(pk);
            BossEventPacket pk2 = new BossEventPacket();
            pk2.bossEid = this.id;
            pk2.type = 2;
            p.dataPacket(pk2);
        }
    }

    public void removePlayer(String name) {
        this.players.remove(name);
    }

    public void update() {
        this.players.values().forEach(this::update);
    }

    public void update(Player p) {
        this.update(p, false);
    }

    public void update(Player p, boolean respawn) {
        Location pos = p.add(this.getDirectionVector(p).normalize().multiply(-15));
        MoveEntityAbsolutePacket pk2 = new MoveEntityAbsolutePacket();
        pk2.eid = this.id;
        pk2.x = ((float) pos.x);
        pk2.y = ((float) (pos.y - 30));
        pk2.z = ((float) pos.z);
        pk2.yaw = ((float) p.yaw);
        pk2.headYaw = ((float) p.yaw);
        pk2.pitch = ((float) p.pitch);
        p.dataPacket(pk2);
        p.dataPacket(this.permanentPacket);
        p.dataPacket(this.attributesPacket);
    }

    public void setHealth(int health) {
        this.health = Math.max(health, 1);
    }

    public void setMaxHealth(int health) {
        this.maxHealth = Math.max(health, 1);
    }

    public void updateText(String text) {
        this.metadata.putString(Entity.DATA_NAMETAG, text);
    }

    public void updateInfo() {
        SetEntityDataPacket pk = new SetEntityDataPacket();
        pk.eid = this.id;
        pk.metadata = this.metadata;

        this.attributesPacket.entries[0].setMaxValue(this.maxHealth).setValue(this.getHealth());
        //this.attributesPacket.tryEncode();

        plugin.getServer().batchPackets(players.values().toArray(new Player[0]), new DataPacket[]{this.attributesPacket, pk});
    }

    public void updateHealth() {
        this.attributesPacket.entries[0].setMaxValue(this.maxHealth).setValue(this.getHealth());
        //this.attributesPacket.tryEncode();

        Server.broadcastPacket(this.players.values(), this.attributesPacket);
    }

    public Vector3 getDirectionVector(Player p) {
        double pitch = 1.5707963267948966D;
        double yaw = (p.getYaw() + Utils.rand(-10, 10) + 90) * 3.141592653589793D / 180.0D;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double z = Math.sin(pitch) * Math.sin(yaw);
        double y = Math.cos(pitch);
        return (new Vector3(x, y, z)).normalize();
    }

    public long getId() {
        return this.id;
    }

    public int getHealth() {
        return this.health;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }
}
