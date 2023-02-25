package com.devbobcorn.mapprojector.commands;

import com.devbobcorn.mapprojector.PluginMain;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandProjectMap implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.spigot().sendMessage(new TextComponent("Command not run by a player!"));
            return false;
        }

        int mapIdStart, mapCountX = 1, mapCountY = 1;

        if (args.length > 0) {
            mapIdStart = Integer.parseInt(args[0]);

            if (args.length > 2) {
                mapCountX = Integer.parseInt(args[1]);
                mapCountY = Integer.parseInt(args[2]);
            }

        } else {
            sender.spigot().sendMessage(new TextComponent("Map Id not specified"));
            return false;
        }

        var plugin = PluginMain.getInstance();

        if (plugin != null) {
            plugin.updateMap(player, mapIdStart, mapCountX, mapCountY);
            return true;
        } else {
            sender.spigot().sendMessage(new TextComponent("Plugin not available!"));
            return false;
        }
    }
}