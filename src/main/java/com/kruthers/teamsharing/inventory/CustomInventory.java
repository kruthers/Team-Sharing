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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class CustomInventory {

    @Getter
    @Setter
    private ItemStack[] contents = new ItemStack[41];

    public CustomInventory(Inventory inv){
        ItemStack air = new ItemStack(Material.AIR);
        for (int i = 0; i <inv.getContents().length && i < 41; i ++) {
            ItemStack item = inv.getItem(i);
            this.setItem(item,i);
        }
    }

    public CustomInventory() {
        ItemStack air = new ItemStack(Material.AIR);

        for (int i = 0; i <= 40; i ++) {
            ItemStack item = this.getItem(i);
            if (item == null) {
                this.setItem(air.clone(),i);
            }
        }
    }

    /**
     * Set an item in a given slot to be another item (over rights)
     * @param stack The itemstack to set that slot too
     * @param slot the slot to set
     * @throws IndexOutOfBoundsException
     */
    public void setItem(ItemStack stack, @NonNull int slot) throws IndexOutOfBoundsException{
        if (slot < 0 || slot > 40) {
            throw new IndexOutOfBoundsException("slot "+slot+" out of bounds for inventory");
        }

        if (stack == null) {
            stack = new ItemStack(Material.AIR);
        }

        this.contents[slot] = stack.clone();

    }

    /**
     * Gets the given item
     * @param slot slot to get the item from
     * @return The item in that slot Air if none
     * @throws IndexOutOfBoundsException
     */
    public ItemStack getItem(@NonNull int slot) throws IndexOutOfBoundsException {
        if (slot < 0 || slot > 40) {
            throw new IndexOutOfBoundsException("slot "+slot+" out of bounds for inventory");
        }

        ItemStack item = this.contents[slot];

        if (item == null) {
            return new ItemStack(Material.AIR);
        } else {
            return item;
        }
    }

    /**
     * Add the item to the one in the slot already
     * @param item the item you want to add
     * @param slot the slot to add it too
     * @return any remaining once added
     * @throws IndexOutOfBoundsException If slot is not in the player's inventory
     */
    public int addItem(@NonNull ItemStack item, @NonNull int slot) throws IndexOutOfBoundsException {
        if (slot > 40 || slot < 0) {
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
        if (slot > 40 || slot < 0) {
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

    public boolean autoAddItem(@NonNull ItemStack item, @NonNull boolean reverse, @NotNull int[] prioritySlot) throws IndexOutOfBoundsException {
        int count = item.getAmount();
        Bukkit.broadcastMessage("Added "+count+" of "+item.getType());

        if (prioritySlot.length > 0) {
            for (int slot : prioritySlot) {
                Bukkit.broadcastMessage("Checking priority slot, "+slot);
                ItemStack slotItem = this.getItem(slot);
                if (slotItem.getType() == Material.AIR) {
                    continue;
                } else {
                    count = this.addItem(item,slot); //Does the item consistency check first
                    item.setAmount(count);

                    if (count < 1) {
                        Bukkit.broadcastMessage("Added all (1)");
                        return true;
                    }
                }
            }
        }

        int emptySlot = -1;
        //cycle though the main inventory
        for (int i = 0; i <= 35; i++) {
            int slot = i;
            if (reverse) {
                if (i < 8) {
                    slot = 8-i;
                } else {
                    slot = 35-i+9;
                }
            }

            ItemStack slotItem = this.getItem(slot);

            if (slotItem.getType() == Material.AIR) {
                if (emptySlot == -1) {
                    emptySlot = slot;
                }
            } else {
                count = this.addItem(item,slot);
                item.setAmount(count);

                if (count < 1) {
                    Bukkit.broadcastMessage("Added all (2)");
                    return true;
                }
            }

        }

        if (emptySlot > -1) {
            this.setItem(item,emptySlot);
            return true;
        } else {
            Bukkit.broadcastMessage("Extra items exist, i am broken");
        }

        return false;

    }

    public void LoadToPlayer(Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i <= 40; i ++) {
            inv.setItem(i, this.getItem(i));
        }

    }

    public CustomInventory clone() {
        CustomInventory customInventory = new CustomInventory();
        for (int i = 0; i <= 40; i++) {
            customInventory.setItem(this.getItem(i),i);
        }

        return customInventory;
    }

    public ItemStack[] getArmorContents() {
        ItemStack[] armourContents = new ItemStack[4];
        armourContents[0] = this.getItem(36);
        armourContents[1] = this.getItem(37);
        armourContents[2] = this.getItem(38);
        armourContents[3] = this.getItem(39);

        return armourContents;
    }

    @Override
    public String toString() {
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i <= 40; i++) {
            ItemStack item = this.getItem(i);
            if (item == null) {
                contents.append(ChatColor.GOLD+""+i+ChatColor.WHITE+": "+ChatColor.GREEN+"NULL");
            } else if (item.getType() == Material.AIR) {
                contents.append(ChatColor.GOLD+""+i+ChatColor.WHITE+": "+ChatColor.GREEN+"EMPTY");
            } else {
                contents.append(ChatColor.GOLD+""+i+ChatColor.WHITE+": "+ChatColor.GREEN+item.getType()+" * "+item.getAmount());
            }

            if (i < 40) {
                contents.append(ChatColor.WHITE+", ");
            }
        }

        return "{"+ ChatColor.AQUA +"Contents"+ ChatColor.WHITE +": ["+contents+ChatColor.WHITE +"]}";
    }

}
