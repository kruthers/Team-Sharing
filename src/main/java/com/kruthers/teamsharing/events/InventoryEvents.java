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

package com.kruthers.teamsharing.events;

import com.kruthers.teamsharing.objects.TeamInventory;
import com.kruthers.teamsharing.TeamSharing;
import com.kruthers.teamsharing.utils.ItemUtils;
import com.kruthers.teamsharing.utils.TeamManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
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

        containerInvs.add(InventoryType.CREATIVE);
        containerInvs.add(InventoryType.BARREL);
        containerInvs.add(InventoryType.CHEST);
        containerInvs.add(InventoryType.DISPENSER);
        containerInvs.add(InventoryType.DROPPER);
        containerInvs.add(InventoryType.WORKBENCH);
        containerInvs.add(InventoryType.SHULKER_BOX);
        containerInvs.add(InventoryType.ENDER_CHEST);
        containerInvs.add(InventoryType.ENDER_CHEST);

    }

    @EventHandler
    public void onInteraction(InventoryClickEvent event){
        // TESTING
        if (event.isCancelled()) {
            Bukkit.broadcastMessage("Event is canceled");
        }

        // Default checks to check that the system is active and that they are ona  team
        Player player = (Player) event.getWhoClicked();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.sharingActive) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
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
        /* TODO
         - Shift clicking (MOVE_TO_OTHER_INVENTORY)
         - COLLECT_TO_CURSOR

         BUGS
         - Dragging sets it max stack size
         - Switch from off hand to main hand is broken
         - Picking up items
        */

        if (action == InventoryAction.HOTBAR_SWAP){
            //Called when a player uses 0-8 or f to move items around in their inv
            int oldSlot = event.getHotbarButton();
            if (oldSlot == -1){
                oldSlot = 40;
            }

            ItemStack item = event.getCurrentItem();
            if (rawSlot > containerSlots){
                //in the inv
                ItemStack swapItem = inv.getItem(oldSlot);

                inv.setItem(oldSlot,item);
                inv.setItem(slot,swapItem);

            } else {
                // sawp form the container:
                inv.setItem(oldSlot,item);
            }

            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);

            return;
        } else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            Bukkit.broadcastMessage("Attempting to move item");
            // Called when a player shift clicks a item into the first available slot
            if (rawSlot > containerSlots){
                // inside main inv
                Bukkit.broadcastMessage("From Main inv clicking");
                InventoryType type = event.getClickedInventory().getType();
                if (type == InventoryType.PLAYER) {
                    ItemStack item = event.getCurrentItem();
                    Bukkit.broadcastMessage("In main inv");

                    if (ItemUtils.checkArmourSlot(39,item)) {
                        Bukkit.broadcastMessage("Is helmet?");
                        if (inv.getItem(39) == null) {
                            inv.setItem(39,item);
                            Bukkit.broadcastMessage("Added helmet");
                        } else if (inv.getItem(39).getType() == Material.AIR){
                            inv.setItem(39,item);
                        }
                    } else if (ItemUtils.checkArmourSlot(38,item)) {
                        if (inv.getItem(38) == null) {
                            inv.setItem(38,item);
                        } else if (inv.getItem(38).getType() == Material.AIR){
                            inv.setItem(38,item);
                        }

                    } else if (ItemUtils.checkArmourSlot(37,item)) {
                        if (inv.getItem(37) == null) {
                            inv.setItem(37,item);
                        } else if (inv.getItem(37).getType() == Material.AIR){
                            inv.setItem(37,item);
                        }

                    } else if (ItemUtils.checkArmourSlot(36,item)) {
                        if (inv.getItem(36) == null) {
                            inv.setItem(36,item);
                        } else if (inv.getItem(36).getType() == Material.AIR){
                            inv.setItem(36,item);
                        }

                    } else {
                        player.sendMessage(ChatColor.RED+"This event is currently blocked and is coming soon");
                        player.playSound(player.getLocation(),Sound.ITEM_TRIDENT_RETURN,(float)100000,(float)1.2);
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    player.sendMessage(ChatColor.RED+"This event is currently being added and is coming soon");
                    player.playSound(player.getLocation(),Sound.ITEM_TRIDENT_RETURN,(float)100000,(float)1.2);
                    event.setCancelled(true);
                    return;
                }

            } else {
                // entering the inv from outside
                ItemStack item = event.getCurrentItem();
                Bukkit.broadcastMessage("Added item "+item.getType());
                inv.autoAddItem(item,0,true,true);

                Bukkit.broadcastMessage("added item??? "+inv.getItem(2));

            }
            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);

            return;
        } else if (action == InventoryAction.COLLECT_TO_CURSOR) {
            player.sendMessage("Attempting to collect items!");

            ItemStack item = event.getCursor();
            int count = item.getAmount();

            while (count < item.getMaxStackSize()) {
                int slotFound = inv.findFirstSlot(item,false);
                if (slotFound == -1 ) {
                    break;
                } else {
                    ItemStack itemFound = inv.getItem(slotFound);
                    count += itemFound.getAmount();

                    if (count < item.getMaxStackSize()) {
                        inv.setItem(slotFound,null);
                        break;
                    } else {
                        int remaining = count - item.getMaxStackSize();
                        itemFound.setAmount(remaining);
                        inv.setItem(slotFound,itemFound);
                    }
                }
            }

            return;
        }

        if (rawSlot < containerSlots) {
            //Bukkit.broadcastMessage("Outside inventory!");
            return;
        }

        // -------- All below this line only happen if the action is in the main inv -------- \\

        //Check which kind of action it was
        if (addActions.contains(action)){
            player.sendMessage("Attempting to add item to inv");
            //Actions where they add an item to their inventory
            ItemStack handItem = event.getCursor();

            if (slot > 35 && slot < 40) {
                if (!ItemUtils.checkArmourSlot(slot,handItem)) {
                    return;
                }
            }

            if (action==InventoryAction.PLACE_ONE) {
                handItem.setAmount(1);
            }

            inv.addItem(handItem,slot);

            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);

        } else if (removeActions.contains(action)) {
            player.sendMessage("Attempting to remove item from inv");
            //Actions where they remove an item from their inventory
            ItemStack localItem = event.getCurrentItem();
            int count = localItem.getAmount();

            Bukkit.broadcastMessage("Performing remove event for "+action);

            if (action==InventoryAction.DROP_ONE_SLOT || action == InventoryAction.PICKUP_ONE) {
                count = 1;
            } else if (action == InventoryAction.PICKUP_HALF) {
                count = (int) Math.ceil(count/2.0);
            }

            Bukkit.broadcastMessage("Removing "+count+" from slot "+slot);
            
            inv.removeItem(slot,count,localItem);

            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);

        } else if (action == InventoryAction.SWAP_WITH_CURSOR) {
            player.sendMessage("Attempting to swap item in inv");
            //Actions where they swap an item from their cursor with the set slot
//            player.sendMessage("Swapped with cursor");

            ItemStack localItem = event.getCurrentItem();
            ItemStack item = event.getCursor();

            if (slot > 35 && slot < 40) {
                if (!ItemUtils.checkArmourSlot(slot,item)) {
                    return;
                }
            }

            inv.swapItem(slot,item,localItem);

            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);

        }  else {
            //the action is ignored, print for debugging
            player.sendMessage("Ignored/ skipped action "+action);
        }

    }



    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.sharingActive) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        boolean cancel = false;

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
                    inv.setItem(slot,item);
                } else if (ItemUtils.compareItems(item,inv.getItem(slot))) {
                    inv.setItem(slot,item);
                } else {
                    cancel = true;
                    break;
                }
            }
        }

        if (cancel){
            Bukkit.broadcastMessage(ChatColor.RED+"Inventory desynced for "+player.getName());
            event.setCancelled(true);
            inv.loadToPlayer(player);
        } else {
            if (updatedSlots > 0){
                Bukkit.broadcastMessage("Dragged "+updatedSlots+" in main inv");

                TeamSharing.setTeamInventory(team.getName(),inv);
                TeamManagement.syncTeam(team,player);
            }
        }

    }




    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
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
                inv.setItem(slot,item);

                TeamSharing.setTeamInventory(team.getName(),inv);
                TeamManagement.syncTeam(team,player);

            } else {
                player.sendMessage(ChatColor.RED+"Your Inv looks to have de-synced, re-syncing");
                event.setCancelled(true);
                inv.loadToPlayer(player);

            }
        } else {
            droppedFromInvs.remove(player);
        }

    }




    //Track off hand swap events
    @EventHandler
    public void onOffHandSwap(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        int slot = player.getInventory().getHeldItemSlot();

        ItemStack offItem = inv.getItem(40);
        ItemStack handItem = inv.getItem(slot);

        inv.setItem(40,handItem);
        inv.setItem(slot,offItem);

        TeamSharing.setTeamInventory(team.getName(),inv);
        TeamManagement.syncTeam(team,player);

    }


}
