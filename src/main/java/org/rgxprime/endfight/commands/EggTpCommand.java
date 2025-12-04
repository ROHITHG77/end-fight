package org.rgxprime.endfight.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rgxprime.endfight.EndFightPlugin;

public class EggTpCommand implements CommandExecutor {

    private final EndFightPlugin plugin;

    public EggTpCommand(EndFightPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (!plugin.getCompetitionManager().isCompetitionActive()) {
            plugin.getMessageManager().send(player, "no-competition");
            return true;
        }

        if (!plugin.getCompetitionManager().isTeleportPhaseActive()) {
            plugin.getMessageManager().send(player, "eggtp-phase-inactive");
            return true;
        }

        Player eggHolder = plugin.getCompetitionManager().getEggHolder();
        if (eggHolder == null) {
            plugin.getMessageManager().send(player, "eggtp-no-holder");
            return true;
        }

        if (eggHolder.getUniqueId().equals(player.getUniqueId())) {
            plugin.getMessageManager().send(player, "eggtp-self");
            return true;
        }

        if (!plugin.getCompetitionManager().canTeleport(player)) {
            int remaining = plugin.getCompetitionManager().getRemainingCooldown(player);
            plugin.getMessageManager().send(player, "eggtp-cooldown", "%time%", String.valueOf(remaining));
            return true;
        }

        player.teleport(eggHolder.getLocation());
        plugin.getCompetitionManager().setTeleportCooldown(player);
        plugin.getMessageManager().send(player, "eggtp-success", "%player%", eggHolder.getName());

        return true;
    }
}
