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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class InventoryEvents implements Listener {

    private List<InventoryAction> addActions = new ArrayList<>(); // all actions that are from adding an item to your inv
    private List<InventoryAction> removeActions = new ArrayList<>(); // all actions where you are directly removing an item
    private List<InventoryAction> dropActions = new ArrayList<>(); // all actions that involve dropping an item from within your inv

    private List<Player> droppedFromInvs = new ArrayList<>();

    private List<InventoryType> containerInvs = new ArrayList<>(); // containers that are not restricted for item type

    public InventoryEvents(){
        addActions.add(InventoryAction.PLACE_ALL);
        addActions.add(InventoryAction.PLACE_ONE);
        addActions.add(InventoryAction.PLACE_SOME);

        removeActions.add(InventoryAction.PICKUP_ALL);
        removeActions.add(InventoryAction.PICKUP_HALF);
        removeActions.add(InventoryAction.PICKUP_ONE);
        removeActions.add(InventoryAction.PICKUP_SOME);
        removeActions.add(InventoryAction.DROP_ALL_SLOT);
        removeActions.add(InventoryAction.DROP_ONE_SLOT);



        dropActions.add(InventoryAction.DROP_ONE_SLOT);
        dropActions.add(InventoryAction.DROP_ALL_SLOT);
        dropActions.add(InventoryAction.DROP_ALL_CURSOR);
        dropActions.add(InventoryAction.DROP_ONE_CURSOR);

    }

    //General Interations
    @EventHandler
    public void onInteraction(InventoryClickEvent event) {
        // TESTING
        if (event.isCancelled()) {
            Bukkit.broadcastMessage("Event is canceled");
        }

        // Default checks to check that the system is active and that they are ona  team
        Player player = (Player) event.getWhoClicked();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        //Get the inventory action for checking it & store all varibles
        InventoryAction action = event.getAction();
        int rawSlot = event.getRawSlot();
        int slot = event.getSlot();
        int containerSlots = event.getInventory().getSize();

        //When a player drops an item from within their inv, log it for the drop event
        if (dropActions.contains(action)){
            droppedFromInvs.add(player);
        }

        boolean inPlayerInv = true;
        if (rawSlot < containerSlots) {
            inPlayerInv = false;
        }

        /*
        Actions:
        InventoryAction.COLLECT_TO_CURSOR;
        InventoryAction.HOTBAR_MOVE_AND_READD;
        InventoryAction.HOTBAR_SWAP;
        InventoryAction.MOVE_TO_OTHER_INVENTORY;
        InventoryAction.SWAP_WITH_CURSOR;
         */
        boolean canceled = false;

        if (addActions.contains(action) && inPlayerInv) {
            ItemStack item = event.getCursor();
            if (action == InventoryAction.PLACE_ONE) {
                item.setAmount(1);
            }

            if (inv.addItem(item,slot) == item.getAmount()) {
                canceled = true;
            }

        } if (removeActions.contains(action) && inPlayerInv) {
            ItemStack item = event.getCurrentItem();
            ItemStack handItem = event.getCursor();
            int amount = item.getAmount();
            if (action == InventoryAction.PICKUP_ONE) {
                amount = 1;
            } else if (action == InventoryAction.PICKUP_HALF) {
                amount = item.getAmount() / 2;
            } else if (action == InventoryAction.PICKUP_SOME) {
                amount = item.getMaxStackSize() - handItem.getAmount();
            }

            if (!inv.removeItem(handItem,amount,slot)) {
                canceled = true;
            }

        }



        if (canceled) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
            inv.LoadToPlayer(player);

        } else {
            Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
            TeamSharing.setInventory(team.getName(),inv);
        }


    }

}
