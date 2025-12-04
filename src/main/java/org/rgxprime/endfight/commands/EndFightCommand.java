package org.rgxprime.endfight.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rgxprime.endfight.EndFightPlugin;

import java.util.Arrays;
import java.util.List;

public class EndFightCommand implements CommandExecutor, TabCompleter {

    private final EndFightPlugin plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "status", "reset", "help");

    public EndFightCommand(EndFightPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("endfight.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage("§aEndFight configuration reloaded!");
            }
            case "status" -> sender.sendMessage(plugin.getCompetitionManager().getStatus());
            case "reset" -> {
                plugin.getCompetitionManager().reset();
                sender.sendMessage("§aEndFight data has been reset! The dragon egg competition can happen again.");
            }
            case "help" -> sendHelp(sender);
            default -> sender.sendMessage("§cUnknown subcommand. Use /endfight help for available commands.");
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== EndFight Commands ===");
        sender.sendMessage("§e/endfight reload §7- Reload the configuration");
        sender.sendMessage("§e/endfight status §7- View competition status");
        sender.sendMessage("§e/endfight reset §7- Reset all data (allows new competition)");
        sender.sendMessage("§e/endfight help §7- Show this help message");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("endfight.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(input))
                    .toList();
        }

        return List.of();
    }
}
