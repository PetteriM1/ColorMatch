package com.creeperface.nukkitx.colormatch.utils;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.ConfigSection;
import com.creeperface.nukkitx.colormatch.ColorMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Reward {

    private ColorMatch plugin;

    private double money;

    private Item[] items;

    private boolean enabled;

    public void init(ColorMatch plugin, ConfigSection section) {
        this.plugin = plugin;

        if (section == null || section.isEmpty()) {
            enabled = false;
            return;
        }

        enabled = section.getBoolean("enable");

        if (!enabled) {
            return;
        }

        money = section.getDouble("money");

        List<Map> items = section.getMapList("items");

        List<Item> itemList = new ArrayList<>();

        for (Map map : items) {
            int id = (int) map.getOrDefault("id", 0);
            int damage = (int) map.getOrDefault("damage", 0);

            if (id == 0) {
                continue;
            }

            itemList.add(Item.get(id, damage));
        }

        this.items = itemList.toArray(new Item[0]);
    }

    public void give(Player... players) {
        if (!enabled) {
            return;
        }

        for (Player p : players) {
            if (money > 0) {
                plugin.getEconomy().addMoney(p, money);
            }

            p.getInventory().addItem(items);
        }
    }
}
