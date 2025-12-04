package org.rgxprime.endfight.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.rgxprime.endfight.EndFightPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {

    private final EndFightPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    private boolean dragonDefeated;
    private boolean competitionCompleted;
    private boolean competitionActive;
    private String winner;
    private long competitionStartTime;
    private long teleportPhaseEndTime;
    private long competitionEndTime;
    private UUID eggHolderUUID;
    private String eggHolderName;
    private Location eggLocation;

    public DataManager(EndFightPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        dragonDefeated = dataConfig.getBoolean("dragon-defeated", false);
        competitionCompleted = dataConfig.getBoolean("competition-completed", false);
        competitionActive = dataConfig.getBoolean("competition-active", false);
        winner = dataConfig.getString("winner", null);
        competitionStartTime = dataConfig.getLong("competition-start-time", 0);
        teleportPhaseEndTime = dataConfig.getLong("teleport-phase-end-time", 0);
        competitionEndTime = dataConfig.getLong("competition-end-time", 0);
        
        String holderUUIDStr = dataConfig.getString("egg-holder-uuid", null);
        eggHolderUUID = holderUUIDStr != null ? UUID.fromString(holderUUIDStr) : null;
        eggHolderName = dataConfig.getString("egg-holder-name", null);
        
        if (dataConfig.contains("egg-location.world")) {
            String worldName = dataConfig.getString("egg-location.world");
            double x = dataConfig.getDouble("egg-location.x");
            double y = dataConfig.getDouble("egg-location.y");
            double z = dataConfig.getDouble("egg-location.z");
            if (worldName != null && plugin.getServer().getWorld(worldName) != null) {
                eggLocation = new Location(plugin.getServer().getWorld(worldName), x, y, z);
            }
        }
    }

    public void saveData() {
        dataConfig.set("dragon-defeated", dragonDefeated);
        dataConfig.set("competition-completed", competitionCompleted);
        dataConfig.set("competition-active", competitionActive);
        dataConfig.set("winner", winner);
        dataConfig.set("competition-start-time", competitionStartTime);
        dataConfig.set("teleport-phase-end-time", teleportPhaseEndTime);
        dataConfig.set("competition-end-time", competitionEndTime);
        dataConfig.set("egg-holder-uuid", eggHolderUUID != null ? eggHolderUUID.toString() : null);
        dataConfig.set("egg-holder-name", eggHolderName);
        
        if (eggLocation != null) {
            dataConfig.set("egg-location.world", eggLocation.getWorld().getName());
            dataConfig.set("egg-location.x", eggLocation.getX());
            dataConfig.set("egg-location.y", eggLocation.getY());
            dataConfig.set("egg-location.z", eggLocation.getZ());
        } else {
            dataConfig.set("egg-location", null);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    public boolean isDragonDefeated() {
        return dragonDefeated;
    }

    public void setDragonDefeated(boolean dragonDefeated) {
        this.dragonDefeated = dragonDefeated;
        saveData();
    }

    public boolean isCompetitionCompleted() {
        return competitionCompleted;
    }

    public void setCompetitionCompleted(boolean competitionCompleted) {
        this.competitionCompleted = competitionCompleted;
        saveData();
    }

    public boolean isCompetitionActive() {
        return competitionActive;
    }

    public void setCompetitionActive(boolean competitionActive) {
        this.competitionActive = competitionActive;
        saveData();
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
        saveData();
    }

    public long getCompetitionStartTime() {
        return competitionStartTime;
    }

    public void setCompetitionStartTime(long competitionStartTime) {
        this.competitionStartTime = competitionStartTime;
        saveData();
    }

    public long getTeleportPhaseEndTime() {
        return teleportPhaseEndTime;
    }

    public void setTeleportPhaseEndTime(long teleportPhaseEndTime) {
        this.teleportPhaseEndTime = teleportPhaseEndTime;
        saveData();
    }

    public long getCompetitionEndTime() {
        return competitionEndTime;
    }

    public void setCompetitionEndTime(long competitionEndTime) {
        this.competitionEndTime = competitionEndTime;
        saveData();
    }

    public UUID getEggHolderUUID() {
        return eggHolderUUID;
    }

    public void setEggHolderUUID(UUID eggHolderUUID) {
        this.eggHolderUUID = eggHolderUUID;
        saveData();
    }

    public String getEggHolderName() {
        return eggHolderName;
    }

    public void setEggHolderName(String eggHolderName) {
        this.eggHolderName = eggHolderName;
        saveData();
    }

    public Location getEggLocation() {
        return eggLocation;
    }

    public void setEggLocation(Location eggLocation) {
        this.eggLocation = eggLocation;
        saveData();
    }

    public void reset() {
        dragonDefeated = false;
        competitionCompleted = false;
        competitionActive = false;
        winner = null;
        competitionStartTime = 0;
        teleportPhaseEndTime = 0;
        competitionEndTime = 0;
        eggHolderUUID = null;
        eggHolderName = null;
        eggLocation = null;
        saveData();
    }
}
