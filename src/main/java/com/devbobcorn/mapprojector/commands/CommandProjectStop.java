package com.devbobcorn.mapprojector.commands;

import com.devbobcorn.mapprojector.PluginMain;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandProjectStop implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        var plugin = PluginMain.getInstance();

        if (plugin != null) {
            plugin.stopUpdatingMaps();
            return true;
        } else {
            sender.spigot().sendMessage(new TextComponent("Plugin not available!"));
            return false;
        }


    }
}
