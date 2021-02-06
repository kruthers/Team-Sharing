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

package com.kruthers.teamsharing;

import com.kruthers.teamsharing.commands.CoreCommand;
import com.kruthers.teamsharing.events.InventoryEvents;
import com.kruthers.teamsharing.inventory.CustomInventory;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class TeamSharing extends JavaPlugin {
    private Logger logger;
    @Getter
    private static boolean sharingActive = false;
    public static Properties properties = new Properties();
    public static HashMap<String, Set<Player>> playerCache = new HashMap<>();

    //Runable - Used to ensure peoples inventories are up to date and refresh the player cache
    private static BukkitRunnable mainThread = new BukkitRunnable() {
        @Override
        public void run() {

            if (!TeamSharing.isSharingActive()) {
                return;
            }

            Scoreboard scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();

            for (Player player : Bukkit.getOnlinePlayers()) {
                Team team = scoreboard.getEntryTeam(player.getName());
                if (team == null) {
                    continue;
                }

                CustomInventory inv = TeamSharing.getInventory(team.getName());
                if (inv == null) {
                    continue;
                }
//                inv.LoadToPlayer(player);

                Set<Player> players = TeamSharing.getTeamPlayers(team.getName());
                players.add(player);
                TeamSharing.setTeamPlayers(team.getName(),players);
            }
        }
    };

    //getters / setters
    @Getter
    @Setter
    private static HashMap<String, CustomInventory> teamInventories = new HashMap<>();

    @Override
    public void onEnable() {
        logger = this.getLogger();
        try {
            properties.load(this.getClassLoader().getResourceAsStream(".properties"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Failed to load plugin properties");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        logger.info("Enabling Team Sharing by kruthers Version "+properties.getProperty("version"));

        logger.info("Registering events...");
        this.getServer().getPluginManager().registerEvents(new InventoryEvents(), this);

        logger.info("Events registered, Registering commands...");
        this.getServer().getPluginCommand("teamsharing").setExecutor(new CoreCommand(this));

        logger.info("Commands registered, loading core runables ");
        mainThread.runTaskTimerAsynchronously(this,100,60);


    }

    @Override
    public void onDisable() {

    }

    public static CustomInventory getInventory(String team) {
        if (teamInventories.containsKey(team)) {
            return teamInventories.get(team);
        } else {
            return null;
        }
    }

    public static void setInventory(String team, CustomInventory inv) {
        teamInventories.put(team, inv);
    }

    public static Set<Player> getTeamPlayers(String team) {
        Set<Player> players = TeamSharing.playerCache.get(team);
        if (players == null) {
            players = new HashSet<>();
        }

        return players;
    }

    public static void setTeamPlayers(String team, Set<Player> players) {
        TeamSharing.playerCache.put(team,players);
    }

    public static void enableSharing() {
        TeamSharing.sharingActive = true;
    }

    public static void disableSharing() {
        TeamSharing.sharingActive = false;
    }


}
