package com.creeperface.nukkitx.colormatch.stats;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import com.creeperface.nukkitx.colormatch.ColorMatch;

public class YamlStatsProvider implements StatsProvider {

    private Config cfg;

    @Override
    public boolean init(ColorMatch plugin) {
        cfg = new Config(plugin.getDataFolder() + "/stats.yml", Config.YAML);
        Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(plugin, new SaveTask(this), 6000, 6000);
        return true;
    }

    @Override
    public void updateStats(String name, boolean win, int rounds) {
        ConfigSection data = cfg.getSection(name.toLowerCase());

        if (win) {
            data.set("wins", data.getInt("wins") + 1);
        } else {
            data.set("deaths", data.getInt("deaths") + 1);
        }

        data.set("rounds", data.getInt("rounds") + rounds);
    }

    @Override
    public boolean createNewUser(String name) {
        name = name.toLowerCase();
        if (cfg.exists(name)) {
            return false;
        }

        ConfigSection data = new ConfigSection();
        data.set("wins", 0);
        data.set("deaths", 0);
        data.set("rounds", 0);

        cfg.set(name, data);
        return true;
    }

    @Override
    public void sendStats(String p, CommandSender receiver) {
        ConfigSection data = cfg.getSection(p.toLowerCase());

        ColorMatch plugin = ColorMatch.getInstance();

        if (data.isEmpty()) {
            receiver.sendMessage(plugin.getLanguage().translateString("commands.failure.stats_player", p));
            return;
        }

        String msg = plugin.getLanguage().translateString("commands.success.stats", p);
        msg += '\n' + plugin.getLanguage().translateString("stats.wins", data.getString("wins"));
        msg += '\n' + plugin.getLanguage().translateString("stats.deaths", data.getString("deaths"));
        msg += '\n' + plugin.getLanguage().translateString("stats.rounds", data.getString("rounds"));

        receiver.sendMessage(msg);
    }

    private static class SaveTask implements Runnable {

        YamlStatsProvider plugin;

        SaveTask(YamlStatsProvider plugin) {
            this.plugin = plugin;
        }

        public void run() {
            plugin.cfg.save(true);
        }
    }

    @Override
    public void onDisable() {
        cfg.save();
    }
}
