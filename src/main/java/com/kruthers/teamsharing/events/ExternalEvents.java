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

package com.kruthers.teamsharing.events;

import com.kruthers.teamsharing.TeamSharing;
import com.kruthers.teamsharing.inventory.CustomInventory;
import com.kruthers.teamsharing.inventory.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

public class ExternalEvents implements Listener {

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team == null || !TeamSharing.isSharingActive()) return;
            CustomInventory inv = TeamSharing.getInventory(team.getName());
            if (inv == null) return;

            ItemStack item = event.getItem().getItemStack();

            if (inv.autoAddItem(item,false,new int[]{player.getInventory().getHeldItemSlot(),40})) {
                Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
                TeamSharing.setInventory(team.getName(),inv);

            } else {
                event.setCancelled(false);
                player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
                inv.LoadToPlayer(player);

            }
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack item = event.getItem().getItemStack();

        if (inv.autoAddItem(item,false,new int[]{player.getInventory().getHeldItemSlot(),40})) {
            Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
            TeamSharing.setInventory(team.getName(),inv);

        } else {
            event.setCancelled(false);
            player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
            inv.LoadToPlayer(player);

        }
    }

}
