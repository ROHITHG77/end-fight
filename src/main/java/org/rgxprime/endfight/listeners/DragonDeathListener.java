package org.rgxprime.endfight.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.rgxprime.endfight.EndFightPlugin;

public class DragonDeathListener implements Listener {

    private final EndFightPlugin plugin;

    public DragonDeathListener(EndFightPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) {
            return;
        }

        if (plugin.getDataManager().isDragonDefeated()) {
            return;
        }

        World endWorld = dragon.getWorld();
        if (endWorld.getEnvironment() != World.Environment.THE_END) {
            return;
        }

        DragonBattle battle = endWorld.getEnderDragonBattle();
        if (battle != null && battle.hasBeenPreviouslyKilled()) {
            return;
        }

        plugin.getDataManager().setDragonDefeated(true);
        plugin.getMessageManager().broadcast("dragon-defeated");

        new BukkitRunnable() {
            @Override
            public void run() {
                Location eggLoc = plugin.getCompetitionManager().spawnEggInOverworld();

                if (eggLoc != null) {
                    plugin.getMessageManager().broadcast("egg-spawned",
                            "%x%", String.valueOf(eggLoc.getBlockX()),
                            "%y%", String.valueOf(eggLoc.getBlockY()),
                            "%z%", String.valueOf(eggLoc.getBlockZ()));
                }
            }
        }.runTaskLater(plugin, 60L);
    }
}
