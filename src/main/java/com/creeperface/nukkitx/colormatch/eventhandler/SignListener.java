package com.creeperface.nukkitx.colormatch.eventhandler;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.utils.TextFormat;
import com.creeperface.nukkitx.colormatch.ColorMatch;
import com.creeperface.nukkitx.colormatch.arena.Arena;

public class SignListener implements Listener {

    private final ColorMatch plugin;

    public SignListener(ColorMatch plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (e.isCancelled()) {
            return;
        }

        if (b instanceof BlockSignPost) {
            BlockEntitySign sign = (BlockEntitySign) b.getLevel().getBlockEntityIfLoaded(b);

            if (sign == null) {
                return;
            }

            String line1 = sign.getText()[0];

            if (line1 != null && TextFormat.clean(line1.toLowerCase()).trim().equals("[cm]")) {
                e.setCancelled();

                if (!p.hasPermission("colormatch.sign.use")) {
                    p.sendMessage(plugin.getLanguage().translateString("general.permission_message"));
                    return;
                }

                String name = TextFormat.clean(sign.getText()[1]).trim().toLowerCase();
                Arena arena = plugin.getPlayerArena(p);

                if (name.equals("leave")) {
                    if (arena != null) {
                        if (arena.isSpectator(p)) {
                            arena.removeSpectator(p);
                        } else {
                            arena.removeFromArena(p);
                        }
                    }
                } else {
                    arena = plugin.getArena(name);

                    if (arena != null) {
                        arena.addToArena(p);
                    } else {
                        p.sendMessage(plugin.getLanguage().translateString("general.arena_doesnt_exist"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        Player p = e.getPlayer();
        String line1 = e.getLine(0);

        if (TextFormat.clean(line1.toLowerCase()).trim().equals("[cm]")) {
            if (!p.hasPermission("colormatch.sign.create")) {
                e.setCancelled();
                p.sendMessage(plugin.getLanguage().translateString("general.permission_message"));
                return;
            }

            String line2 = TextFormat.clean(e.getLine(1)).toLowerCase();

            if (line2.equals("leave")) {
                e.setLine(0, "");
                e.setLine(1, ColorMatch.getPrefix());
                e.setLine(2, TextFormat.GRAY + "leave");
                e.setLine(3, "");
            } else {

                Arena arena = plugin.getArena(line2);

                if (arena == null) {
                    p.sendMessage(plugin.getLanguage().translateString("general.arena_doesnt_exist"));
                    e.setCancelled();
                    return;
                }

                e.setLine(0, ColorMatch.getPrefix());
                e.setLine(1, TextFormat.DARK_AQUA + arena.getName().substring(0, 1).toUpperCase() + arena.getName().substring(1).toLowerCase());
                e.setLine(2, TextFormat.BLUE + arena.getTypeString(arena.getType()));
                e.setLine(3, "");
            }

            p.sendMessage(plugin.getLanguage().translateString("general.create_sign"));
        }
    }
}
