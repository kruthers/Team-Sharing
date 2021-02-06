/*
 * Team Sharing - A Spigot plugin thats allows plays on the same team to share there inventory
 * Copyright (C) 2020 kruthers
 *
 * This Program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The program  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kruthers.old;

import com.kruthers.old.objects.TeamInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.logging.Logger;

public class CoreCommand implements CommandExecutor {
    private static TeamSharing plugin;
    private static Logger LOG;

    public CoreCommand(TeamSharing pl){
        plugin=pl;
        LOG = plugin.getLogger();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0){
            sender.sendMessage(ChatColor.GREEN+"This server is running Team Sharing version 1.0");
        } else if (args.length == 1){
            String subCommand = args[0];
            switch (subCommand){
                case "start":
                    Scoreboard scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
                    for (Player player : Bukkit.getOnlinePlayers()){
                        Team team = scoreboard.getEntryTeam(player.getName());
                        if (team==null) continue;
                        if (!TeamSharing.getBlockedTeams().contains(team.getName())){
                            TeamInventory inventory = TeamSharing.getInventory(team.getName());
                            if (inventory == null) {
                                inventory = new TeamInventory();
                                TeamSharing.setTeamInventory(team.getName(),inventory);
                                LOG.info(team.getName()+" Added a setup team");
                            }
                            inventory.loadToPlayer(player);
                        } else {
                            LOG.info(player.getName()+" is on blocked team or no team, skipping");
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN+"Inventory sharing is setup!");
                    TeamSharing.sharingActive = true;
                break;
                case "stop":
                    sender.sendMessage("Stopping inventory sharing");
                break;
                case "wipe":
                    sender.sendMessage("Wiping shared inventories");
                break;
                case "reload":
                    sender.sendMessage("Reloading plugin and config");
                break;
                default:
                    sender.sendMessage(ChatColor.RED+"Invalid Argument given /teamsharing [<start>|<stop>|<wipe>|<reload>]");
                break;
            }
        } else {
            sender.sendMessage(ChatColor.RED+"Invalid Argument given /teamsharing [<start>|<stop>|<wipe>|<reload>]");
        }

        return true;
    }
}
