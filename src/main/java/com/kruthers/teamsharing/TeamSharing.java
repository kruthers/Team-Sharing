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
import com.kruthers.teamsharing.events.*;
import com.kruthers.teamsharing.inventory.CustomInventory;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    @Getter
    @Setter
    private static HashMap<String, CustomInventory> teamInventories = new HashMap<>();

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
        /* Events TODO
        - Inventory:
          - shift click
          - collect to cursor
        - Throw an item (egg, snowball)
        - Equip Armour
        - middle click
        - Feed an animal
        - Right click Respawn Anchor
        - fill a bucket
        - empty a bucket
         */
        /* Events DONE
        - Inventory:
          - Remove
          - swap
          - number swap
          - Drop (q on a stack)
          - Place
        - Drop (q on hotbar)
        - Place a block
        - Arrow pickup
        - Tool Damage
        - Pick up
        - Shoot a arrow
        - Consume an item (food/ potion
        - Break Item
         */
        /* Not working as intended
        - Inventory:
          - Drag - not working when item in slot
         */
        this.getServer().getPluginManager().registerEvents(new InventoryEvents(), this);
        this.getServer().getPluginManager().registerEvents(new BlockEvents(), this);
//        this.getServer().getPluginManager().registerEvents(new EntityEvents(), this);
        this.getServer().getPluginManager().registerEvents(new EquipmentEvents(), this);
        this.getServer().getPluginManager().registerEvents(new ExternalEvents(), this);

        logger.info("Events registered, Registering commands...");
        this.getServer().getPluginCommand("teamsharing").setExecutor(new CoreCommand(this));

        logger.info("Commands registered, loading core runables ");
        //mainThread.runTaskTimerAsynchronously(this,100,60);


    }

    @Override
    public void onDisable() {

    }

    public static void setInventory(String team, CustomInventory inv) {
        Bukkit.broadcastMessage(ChatColor.GOLD+"\nSaving to "+team+":\n"+ChatColor.WHITE+inv.toString());
        CustomInventory newInv = inv.clone();
        teamInventories.put(team, newInv);
//        Bukkit.broadcastMessage(ChatColor.GOLD+"\nSaved for "+team+":\n"+ChatColor.WHITE+teamInventories.get(team).toString());
    }

    public static CustomInventory getInventory(String team) {
        CustomInventory inv = teamInventories.get(team);
        if (inv != null) {
            Bukkit.broadcastMessage(ChatColor.GOLD+"\nReceived for "+team+":\n"+ChatColor.WHITE+teamInventories.get(team).toString());
        }
        return inv;
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
