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

package com.kruthers.teamsharing.inventory;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CustomInventory {

    @Getter
    @Setter
    private ItemStack[] contents = new ItemStack[40];

    public int counter = 0;

    public CustomInventory(Inventory inv){
        this.contents = inv.getContents();
        ItemStack air = new ItemStack(Material.AIR);
        for (int i = 0; i <this.contents.length; i ++) {
            ItemStack item = this.contents[i];
            if (item == null) {
                this.contents[i] = air.clone();
            }
        }
    }

    public CustomInventory() {
        ItemStack air = new ItemStack(Material.AIR);

        for (int i = 0; i <this.contents.length; i ++) {
            ItemStack item = this.contents[i];
            if (item == null) {
                this.contents[i] = air.clone();
            }
        }
    }

    /**
     * Set an item in a given slot to be another item (over rights)
     * @param stack The itemstack to set that slot too
     * @param slot the slot to set
     * @throws IndexOutOfBoundsException
     */
    protected void setItem(ItemStack stack, @NonNull int slot) throws IndexOutOfBoundsException{
        if (stack == null) {
            stack = new ItemStack(Material.AIR);
        }

        if (slot > this.contents.length || slot < 0) {
            throw new IndexOutOfBoundsException("slot "+slot+" out of bounds for inventory");
        }

        this.contents[slot] = stack;

    }

    /**
     * Gets the given item
     * @param slot slot to get the item from
     * @return The item in that slot Air if none
     * @throws IndexOutOfBoundsException
     */
    protected ItemStack getItem(@NonNull int slot) throws IndexOutOfBoundsException {
        if (slot > this.contents.length || slot < 0) {
            throw new IndexOutOfBoundsException("slot "+slot+" out of bounds for inventory");
        }

        return this.contents[slot];
    }

    /**
     * Add the item to the one in the slot already
     * @param item the item you want to add
     * @param slot the slot to add it too
     * @return any remaining once added
     * @throws IndexOutOfBoundsException If slot is not in the player's inventory
     */
    public int addItem(@NonNull ItemStack item, @NonNull int slot) throws IndexOutOfBoundsException {
        if (slot > this.contents.length || slot < 0) {
            throw new IndexOutOfBoundsException("slot "+slot+" out of bounds for inventory");
        }

        ItemStack oldItem = this.getItem(slot);
        if (oldItem.getType() == Material.AIR) {
            this.setItem(item,slot);
            return 0;

        } else if (Utils.compareItems(oldItem,item,false)) {
            int count = oldItem.getAmount();
            int remaining = 0;
            count += item.getAmount();
            if (count > oldItem.getMaxStackSize()) {
                remaining = count - oldItem.getMaxStackSize();
                count = oldItem.getMaxStackSize();
            }

            oldItem.setAmount(count);
            this.setItem(oldItem,slot);

            return remaining;

        } else {
            return item.getAmount();

        }
    }

    private boolean removeCount(@NonNull int count, @NonNull int slot) {
        ItemStack oldItem = this.getItem(slot);
        int amount = oldItem.getAmount();
        amount -= count;

        if (amount == 0) {
            this.setItem(null,slot);
        } else if (amount > 0) {
            oldItem.setAmount(amount);
            this.setItem(oldItem,slot);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Used to remove items from the inventory
     * @param localItem The item that is ment to be being removed
     * @param count The count of it to remove
     * @param slot The slot it is in
     * @return If it succeeded
     * @throws IndexOutOfBoundsException
     */
    public boolean removeItem(@NonNull ItemStack localItem, @NonNull int count, @NonNull int slot) throws IndexOutOfBoundsException {
        if (slot > this.contents.length || slot < 0) {
            throw new IndexOutOfBoundsException("slot "+slot+" out of bounds for inventory");
        }

        ItemStack oldItem = this.getItem(slot);
        if (Utils.compareItems(localItem,oldItem,true)) {
            return this.removeCount(count,slot);
        } else if (localItem.getType() == Material.AIR) {
            return this.removeCount(count,slot);
        } else {
            return false;
        }
    }

    public boolean autoAddItem(@NonNull ItemStack item, @NonNull boolean checkOffHand, @NonNull boolean reverse, @NonNull int startSlot) throws IndexOutOfBoundsException {
        int count = item.getAmount();

        if (startSlot != 0 && startSlot <= 35 || startSlot == 40) {
            ItemStack checkItem = this.getItem(startSlot);
            if (Utils.compareItems(checkItem,item,false)) {
                count = this.addItem(item,startSlot);
            }
        } else {
            throw new IndexOutOfBoundsException("Starting slot "+startSlot+" out of bounds for inventory");
        }

        if (count <= 0) {
            return true;
        }


        //cycle though main inventory
        int firstEmptySlot = -1;
        for (int i = 0; i < 36; i++){
            int slot = i;
            if (reverse) {
                if (i < 9) {
                    slot = 8-i;
                } else {
                    slot = 53-i+9;
                }
            }

            ItemStack slotItem = this.getItem(slot);
            if (slotItem.getType() == Material.AIR) {
                if (firstEmptySlot == -1){
                    firstEmptySlot = slot;
                }
            } else {
                count = this.addItem(item,slot);
                item.setAmount(count);
            }

            if(count == 0){
                return true;
            }
        }

        if (count > 0){
            if (firstEmptySlot != -1) {
                this.setItem(item,firstEmptySlot);
            }
        }

        return false;

    }

    public void LoadToPlayer(Player player) {
        player.getInventory().setContents(this.contents);

    }

    @Override
    public String toString() {
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i < this.contents.length; i++) {
            ItemStack item = this.contents[i];
            if (item == null) {
                contents.append(ChatColor.GOLD+""+i+ChatColor.WHITE+": "+ChatColor.GREEN+"EMPTY");
            }  else {
                contents.append(ChatColor.GOLD+""+i+ChatColor.WHITE+": "+ChatColor.GREEN+item.getType());
            }

            if (i < (this.contents.length-1)) {
                contents.append(ChatColor.WHITE+", ");
            }
        }

        return "{"+ ChatColor.AQUA +"Contents"+ ChatColor.WHITE +": ["+contents+ChatColor.WHITE +"], "
                +ChatColor.AQUA+"UpdateCount"+ChatColor.WHITE+": "+ChatColor.GREEN+this.counter+ChatColor.WHITE+"}";
    }

}
