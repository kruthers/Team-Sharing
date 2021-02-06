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

package com.kruthers.old.objects;

import com.kruthers.old.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TeamInventory {
    private ItemStack[] inventoryContents;
    private ItemStack[] armourContents;
    private ItemStack offHand;

    public TeamInventory(){
        this.inventoryContents = new ItemStack[36];
        this.armourContents = new ItemStack[4];
        this.offHand = null;

    }

    public void loadToPlayer(Player player){
        PlayerInventory inv = player.getInventory();

        inv.setContents(this.inventoryContents);
        inv.setArmorContents(this.armourContents);
        inv.setItemInOffHand(this.offHand);
    }

    private void setInventoryContents(ItemStack[] inventoryContents) {
        this.inventoryContents = inventoryContents;
    }

    private void setArmourContents(ItemStack[] armourContents) {
        this.armourContents = armourContents;
    }

    private void setOffHand(ItemStack item) {
        this.offHand = item;
    }

    public ItemStack[] getArmourContents() {
        return this.armourContents;
    }

    private ItemStack[] getInventoryContents() {
        return this.inventoryContents;
    }

    private ItemStack getOffHand(){
        return this.offHand;
    }


    // Inventory Management
    // Basic events

    /**
     * Used to set an item in a given slot to a given item regardless of whats there already.
     * @param slot the slot to set item in
     * @param item the item to set
     * @throws IllegalArgumentException
     */
    public void setItem(int slot, ItemStack item) throws IllegalArgumentException{
        if (item != null){
            if (item.getType() == Material.AIR){
//                Bukkit.broadcastMessage("made it null");
                item = null;
            }
        }

        if (slot <= 35) {
            this.inventoryContents[slot] = item;
        } else if (slot <= 39) {
            int armourSlot = slot-36;
            this.armourContents[armourSlot] = item;
        } else if (slot == 40) {
            this.offHand = item;
        } else {
            throw new IllegalArgumentException("Invalid slot given "+slot);
        }
    }


    /**
     * Get an item from a given slot in the inventory
     * @param slot insternal id of the slot to get
     * @return ItemStack or null if there is no item
     * @throws IllegalArgumentException when a number outside the inventory is given
     */
    public ItemStack getItem(int slot) throws IllegalArgumentException {
        ItemStack returnItem;

        if (slot <= 35) {
            returnItem = this.getInventoryContents()[slot];
        } else if (slot <= 39) {
            int armourSlot = slot-36;
            returnItem = this.getArmourContents()[armourSlot];
        } else if (slot == 40) {
            returnItem = this.getOffHand();
        } else {
            throw new IllegalArgumentException("Invalid slot given "+slot);
        }

        if (returnItem == null) {
            return null;
        } else if (returnItem.getType() == Material.AIR) {
            return  null;
        } else {
            return  returnItem;
        }
    }

    // advanced

    /**
     * used to combine items in a slot, used when the max stack size may be exceeded
     * @param slot the slot to combine in
     * @param count how many to increase the current stack by
     * @return the reaming count if not all could be added. 0 if they could.
     */
    private int addSome(int slot, int count) {
        int leftOver = 0;
        ItemStack item = this.getItem(slot);
        int amount = item.getAmount();
        amount += count;
        if ( amount > item.getMaxStackSize() ){
            leftOver = amount-item.getMaxStackSize();
            amount = item.getMaxStackSize();
        }
        Bukkit.broadcastMessage("Increasing Slot "+slot+", current count: "+item.getAmount()+". Increasing by "+count+" which leaves "+leftOver+" remaining");

        item.setAmount(amount);
        this.setItem(slot,item);

        return leftOver;
    }

    /**
     * Finds the first instance of the item in the players inventory, -1 if it could not be found
     * @param item the item to find
     * @param offHandFirst if to start with the offhand
     * @return the slot where ther item can be found, if it fails -1
     */
    public int findFirstSlot(ItemStack item, boolean offHandFirst) {
        if (offHandFirst && offHand != null) {
            if (offHand.getType() == item.getType()) {
                return 40;
            }
        }

        for (int i = 0; i < inventoryContents.length; i++) {
            ItemStack invItem = inventoryContents[i];
            if (invItem == null) continue;
            if (invItem.getType() == item.getType()) {
                return i;
            }
        }

        if (!offHandFirst && offHand != null) {
            if (offHand.getType() == item.getType()) {
                return 40;
            }
        }

        return -1;
    }


    /**
     * Adds items to a given slot, but includes checks to make sure it can
     * @param item the item to add
     * @param slot the slot to add it too
     * @return
     */
    public boolean addItem(ItemStack item, int slot){
        ItemStack oldItem = this.getItem(slot);

        if (oldItem == null){
            this.setItem(slot,item);

        } else if (ItemUtils.compareItems(oldItem,item)) {
            int maxStackSize = oldItem.getMaxStackSize();
            int count = oldItem.getAmount() + item.getAmount();

            if (count >= maxStackSize){
                oldItem.setAmount(maxStackSize);
            } else {
                oldItem.setAmount(count);
            }
            this.setItem(slot,oldItem);

        } else {
            Bukkit.broadcastMessage("Failed add action");
            return false;
        }

        Bukkit.broadcastMessage("Inv: "+this.toString());

        return true;
    }

    /**
     * Remove a given amount from a given slot
     * @param slot slot to remove item from
     * @param count amount to remove
     * @param localStack item that the play thinks is in the slot
     * @return false if the local stack is wrong, true if it removed the items
     */
    public boolean removeItem(int slot, int count, ItemStack localStack) {
        ItemStack oldStack = this.getItem(slot);

        if ( oldStack == null ){
            Bukkit.broadcastMessage("When removing item, found no item to remove");
            Bukkit.broadcastMessage("Inv: "+this.toString());
            return false;

        } else if (ItemUtils.compareItems(localStack,oldStack)){

            if (oldStack.getAmount() == count) {
                Bukkit.broadcastMessage("When removing item, removed all the items");
                oldStack = null;

            } else if (count > oldStack.getAmount()) {
                Bukkit.broadcastMessage("When removing item, tried to remove more then in the slot");
                return false;

            } else {
                int remaining = oldStack.getAmount() - count;
                Bukkit.broadcastMessage("When removing item, left "+remaining+" in slot "+slot);
                oldStack.setAmount(remaining);
            }

            this.setItem(slot,oldStack);
        }else {
            Bukkit.broadcastMessage("When removing item, found wrong local stack");
        }
        return true;
    }

    /**
     * Used when you are replacing an item with another one and used to compare sync
     * @param slot the slot the action is occurring in
     * @param item the new item for the slot
     * @param localStack The item stack in that players slot for comparison
     * @return true if it succeeds, false if it fails
     */
    public boolean swapItem(int slot, ItemStack item, ItemStack localStack) {
        ItemStack oldStack = this.getItem(slot);

        if (oldStack == null && localStack == null){
            this.setItem(slot,item);
//            Bukkit.broadcastMessage("Swapped item from slot "+slot+" now "+item.getType());
        } else {
            if (ItemUtils.compareItems(localStack,oldStack)) {
                this.setItem(slot,item);
//                Bukkit.broadcastMessage("Swapped item from slot "+slot+" now "+item.getType());
            } else {
                return false;
            }
        }

        return true;
    }

    // complex

    /**
     * Automatically add an item to the first available slot of the type of action
     * @param item the item to add
     * @param startSlot the slot to start in, 0 = normal, 40 = with off hand then normal 9 = with main inv
     * @param reverse if to go in reverse when adding the items (Starts with the hotbar and goes right to left them bottom left of the inv
     * @param ignoreOffhand if to ignore the offhand when searching for a slot to add the items too.
     * @return true if it added all the items correctly, false if it failed to add them all
     */
    public boolean autoAddItem(ItemStack item, int startSlot, boolean reverse, boolean ignoreOffhand) {
        Bukkit.broadcastMessage("Auto Adding item "+item.getType()+"*"+item.getAmount());
        int emptySlot = -1;
        int count = item.getAmount();

        int i = startSlot;
        if (startSlot == 40) {
            i = 0;
            if (ItemUtils.compareForCombining(offHand, item)){
                Bukkit.broadcastMessage("Adding to off hand (at start)");
                count = addSome(40,count);
            }
        }

        if (count < 1) {
            return true;
        }

        while(i < inventoryContents.length){
            int slot = i;
            if (reverse) {
                if (i < 9) {
                    slot = 8-i;
                } else {
                    slot = inventoryContents.length - 1 - i + 9;
                }
            }

//            Bukkit.broadcastMessage("Checking Slot "+slot);
            ItemStack slotItem = inventoryContents[slot];
            if (slotItem == null) {
                if (emptySlot == -1) {
//                    Bukkit.broadcastMessage("First empty slot at "+i);
                    emptySlot = slot;
                }
            } else if (slotItem.getType() == Material.AIR) {
                if (emptySlot == -1) {
//                    Bukkit.broadcastMessage("First empty slot at "+i);
                    emptySlot = slot;
                }
            } else if (ItemUtils.compareForCombining(slotItem,item)) {
                count = this.addSome(slot,count);
            }

            if (count==0) {
                break;
            }
            i++;
        }

        if (startSlot != 40 && !ignoreOffhand && count > 0) {
            if (ItemUtils.compareForCombining(offHand,item)){
                Bukkit.broadcastMessage("Adding to off hand");
                count = this.addSome(40,item.getAmount());
            }
        }

        Bukkit.broadcastMessage("Count "+ count);
        if (count > 0) {
            Bukkit.broadcastMessage("Adding to empty Slot "+emptySlot+" * "+count);
            item.setAmount(count);
//            Bukkit.broadcastMessage("Count of item:"+item.getAmount());
            this.setItem(emptySlot,item);
//            Bukkit.broadcastMessage("Items in slot "+emptySlot+" are "+getItem(emptySlot).getAmount());
        }

        return true;
    }


    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i <= 40; i++) {
            ItemStack item = this.getItem(i);
            if (item == null) {
                output.append(i+" NULL  ");
            } else {
                output.append(i+" : "+item.getType()+"*"+item.getAmount()+"  ");
            }
        }

        return output.toString();
    }

}
