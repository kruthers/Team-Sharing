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

package com.kruthers.old.events;

import com.kruthers.old.objects.TeamInventory;
import com.kruthers.old.TeamSharing;
import com.kruthers.old.utils.TeamManagement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

public class ExternalEvents implements Listener {

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof Player){
            Player player = (Player) entity;
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team == null || !TeamSharing.sharingActive) return;
            TeamInventory inv = TeamSharing.getInventory(team.getName());
            if (inv == null) return;

            ItemStack item = event.getItem().getItemStack();
            inv.autoAddItem(item,40,false,false);

            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack item = event.getItem().getItemStack();
        inv.autoAddItem(item,40,false,false);

        TeamSharing.setTeamInventory(team.getName(),inv);
        TeamManagement.syncTeam(team,player);
    }

    @EventHandler
    public void onAnimalFeed(PlayerInteractAtEntityEvent event) {
        Bukkit.broadcastMessage("Clicked on animal");

        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        Entity entity = event.getRightClicked();
        if (entity instanceof Breedable) {
            Breedable animal = (Breedable) entity;

            if (animal.canBreed()) {

            }

        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }


    }

}
