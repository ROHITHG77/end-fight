package org.rgxprime.endfight.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.rgxprime.endfight.EndFightPlugin;

public class PlayerConnectionListener implements Listener {

    private final EndFightPlugin plugin;

    public PlayerConnectionListener(EndFightPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getCompetitionManager().isCompetitionActive()) {
            return;
        }

        if (plugin.getCompetitionManager().playerHasDragonEgg(player)) {
            plugin.getCompetitionManager().handleHolderDisconnect(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getCompetitionManager().isCompetitionActive()) {
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getCompetitionManager().playerHasDragonEgg(player)) {
                plugin.getCompetitionManager().handleHolderReconnect(player);
            }
        }, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!plugin.getCompetitionManager().isCompetitionActive()) {
            return;
        }

        plugin.getCompetitionManager().handlePlayerDeath(player);
    }
}
