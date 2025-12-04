package org.rgxprime.endfight;

import org.bukkit.plugin.java.JavaPlugin;
import org.rgxprime.endfight.commands.EggTpCommand;
import org.rgxprime.endfight.commands.EndFightCommand;
import org.rgxprime.endfight.listeners.DragonDeathListener;
import org.rgxprime.endfight.listeners.EggListener;
import org.rgxprime.endfight.listeners.PlayerConnectionListener;
import org.rgxprime.endfight.managers.CompetitionManager;
import org.rgxprime.endfight.managers.DataManager;
import org.rgxprime.endfight.managers.MessageManager;

public class EndFightPlugin extends JavaPlugin {

    private static EndFightPlugin instance;
    private CompetitionManager competitionManager;
    private MessageManager messageManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.messageManager = new MessageManager(this);
        this.dataManager = new DataManager(this);
        this.competitionManager = new CompetitionManager(this);

        getServer().getPluginManager().registerEvents(new DragonDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EggListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        getCommand("eggtp").setExecutor(new EggTpCommand(this));
        EndFightCommand endFightCommand = new EndFightCommand(this);
        getCommand("endfight").setExecutor(endFightCommand);
        getCommand("endfight").setTabCompleter(endFightCommand);

        getLogger().info("EndFight plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (competitionManager != null) {
            competitionManager.shutdown();
        }
        if (dataManager != null) {
            dataManager.saveData();
        }
    }

    public static EndFightPlugin getInstance() {
        return instance;
    }

    public CompetitionManager getCompetitionManager() {
        return competitionManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void reload() {
        reloadConfig();
        messageManager = new MessageManager(this);
        dataManager.loadData();
    }
}
