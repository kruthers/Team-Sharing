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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.*;

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
        Bukkit.broadcastMessage("Action: "+event.getAction());

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

        //Bukkit.broadcastMessage("\n\nEvent Start\n\n"+inv.toString());
        boolean canceled = false;

        if (addActions.contains(action) && inPlayerInv) {
            ItemStack item = event.getCursor();
            if (action == InventoryAction.PLACE_ONE) {
                item.setAmount(1);
            }

            if (slot > 35 && slot < 40) {
                if (!Utils.checkIfItemValid(slot,item)) {
                    Bukkit.broadcastMessage("Hey this does not work in that slot!");
                    return;
                }
            }

            if (inv.addItem(item,slot) == item.getAmount()) {
                canceled = true;
            }

        } else if (removeActions.contains(action) && inPlayerInv) {
            ItemStack item = event.getCurrentItem();
            ItemStack handItem = event.getCursor();
            int amount = item.getAmount();
            if (action == InventoryAction.PICKUP_ONE || action == InventoryAction.DROP_ONE_SLOT) {
                amount = 1;
            } else if (action == InventoryAction.PICKUP_HALF) {
                amount = item.getAmount() / 2;
            } else if (action == InventoryAction.PICKUP_SOME) {
                amount = item.getMaxStackSize() - handItem.getAmount();
            }

            if (!inv.removeItem(handItem,amount,slot)) {
                canceled = true;
            }

        } else if (action == InventoryAction.SWAP_WITH_CURSOR && inPlayerInv) {
            ItemStack newItem = event.getCursor();
            ItemStack localItem = event.getCurrentItem();
            ItemStack invItem = inv.getItem(slot);

            if (Utils.compareItems(invItem,localItem,true)) {
                inv.setItem(newItem,slot);
            } else {
                canceled = true;
            }

        } else if (action == InventoryAction.HOTBAR_SWAP) {
            int hotBarSlot = event.getHotbarButton();
            if (hotBarSlot == -1){
                hotBarSlot = 40;
            }

            ItemStack localHotBar = player.getInventory().getItem(hotBarSlot);
            ItemStack invHotBar = inv.getItem(hotBarSlot);

            if (Utils.compareItems(localHotBar,invHotBar,true)) {
                if (inPlayerInv) {
                    ItemStack localSelected = player.getInventory().getItem(slot);
                    ItemStack invSelected = inv.getItem(slot);

                    if (Utils.compareItems(localSelected,invSelected,true)) {
                        inv.setItem(invHotBar,slot);
                        inv.setItem(invSelected,hotBarSlot);
                    } else {
                        canceled = true;
                    }
                } else {
                    ItemStack item = event.getInventory().getItem(slot);
                    inv.setItem(item,hotBarSlot);
                }

            } else {
                canceled = true;
            }

        } else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            Bukkit.broadcastMessage(""+event.getResult());
//            if (inPlayerInv) {
//                Bukkit.broadcastMessage("Removing item from inv");
//                ItemStack localItem = player.getInventory().getItem(slot);
//                ItemStack item = inv.getItem(slot);
//                if (Utils.compareItems(localItem,item,true)) {
//                    event.get
//                }
//
//            } else {
//                Bukkit.broadcastMessage("Added item to inv");
//
//            }

        } else if (action == InventoryAction.COLLECT_TO_CURSOR) {

        } else if (action == InventoryAction.HOTBAR_MOVE_AND_READD) {

        } else if (!addActions.contains(action) && !removeActions.contains(action)) {
            player.sendMessage("Ignored action");
        }

        if (canceled) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
            inv.LoadToPlayer(player);

        } else {
            //Bukkit.broadcastMessage("\nSaving\n\n"+inv.toString()+ChatColor.RED+"\n Event End");
            Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
            TeamSharing.setInventory(team.getName(),inv);
        }


    }


    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        // Default checks to check that the system is active and that they are ona  team
        Player player = (Player) event.getWhoClicked();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        boolean canceled = false;

        Set<Integer> rawSlots = event.getRawSlots();
        Iterator<Integer> iterator = rawSlots.iterator();
        Iterator<Integer> slotIterator = event.getInventorySlots().iterator();
        Map<Integer,ItemStack> items = event.getNewItems();
        player.sendMessage("Dragged items in inv");

        int containerSlots = event.getInventory().getSize();
        int updatedSlots = 0;
        while (iterator.hasNext()) {
            int rawSlot = iterator.next();
            int slot = slotIterator.next();

            if (rawSlot > containerSlots){
                updatedSlots++;
                ItemStack item = items.get(rawSlot);

                if (inv.getItem(slot) == null){
                    inv.setItem(item,slot);
                } else if (inv.getItem(slot).getType() == Material.AIR){
                    inv.setItem(item,slot);
                } else if (Utils.compareItems(item,inv.getItem(slot),false)) {
                    inv.addItem(item,slot);
                } else {
                    canceled = true;
                    break;
                }
            }
        }

        if (canceled) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
            inv.LoadToPlayer(player);

        } else if (updatedSlots > 0) {
            Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
            TeamSharing.setInventory(team.getName(),inv);
        }

    }


    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        // Default checks to check that the system is active and that they are ona  team
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;


        Bukkit.broadcastMessage("Dropping item");
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        if (!droppedFromInvs.contains(player)) {
            int slot = player.getInventory().getHeldItemSlot();
            ItemStack handItem = inv.getItem(slot);

            if (handItem == null) return;
            if (handItem.getType() == droppedItem.getType()){
                int count = droppedItem.getAmount();
                ItemStack item = inv.getItem(slot);
                count = item.getAmount() - count;
                item.setAmount(count);
                inv.setItem(item,slot);

                Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
                TeamSharing.setInventory(team.getName(),inv);

            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
                inv.LoadToPlayer(player);

            }
        } else {
            droppedFromInvs.remove(player);
        }

    }


    //Track off hand swap events
    @EventHandler
    public void onOffHandSwap(PlayerSwapHandItemsEvent event){
        // Default checks to check that the system is active and that they are ona  team
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        int slot = player.getInventory().getHeldItemSlot();

        ItemStack invOffhand = inv.getItem(40);
        ItemStack invSlotItem = inv.getItem(slot);
        ItemStack offhand = player.getInventory().getItemInOffHand();
        ItemStack slotItem = player.getInventory().getItem(slot);

        if (Utils.compareItems(invOffhand,offhand,true) && Utils.compareItems(invSlotItem,slotItem,true)) {
            inv.setItem(invOffhand,slot);
            inv.setItem(invSlotItem,40);

            Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
            TeamSharing.setInventory(team.getName(),inv);

        } else {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
            inv.LoadToPlayer(player);
        }

    }



    /*
    if (Utils.checkIfItemValid(36,item)) {
        if (inv.getItem(36).getType() == Material.AIR) {
            inv.setItem(item,36);
            inv.setItem(null,slot);
        } else {
            canceled = true;
        }
    } else if (Utils.checkIfItemValid(37,item)) {
        if (inv.getItem(37).getType() == Material.AIR) {
            inv.setItem(item,37);
            inv.setItem(null,slot);
        } else {
            canceled = true;
        }
    } else if (Utils.checkIfItemValid(38,item)) {
        if (inv.getItem(38).getType() == Material.AIR) {
            inv.setItem(item,38);
            inv.setItem(null,slot);
        } else {
            canceled = true;
        }
    } else if (Utils.checkIfItemValid(39,item)) {
        if (inv.getItem(39).getType() == Material.AIR) {
            inv.setItem(item,39);
            inv.setItem(null,slot);
        } else {
            canceled = true;
        }
    }
     */
}
