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

import com.kruthers.old.events.BlockEvents;
import com.kruthers.old.events.ExternalEvents;
import com.kruthers.old.events.InventoryEvents;
import com.kruthers.old.events.ToolEvents;
import com.kruthers.old.objects.TeamInventory;
import com.kruthers.old.utils.OtherUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public final class TeamSharing extends JavaPlugin {
    private static HashMap<String, TeamInventory> inventories = new HashMap<>();
    private static List<String> blockedTeams;
    private Logger LOG;

    public static boolean sharingActive = false;

    @Override
    public void onEnable() {
        LOG = this.getLogger();

        LOG.info("Loading Team Sharing, loading config...");
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        OtherUtils.loadConfig();
        if (!FileManager.init()){
            LOG.severe("Failed to create storage file, disabling");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        LOG.info("Loaded config, loading commands...");
        this.getServer().getPluginCommand("teamsharing").setExecutor(new CoreCommand(this));

        LOG.info("Loaded commands, loading events...");
        this.getServer().getPluginManager().registerEvents(new BlockEvents(), this);
        this.getServer().getPluginManager().registerEvents(new ExternalEvents(), this);
        this.getServer().getPluginManager().registerEvents(new InventoryEvents(), this);
        this.getServer().getPluginManager().registerEvents(new ToolEvents(), this);

        LOG.info("Loaded Events, starting main runnable");
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (!sharingActive){
//                    return;
//                }
//                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
//                for (Player player : Bukkit.getOnlinePlayers()){
//                    Team team = scoreboard.getEntryTeam(player.getName());
//                    if (team == null) return;
//                    TeamInventory inv = TeamSharing.getInventory(team.getName());
//                    if (inv == null) return;
//
//                    inv.loadToPlayer(player);
//                }
//            }
//        }.runTaskTimerAsynchronously(this,100,2);

        LOG.info("Started runnable, all loaded");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD+"Team Sharing V1 by kruthers' is installed and enabled");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void setBlockedTeams(List<String> blockedTeams) {
        TeamSharing.blockedTeams = blockedTeams;
    }

    public static void setTeamInventory(String team, TeamInventory inventory){
        inventories.put(team,inventory);
        Bukkit.broadcastMessage(ChatColor.GOLD+"Updated shared inv for team "+team);

        Bukkit.broadcastMessage("\n\n"+team+"'s Inv is now: "+inventory.toString());
    }

    public static List<String> getBlockedTeams() {
        return blockedTeams;
    }

    public static TeamInventory getInventory(String team){
        if (team == null) return null;
        return inventories.get(team);
    }
}
