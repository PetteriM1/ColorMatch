package com.creeperface.nukkitx.colormatch.arena;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.player.*;
import cn.nukkit.event.player.PlayerInteractEvent.Action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

class ArenaListener implements Listener {

    private final Arena plugin;

    public ArenaListener(Arena plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p)) {
            plugin.removeFromArena(p);
        } else if (plugin.isSpectator(p)) {
            plugin.removeSpectator(p);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();

        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player p = e.getPlayer();

        if (plugin.inArena(p)) {
            e.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p) || plugin.isSpectator(p)) {
            e.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p)) {
            e.setCancelled();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p)) {
            e.setCancelled();
        }
    }

    /*@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onChat(PlayerChatEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();

        if (e.isCancelled()) {
            return;
        }

        Set<CommandSender> recipients = e.getRecipients();
        String prefix = "";

        if (plugin.inArena(p)) {
            prefix = plugin.plugin.conf.getGameChatFormat();
            /*String lastColor = "f";

            if(utils.getLastColor(msg).toLowerCase().equals("f")){
                lastColor = utils.getLastColor(p.getDisplayName().toLowerCase());
            }

            if(e.getMessage().lastIndexOf() !p.getDisplayName().toLowerCase().trim().substring(Math.max(0, p.getDisplayName().length() - 5)).contains(TextFormat.WHITE)) {
                e.setMessage(TextFormat.GRAY + e.getMessage());
            }*/
        /*} else if (plugin.isSpectator(p)) {
            prefix = plugin.plugin.conf.getSpectatorChatFormat();
        } else {
            for (CommandSender sender : new HashSet<>(recipients)) {
                if (!(sender instanceof Player)) {
                    continue;
                }

                Player s = (Player) sender;

                if (!plugin.inArena(s) && !plugin.isSpectator(s)) {
                    continue;
                }

                recipients.remove(sender);
            }
            return;
        }

        e.setCancelled();
        plugin.messageArenaPlayers(prefix.replaceAll("\\{PLAYER}", p.getDisplayName()).replaceAll("\\{MESSAGE}", e.getMessage()));
    }*/

    private static final Set<DamageCause> allowedCauses = new HashSet<>(Arrays.asList(DamageCause.VOID, DamageCause.FALL, DamageCause.FIRE, DamageCause.FIRE_TICK, DamageCause.LAVA, DamageCause.CONTACT));

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        Player p;
        DamageCause cause = e.getCause();

        if (entity instanceof Player) {
            p = (Player) entity;

            /*if (plugin.isSpectator(p)) {
                e.setCancelled();
                return;
            }*/

            if (plugin.inArena(p)) {
                if (plugin.phase == Arena.PHASE_LOBBY) {
                    e.setCancelled();
                    return;
                } else if (e instanceof EntityDamageByEntityEvent) {
                    if (!plugin.aggressive) {
                        e.setCancelled();
                        return;
                    }

                    Entity entityDamager = ((EntityDamageByEntityEvent) e).getDamager();

                    if (entityDamager instanceof Player) {
                        Player dmgr = (Player) entityDamager;

                        if (plugin.inArena(dmgr)) {
                            if (plugin.getFieldIndexFromPos(dmgr) != plugin.getFieldIndexFromPos(p)) {
                                e.setCancelled();
                            }
                            return;
                        }
                    }
                } else if (!allowedCauses.contains(cause)) {
                    e.setCancelled();
                    return;
                } else if (e.getFinalDamage() >= p.getHealth()) {
                    e.setCancelled();
                    plugin.onDeath(p);
                    return;
                }
            }
        }

        if (e instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                Player damager = (Player) ((EntityDamageByEntityEvent) e).getDamager();

                if ((plugin.inArena(damager) || plugin.isSpectator(damager))) {
                    e.setCancelled();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onFoodChange(PlayerFoodLevelChangeEvent e) {
        Player p = e.getPlayer();

        if (e.getFoodLevel() >= p.getFoodData().getLevel()) {
            return;
        }

        if (plugin.inArena(p)) {
            e.setCancelled(true);
        }
    }

    private static final Pattern FILTER_EMPTY_PATTERN = Pattern.compile("\\s+");

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        if (plugin.inArena(p) || plugin.isSpectator(p)) {
            String cmd = FILTER_EMPTY_PATTERN.matcher(e.getMessage().toLowerCase()).replaceAll("");

            if (!p.isOp() && !cmd.startsWith("/cm") && !cmd.startsWith("/hub") && !cmd.startsWith("/spawn") && !cmd.startsWith("/lobby")) {
                p.sendMessage(plugin.plugin.getLanguage().translateString("game.commands"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();

        if (p == null) {
            return;
        }

        if (e.getFrom().getLevel().equals(plugin.level) && !e.getTo().getLevel().equals(plugin.level)) {
            if (plugin.inArena(p) || plugin.isSpectator(p)) {
                plugin.removeFromArena(p, true, false);
            }
        }
    }
}
