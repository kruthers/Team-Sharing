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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

public class BlockEvents implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Default checks to check that the system is active and that they are ona  team
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        boolean canceled = false;

        int slot = player.getInventory().getHeldItemSlot();
        ItemStack placedItem = event.getItemInHand();
        ItemStack item = inv.getItem(slot);

        if (Utils.compareItems(item,placedItem,true)) {
            inv.removeItem(placedItem,1,slot);
            Bukkit.broadcastMessage("Test?");
        } else {
            canceled = true;
        }


        if (!canceled) {
            Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
            TeamSharing.setInventory(team.getName(),inv);

        } else {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
            inv.LoadToPlayer(player);
        }

    }
}
