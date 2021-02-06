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

package com.kruthers.old.utils;

import com.kruthers.old.objects.TeamInventory;
import com.kruthers.old.TeamSharing;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamManagement {

    public static void syncTeam(Team team, Player exclude) {
        TeamInventory inventory = TeamSharing.getInventory(team.getName());
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (inventory==null){
            return;
        }
        for (Player player: Bukkit.getOnlinePlayers()){
            Team pTeam = scoreboard.getEntryTeam(player.getName());
            if (pTeam == null) continue;
            if (pTeam.getName().equals(team.getName())) {
                if (player == exclude) continue;
//                player.sendMessage("Syncing inventory");
                inventory.loadToPlayer(player);

                player.updateInventory();
            }
        }

    }

//    public static void syncTeam(Team team) {
//        TeamInventory inventory = TeamSharing.getInventory(team.getName());
//        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
//        if (inventory==null){
//            return;
//        }
//        for (Player player: Bukkit.getOnlinePlayers()){
//            Team pTeam = scoreboard.getEntryTeam(player.getName());
//            if (pTeam == null) continue;
//            if (pTeam.getName().equals(team.getName())) {
////                player.sendMessage("Syncing inventory");
//                inventory.loadToPlayer(player);
//
//                player.updateInventory();
//            }
//        }
//
//    }
}
