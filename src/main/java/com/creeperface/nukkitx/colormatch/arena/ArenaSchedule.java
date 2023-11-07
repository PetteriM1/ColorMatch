package com.creeperface.nukkitx.colormatch.arena;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Attribute;
import cn.nukkit.level.Sound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ArenaSchedule implements Runnable {

    private final Arena plugin;

    public int time = 0;
    public int colorTime = 0;
    public int startTime = 0;

    @Getter
    public int id = 0;

    public boolean floor = true;
    public boolean cooldown = false;

    public int floorResetedTick;

    public void run() {
        if (plugin.getPhase() == Arena.PHASE_GAME) {
            game();
        } else {
            lobby();
        }
    }

    private void lobby() {
        if (plugin.starting) {
            int maxTime = plugin.plugin.conf.getStartTime();

            startTime++;

            plugin.players.values().forEach((Player p) -> p.setExperience(0, maxTime - startTime));

            if (startTime >= plugin.plugin.conf.getStartTime()) {
                plugin.start();
                startTime = 0;
            }
        }
    }

    private void game() {
        if (time >= plugin.plugin.conf.getMaxGameTime()) {
            plugin.players.values().forEach((Player p) -> p.level.addSound(p, Sound.RANDOM_FIZZ, 1f, 1f, p));
            plugin.stop();
            return;
        }

        int interval = plugin.getColorChangeInterval();

        if (Server.getInstance().suomiCraftPEMode()) {
            if (time > interval << 2) {
                interval--;
            }
            if (cooldown) {
                cooldown = false;
                plugin.sendNewColor();
                int finalInterval = interval;
                plugin.players.values().forEach((Player p) -> p.setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE).setValue(1f)));
                plugin.players.values().forEach((Player p) -> p.setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE_LEVEL).setValue(finalInterval)));
                return;
            }
        }

        if (colorTime > 0 && (colorTime % interval) == 0) {
            if (floor) {
                plugin.removeFloor();
                floor = false;
                //this.plugin.bossBar.setHealth(interval * 10);
                //this.plugin.bossBar.updateInfo();
            } else {
                floor = true;
                cooldown = true;
                plugin.selectNewColor();
                plugin.resetFloor();
                floorResetedTick = plugin.plugin.getServer().getTick();
            }

            plugin.players.values().forEach((Player p) -> p.setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE).setValue(0)));
            plugin.players.values().forEach((Player p) -> p.setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE_LEVEL).setValue(0)));
        } else if (floor) {
            int finalInterval = interval;
            plugin.players.values().forEach((Player p) -> p.setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE).setValue((float) (finalInterval - (colorTime % finalInterval)) / finalInterval)));
            plugin.players.values().forEach((Player p) -> p.setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE_LEVEL).setValue(finalInterval - (colorTime % finalInterval))));
        }

//        if (floor) {
//            update = true;
//            this.plugin.bossBar.setHealth((interval - 1) - (colorTime % (interval - 1)));
//        }

        time++;
        colorTime++;
    }
}
