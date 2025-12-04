package org.rgxprime.endfight.listeners;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.rgxprime.endfight.EndFightPlugin;

public class EggListener implements Listener {

    private final EndFightPlugin plugin;

    public EggListener(EndFightPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEggClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.DRAGON_EGG) {
            return;
        }

        if (block.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (!plugin.getDataManager().isDragonDefeated() || plugin.getDataManager().isCompetitionCompleted()) {
            return;
        }

        event.setCancelled(true);

        block.setType(Material.AIR);

        Player player = event.getPlayer();
        ItemStack egg = new ItemStack(Material.DRAGON_EGG, 1);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(egg);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), egg);
        }

        if (!plugin.getCompetitionManager().isCompetitionActive()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getCompetitionManager().playerHasDragonEgg(player)) {
                    plugin.getCompetitionManager().startCompetition(player);
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEggBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.DRAGON_EGG) {
            return;
        }

        if (event.getBlock().getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (!plugin.getDataManager().isDragonDefeated() || plugin.getDataManager().isCompetitionCompleted()) {
            return;
        }

        event.setDropItems(false);

        Player player = event.getPlayer();
        ItemStack egg = new ItemStack(Material.DRAGON_EGG, 1);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(egg);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), egg);
        }

        if (!plugin.getCompetitionManager().isCompetitionActive()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getCompetitionManager().playerHasDragonEgg(player)) {
                    plugin.getCompetitionManager().startCompetition(player);
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Item item = event.getItem();
        if (item.getItemStack().getType() != Material.DRAGON_EGG) {
            return;
        }

        if (item.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (!plugin.getDataManager().isDragonDefeated() || plugin.getDataManager().isCompetitionCompleted()) {
            return;
        }

        if (!plugin.getCompetitionManager().isCompetitionActive()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getCompetitionManager().playerHasDragonEgg(player)) {
                    plugin.getCompetitionManager().startCompetition(player);
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() != Material.DRAGON_EGG) {
            return;
        }

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (!plugin.getDataManager().isDragonDefeated() || plugin.getDataManager().isCompetitionCompleted()) {
            return;
        }

        if (!plugin.getCompetitionManager().isCompetitionActive()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getCompetitionManager().playerHasDragonEgg(player)) {
                    plugin.getCompetitionManager().startCompetition(player);
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEggDespawn(ItemDespawnEvent event) {
        if (event.getEntity().getItemStack().getType() != Material.DRAGON_EGG) {
            return;
        }

        if (plugin.getCompetitionManager().isCompetitionActive()) {
            event.setCancelled(true);
        }
    }
}
