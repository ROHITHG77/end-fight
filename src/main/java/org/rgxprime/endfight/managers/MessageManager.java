package org.rgxprime.endfight.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.rgxprime.endfight.EndFightPlugin;

public class MessageManager {

    private final EndFightPlugin plugin;
    private final String prefix;

    public MessageManager(EndFightPlugin plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfig().getString("messages.prefix", "&5&l[Pinky Lifesteal] &r");
    }

    public Component formatMessage(String path) {
        String message = plugin.getConfig().getString("messages." + path, "");
        if (message.isEmpty()) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + message);
    }

    public Component formatMessage(String path, String... replacements) {
        String message = plugin.getConfig().getString("messages." + path, "");
        if (message.isEmpty()) {
            return Component.empty();
        }
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + message);
    }

    public void broadcast(String path) {
        Component msg = formatMessage(path);
        if (!msg.equals(Component.empty())) {
            Bukkit.broadcast(msg);
        }
    }

    public void broadcast(String path, String... replacements) {
        Component msg = formatMessage(path, replacements);
        if (!msg.equals(Component.empty())) {
            Bukkit.broadcast(msg);
        }
    }

    public void send(Player player, String path) {
        Component msg = formatMessage(path);
        if (!msg.equals(Component.empty())) {
            player.sendMessage(msg);
        }
    }

    public void send(Player player, String path, String... replacements) {
        Component msg = formatMessage(path, replacements);
        if (!msg.equals(Component.empty())) {
            player.sendMessage(msg);
        }
    }
}
