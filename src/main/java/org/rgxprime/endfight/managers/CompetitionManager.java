package org.rgxprime.endfight.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.rgxprime.endfight.EndFightPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CompetitionManager {

    private final EndFightPlugin plugin;
    private final Map<UUID, Long> teleportCooldowns;
    private final Random random;

    private Location eggLocation;
    private UUID eggHolderUUID;
    private String lastKnownHolderName;
    private boolean holderOffline;
    private boolean teleportPhaseActive;
    private boolean competitionActive;

    private BukkitTask teleportPhaseTask;
    private BukkitTask competitionTask;
    private BukkitTask eggCheckTask;

    public CompetitionManager(EndFightPlugin plugin) {
        this.plugin = plugin;
        this.teleportCooldowns = new HashMap<>();
        this.random = new Random();
        this.teleportPhaseActive = false;
        this.competitionActive = false;
        this.holderOffline = false;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, this::restoreCompetitionState, 20L);
    }

    private void restoreCompetitionState() {
        DataManager data = plugin.getDataManager();
        
        if (!data.isCompetitionActive() || data.isCompetitionCompleted()) {
            return;
        }

        long now = System.currentTimeMillis();
        long competitionEnd = data.getCompetitionEndTime();
        long teleportEnd = data.getTeleportPhaseEndTime();

        if (now >= competitionEnd) {
            endCompetition();
            return;
        }

        this.competitionActive = true;
        this.eggHolderUUID = data.getEggHolderUUID();
        this.lastKnownHolderName = data.getEggHolderName();
        this.eggLocation = data.getEggLocation();
        this.teleportPhaseActive = now < teleportEnd;

        long remainingTeleportPhase = teleportEnd - now;
        long remainingCompetition = competitionEnd - now;

        if (teleportPhaseActive && remainingTeleportPhase > 0) {
            teleportPhaseTask = new BukkitRunnable() {
                @Override
                public void run() {
                    teleportPhaseActive = false;
                    plugin.getMessageManager().broadcast("teleport-phase-end");
                }
            }.runTaskLater(plugin, remainingTeleportPhase / 50);
        }

        competitionTask = new BukkitRunnable() {
            @Override
            public void run() {
                endCompetition();
            }
        }.runTaskLater(plugin, remainingCompetition / 50);

        startEggHolderTracking();
        
        plugin.getLogger().info("Restored competition state from previous session.");
    }

    public Location spawnEggInOverworld() {
        World overworld = Bukkit.getWorlds().stream()
                .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);

        if (overworld == null) {
            plugin.getLogger().severe("Could not find overworld!");
            return null;
        }

        int minDistance = plugin.getConfig().getInt("egg-spawn.min-distance-from-spawn", 500);
        int maxDistance = plugin.getConfig().getInt("egg-spawn.max-distance-from-spawn", 5000);

        Location spawnLoc = overworld.getSpawnLocation();
        int attempts = 0;
        int maxAttempts = 50;

        while (attempts < maxAttempts) {
            double angle = random.nextDouble() * 2 * Math.PI;
            int distance = minDistance + random.nextInt(maxDistance - minDistance);

            int x = spawnLoc.getBlockX() + (int) (Math.cos(angle) * distance);
            int z = spawnLoc.getBlockZ() + (int) (Math.sin(angle) * distance);

            Chunk chunk = overworld.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) {
                chunk.load(true);
            }

            int y = overworld.getHighestBlockYAt(x, z) + 1;
            Location potentialLoc = new Location(overworld, x, y, z);

            if (isSafeLocation(potentialLoc)) {
                eggLocation = potentialLoc;
                overworld.getBlockAt(eggLocation).setType(Material.DRAGON_EGG);
                plugin.getDataManager().setEggLocation(eggLocation);
                return eggLocation;
            }

            attempts++;
        }

        int x = spawnLoc.getBlockX() + minDistance;
        int z = spawnLoc.getBlockZ() + minDistance;
        
        Chunk chunk = overworld.getChunkAt(x >> 4, z >> 4);
        if (!chunk.isLoaded()) {
            chunk.load(true);
        }
        
        int y = overworld.getHighestBlockYAt(x, z) + 1;
        eggLocation = new Location(overworld, x, y, z);
        overworld.getBlockAt(eggLocation).setType(Material.DRAGON_EGG);
        plugin.getDataManager().setEggLocation(eggLocation);
        return eggLocation;
    }

    private boolean isSafeLocation(Location loc) {
        Block blockBelow = loc.clone().subtract(0, 1, 0).getBlock();
        Material belowType = blockBelow.getType();
        return belowType.isSolid() &&
                belowType != Material.LAVA &&
                belowType != Material.WATER &&
                belowType != Material.MAGMA_BLOCK &&
                loc.getBlock().getType() == Material.AIR;
    }

    public void startCompetition(Player eggHolder) {
        this.eggHolderUUID = eggHolder.getUniqueId();
        this.lastKnownHolderName = eggHolder.getName();
        this.holderOffline = false;
        this.competitionActive = true;
        this.teleportPhaseActive = true;
        this.teleportCooldowns.clear();

        int teleportPhaseDuration = plugin.getConfig().getInt("competition.teleport-phase-duration", 60);
        int competitionDuration = plugin.getConfig().getInt("competition.competition-duration", 120);

        long now = System.currentTimeMillis();
        plugin.getDataManager().setCompetitionActive(true);
        plugin.getDataManager().setCompetitionStartTime(now);
        plugin.getDataManager().setTeleportPhaseEndTime(now + (teleportPhaseDuration * 1000L));
        plugin.getDataManager().setCompetitionEndTime(now + (competitionDuration * 1000L));
        plugin.getDataManager().setEggHolderUUID(eggHolderUUID);
        plugin.getDataManager().setEggHolderName(lastKnownHolderName);

        plugin.getMessageManager().broadcast("egg-picked-up", "%player%", eggHolder.getName());
        plugin.getMessageManager().broadcast("teleport-phase-start", "%time%", String.valueOf(teleportPhaseDuration));

        startEggHolderTracking();

        teleportPhaseTask = new BukkitRunnable() {
            @Override
            public void run() {
                teleportPhaseActive = false;
                plugin.getMessageManager().broadcast("teleport-phase-end");
            }
        }.runTaskLater(plugin, teleportPhaseDuration * 20L);

        competitionTask = new BukkitRunnable() {
            @Override
            public void run() {
                endCompetition();
            }
        }.runTaskLater(plugin, competitionDuration * 20L);
    }

    private void startEggHolderTracking() {
        if (eggCheckTask != null) {
            eggCheckTask.cancel();
        }
        
        eggCheckTask = new BukkitRunnable() {
            private UUID lastBroadcastHolder = eggHolderUUID;
            
            @Override
            public void run() {
                if (!competitionActive) {
                    cancel();
                    return;
                }

                UUID newHolder = null;
                String newHolderName = null;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (playerHasDragonEgg(player)) {
                        newHolder = player.getUniqueId();
                        newHolderName = player.getName();
                        break;
                    }
                }

                if (newHolder != null) {
                    holderOffline = false;
                    if (!newHolder.equals(eggHolderUUID)) {
                        eggHolderUUID = newHolder;
                        lastKnownHolderName = newHolderName;
                        plugin.getDataManager().setEggHolderUUID(eggHolderUUID);
                        plugin.getDataManager().setEggHolderName(lastKnownHolderName);
                        
                        if (!newHolder.equals(lastBroadcastHolder)) {
                            plugin.getMessageManager().broadcast("egg-picked-up", "%player%", newHolderName);
                            lastBroadcastHolder = newHolder;
                        }
                    }
                } else if (!holderOffline) {
                    eggHolderUUID = null;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public boolean playerHasDragonEgg(Player player) {
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                return true;
            }
        }
        
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand.getType() == Material.DRAGON_EGG) {
            return true;
        }
        
        ItemStack cursor = player.getItemOnCursor();
        if (cursor.getType() == Material.DRAGON_EGG) {
            return true;
        }
        
        Inventory enderChest = player.getEnderChest();
        for (ItemStack item : enderChest.getContents()) {
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                return true;
            }
        }
        
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType().name().contains("SHULKER_BOX")) {
                if (item.hasItemMeta() && item.getItemMeta() instanceof org.bukkit.inventory.meta.BlockStateMeta blockMeta) {
                    if (blockMeta.getBlockState() instanceof Container container) {
                        for (ItemStack shulkerItem : container.getInventory().getContents()) {
                            if (shulkerItem != null && shulkerItem.getType() == Material.DRAGON_EGG) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }

    public void endCompetition() {
        competitionActive = false;
        teleportPhaseActive = false;

        cancelAllTasks();

        Player winner = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerHasDragonEgg(player)) {
                winner = player;
                break;
            }
        }

        if (winner != null) {
            plugin.getMessageManager().broadcast("competition-winner", "%player%", winner.getName());
            plugin.getDataManager().setWinner(winner.getName());
        } else if (holderOffline && lastKnownHolderName != null) {
            plugin.getMessageManager().broadcast("competition-winner", "%player%", lastKnownHolderName);
            plugin.getDataManager().setWinner(lastKnownHolderName);
        } else {
            plugin.getMessageManager().broadcast("competition-no-winner");
        }

        plugin.getDataManager().setCompetitionActive(false);
        plugin.getDataManager().setCompetitionCompleted(true);
    }

    public void handleHolderDisconnect(Player player) {
        if (eggHolderUUID != null && eggHolderUUID.equals(player.getUniqueId())) {
            if (playerHasDragonEgg(player)) {
                holderOffline = true;
                lastKnownHolderName = player.getName();
                plugin.getDataManager().setEggHolderName(lastKnownHolderName);
                plugin.getMessageManager().broadcast("egg-holder-offline", "%player%", player.getName());
            }
        }
    }

    public void handleHolderReconnect(Player player) {
        if (playerHasDragonEgg(player)) {
            eggHolderUUID = player.getUniqueId();
            lastKnownHolderName = player.getName();
            holderOffline = false;
            plugin.getDataManager().setEggHolderUUID(eggHolderUUID);
            plugin.getDataManager().setEggHolderName(lastKnownHolderName);
            plugin.getMessageManager().broadcast("egg-holder-online", "%player%", player.getName());
        }
    }

    public void handlePlayerDeath(Player player) {
        if (eggHolderUUID != null && eggHolderUUID.equals(player.getUniqueId())) {
            holderOffline = false;
            eggHolderUUID = null;
        }
    }

    public boolean canTeleport(Player player) {
        if (!teleportPhaseActive) {
            return false;
        }

        long cooldownTime = plugin.getConfig().getInt("competition.eggtp-cooldown", 20) * 1000L;
        Long lastUse = teleportCooldowns.get(player.getUniqueId());

        if (lastUse == null) {
            return true;
        }

        return System.currentTimeMillis() - lastUse >= cooldownTime;
    }

    public int getRemainingCooldown(Player player) {
        long cooldownTime = plugin.getConfig().getInt("competition.eggtp-cooldown", 20) * 1000L;
        Long lastUse = teleportCooldowns.get(player.getUniqueId());

        if (lastUse == null) {
            return 0;
        }

        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    public void setTeleportCooldown(Player player) {
        teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public Player getEggHolder() {
        if (eggHolderUUID == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (playerHasDragonEgg(player)) {
                    eggHolderUUID = player.getUniqueId();
                    return player;
                }
            }
            return null;
        }
        return Bukkit.getPlayer(eggHolderUUID);
    }

    public boolean isTeleportPhaseActive() {
        return teleportPhaseActive;
    }

    public boolean isCompetitionActive() {
        return competitionActive;
    }

    public Location getEggLocation() {
        return eggLocation;
    }

    private void cancelAllTasks() {
        if (teleportPhaseTask != null) {
            teleportPhaseTask.cancel();
            teleportPhaseTask = null;
        }
        if (competitionTask != null) {
            competitionTask.cancel();
            competitionTask = null;
        }
        if (eggCheckTask != null) {
            eggCheckTask.cancel();
            eggCheckTask = null;
        }
    }

    public void shutdown() {
        cancelAllTasks();
        plugin.getDataManager().saveData();
    }

    public void reset() {
        cancelAllTasks();
        teleportPhaseActive = false;
        competitionActive = false;
        holderOffline = false;
        eggHolderUUID = null;
        lastKnownHolderName = null;
        eggLocation = null;
        teleportCooldowns.clear();
        plugin.getDataManager().reset();
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== EndFight Status ===\n");
        sb.append("§eDragon Defeated: §f").append(plugin.getDataManager().isDragonDefeated()).append("\n");
        sb.append("§eCompetition Active: §f").append(competitionActive).append("\n");
        sb.append("§eTeleport Phase Active: §f").append(teleportPhaseActive).append("\n");
        sb.append("§eCompetition Completed: §f").append(plugin.getDataManager().isCompetitionCompleted()).append("\n");

        Player holder = getEggHolder();
        if (holder != null) {
            sb.append("§eEgg Holder: §f").append(holder.getName()).append("\n");
        } else if (holderOffline && lastKnownHolderName != null) {
            sb.append("§eEgg Holder: §f").append(lastKnownHolderName).append(" §c(OFFLINE)").append("\n");
        } else {
            sb.append("§eEgg Holder: §fNone\n");
        }

        String winner = plugin.getDataManager().getWinner();
        sb.append("§eWinner: §f").append(winner != null ? winner : "None");

        return sb.toString();
    }
}
