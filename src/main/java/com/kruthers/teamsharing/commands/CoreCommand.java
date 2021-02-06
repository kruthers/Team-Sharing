/*
 * Team Sharing - A spigot plugin that allows for people on the same team to have a shared inventory.
 * Copyright (c) 2021 kruthers
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kruthers.teamsharing.commands;

import com.kruthers.old.objects.TeamInventory;
import com.kruthers.teamsharing.TeamSharing;
import com.kruthers.teamsharing.inventory.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CoreCommand implements CommandExecutor {
    private TeamSharing plugin;
    public CoreCommand(TeamSharing pl) {
        this.plugin = pl;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!label.equalsIgnoreCase("teamsharing")) {
            return false;
        }
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        }


        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN+"This server is running "+ TeamSharing.properties.getProperty("full_name"));
        } else if (args.length == 1) {
            String arg = args[0];
            if (arg.equalsIgnoreCase("version")) {
                sender.sendMessage(ChatColor.GREEN+"Team Sharing is running version "+ TeamSharing.properties.getProperty("version"));
            } else if (checkWithPerms(sender,"reload",arg,false)) {
                sender.sendMessage("[TeamSharing] Reloading config...");
                plugin.reloadConfig();
                sender.sendMessage("[TeamSharing] Config reloaded.");

            } else if (checkWithPerms(sender,"start","manage",arg,false)) {
                sender.sendMessage("Starting team sharing...");
                this.startCommand(true);

            } else if (checkWithPerms(sender,"stop","manage",arg,false)) {
                sender.sendMessage(ChatColor.RED+"Coming soon...");


            } else {
                sender.sendMessage(ChatColor.RED+"Invalid argument given, correct usage: "+command.getUsage());
            }
        }

        return true;
    }

    private boolean checkWithPerms(CommandSender sender, String command, String arg, boolean playerOnly) {
        return checkWithPerms(sender,command,command,arg,playerOnly);
    }

    private boolean checkWithPerms(CommandSender sender, String command, String perm, String arg, boolean playerOnly) {
        String permission = "teamsharing."+perm;
        if (arg.equalsIgnoreCase(command)) {
            if (sender.hasPermission(permission)) {
                if (playerOnly) {
                    if (sender instanceof Player) {
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED+"Sorry, this command can only be run by players");
                        return false;
                    }
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED+"Sorry, you don't have permission to run that command");
                return false;
            }
        } else {
            return false;
        }
    }

    private void startCommand(boolean clear) {
        Scoreboard mainBoard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team team = mainBoard.getEntryTeam(player.getName());
            if (team == null) {
                continue;
            }

            CustomInventory inv = TeamSharing.getInventory(team.getName());
            if (inv == null) {
                if (clear) {
                    player.getInventory().clear();
                }
                inv = new CustomInventory(player.getInventory());

                TeamSharing.setInventory(team.getName(),inv);
            } else {
                inv.LoadToPlayer(player);
            }

        }

        TeamSharing.enableSharing();
        Bukkit.broadcastMessage(ChatColor.GOLD+"Team Sharing is now in effect!");
    }

}
