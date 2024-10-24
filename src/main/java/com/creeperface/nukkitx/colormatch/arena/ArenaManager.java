package com.creeperface.nukkitx.colormatch.arena;

import cn.nukkit.Player;
import cn.nukkit.block.BlockWallSign;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;

public abstract class ArenaManager extends Configuration {

    protected Arena plugin;

    public boolean inArena(Player p) {
        return plugin.players.containsKey(p.getName());
    }

    public boolean isSpectator(Player p) {
        return plugin.spectators.containsKey(p.getName());
    }

    public void checkLobby() {
        if (plugin.players.size() >= plugin.plugin.conf.getMinPlayers()) {
            plugin.starting = true;
        }
    }

    public void resetPlayer(Player p) {
        p.extinguish();
        p.removeAllEffects();
        p.getInventory().clearAll();
        p.setExperience(0, 0);
        p.setHealth(20);
        p.getFoodData().setFoodSaturationLevel(20);
        p.getFoodData().setLevel(20);
    }

    public void messageArenaPlayers(String msg) {
        plugin.players.values().forEach((Player p) -> p.sendMessage(msg));
        plugin.spectators.values().forEach((Player p) -> p.sendMessage(msg));
    }

    /*public boolean checkAlive(){
        return plugin.players.size() > 0;
    }*/

    public String getTypeString(int type) {
        switch (type) {
            case TYPE_NORMAL:
                return "normal";
            case TYPE_FURIOUS:
                return "furious";
            case TYPE_STONED:
                return "stoned";
            case TYPE_BLIND:
                return "blind";
        }

        return "";
    }

    protected Effect getGameEffect() {
        switch (getType()) {
            case TYPE_BLIND:
                return Effect.getEffect(Effect.BLINDNESS).setDuration(999999999).setVisible(false);
            case TYPE_FURIOUS:
                return Effect.getEffect(Effect.SPEED).setAmplifier(9).setDuration(999999999).setVisible(false);
            case TYPE_STONED:
                return Effect.getEffect(Effect.NAUSEA).setDuration(999999999).setVisible(false);
        }

        return null;
    }

    public void updateJoinSign() {
        if (getJoinSign().x == 0 && getJoinSign().y == 0 && getJoinSign().z == 0) return;

        BlockEntitySign sign = (BlockEntitySign) getJoinSign().level.getBlockEntity(getJoinSign());

        if (sign == null) {
            getJoinSign().level.setBlock(getJoinSign(), new BlockWallSign(), true, false);

            CompoundTag nbt = (new CompoundTag()).putString("id", "Sign").putInt("x", (int) getJoinSign().x).putInt("y", (int) getJoinSign().y).putInt("z", (int) getJoinSign().z).putString("Text1", "").putString("Text2", "").putString("Text3", "").putString("Text4", "");

            sign = new BlockEntitySign(getJoinSign().level.getChunk((int) getJoinSign().x >> 4, (int) getJoinSign().z >> 4, true), nbt);
        }

        sign.setText(TextFormat.GRAY + "[" + TextFormat.DARK_AQUA + plugin.name + TextFormat.GRAY + ']', "" + TextFormat.GRAY + plugin.players.size() + '/' + plugin.plugin.conf.getMaxPlayers(), TextFormat.BLUE + getTypeString(getType()), plugin.enabled ? plugin.phase == Arena.PHASE_GAME ? TextFormat.RED + "running" : TextFormat.GREEN + "lobby" : TextFormat.RED + "disabled");
    }

    public void checkAlive() {
        if (plugin.players.size() <= 1) {
            plugin.endGame();
        }
    }

    public int getFieldIndexFromPos(Vector3 pos) {
        AxisAlignedBB floor = getFloor();

        pos = pos.clone();
        pos.y = floor.getMaxY();

        if (!floor.isVectorInside(pos)) {
            return -1;
        }

        int edgeCount = (getRadius() << 1) + 1;

        pos = pos.subtract(floor.getMinX(), 0, floor.getMinZ());

        int indexX = pos.getFloorX() / edgeCount;
        int indexZ = pos.getFloorZ() / edgeCount;

        return (indexZ * edgeCount) + indexX;
    }
}
