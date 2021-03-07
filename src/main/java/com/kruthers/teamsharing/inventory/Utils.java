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

import com.kruthers.teamsharing.TeamSharing;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Utils {
    private static Set<Material> bootItems = new HashSet<>();
    private static Set<Material> legItems = new HashSet<>();
    private static Set<Material> chestItems = new HashSet<>();
    private static Set<Material> headItems = new HashSet<>();

    static {
        bootItems.add(Material.NETHERITE_BOOTS);
        bootItems.add(Material.DIAMOND_BOOTS);
        bootItems.add(Material.GOLDEN_BOOTS);
        bootItems.add(Material.CHAINMAIL_BOOTS);
        bootItems.add(Material.IRON_BOOTS);
        bootItems.add(Material.LEATHER_BOOTS);

        legItems.add(Material.NETHERITE_LEGGINGS);
        legItems.add(Material.DIAMOND_LEGGINGS);
        legItems.add(Material.GOLDEN_LEGGINGS);
        legItems.add(Material.CHAINMAIL_LEGGINGS);
        legItems.add(Material.IRON_LEGGINGS);
        legItems.add(Material.LEATHER_LEGGINGS);

        chestItems.add(Material.NETHERITE_CHESTPLATE);
        chestItems.add(Material.DIAMOND_CHESTPLATE);
        chestItems.add(Material.GOLDEN_CHESTPLATE);
        chestItems.add(Material.CHAINMAIL_CHESTPLATE);
        chestItems.add(Material.IRON_CHESTPLATE);
        chestItems.add(Material.LEATHER_CHESTPLATE);

        headItems.add(Material.NETHERITE_HELMET);
        headItems.add(Material.DIAMOND_HELMET);
        headItems.add(Material.GOLDEN_HELMET);
        headItems.add(Material.CHAINMAIL_HELMET);
        headItems.add(Material.IRON_HELMET);
        headItems.add(Material.LEATHER_HELMET);
        headItems.add(Material.CARVED_PUMPKIN);
        headItems.add(Material.WITHER_SKELETON_SKULL);
        headItems.add(Material.PLAYER_HEAD);
        headItems.add(Material.ZOMBIE_HEAD);
        headItems.add(Material.CREEPER_HEAD);
        headItems.add(Material.SKELETON_SKULL);
    }

    /**
     * Used to compare 2 items to see if they are the same
     * @param item1 the primary item
     * @param item2 the secondary item
     * @param checkCount this will check if the count also matches of the 2
     * @return true if they pass all checks
     */
    public static boolean compareItems(ItemStack item1, ItemStack item2, @NonNull boolean checkCount) {
        if (item1 == null || item2 == null) {
            if (item1 == null && item2 ==null) {
                return true;
            } else if (item1 == null) {
                return item2.getType() == Material.AIR;
            } else {
                return item1.getType() == Material.AIR;

            }
        } else if (item1.getType() == Material.AIR && item2.getType() == Material.AIR) {
            return true;
        }

        // Check if they are the say item type
        if (item1.getType() != item2.getType()) {
            Bukkit.broadcastMessage("Failed on item type check");
            return false;
        }

        // Checks if they are being added to each other they wont go above the max count size
        if (checkCount && item1.getAmount() != item2.getAmount()) {
            Bukkit.broadcastMessage("Failed on item amount check");
            return false;
        }

        // Compare the item meta to ensure they are the same
        if (item1.getItemMeta() == item2.getItemMeta()) {
            Bukkit.broadcastMessage("Failed on item meta check");
            return false;
        }

        return true; // All good

    }

    /**
     * used to load an inv to the cached team
     * @param inv
     * @param team
     * @param excluded A player to exclude
     */
    public static void loadInvToTeam(CustomInventory inv, String team, UUID excluded) {
        for (Player player : TeamSharing.getTeamPlayers(team)) {
            if (player.getUniqueId() == excluded) {
                continue;
            }

            inv.LoadToPlayer(player);
        }
    }


    public static boolean checkIfItemValid(int slot, @NonNull ItemStack item) throws IndexOutOfBoundsException {
        if (slot >= -1 && slot <= 35 || slot == 40) {
            return true;
        } else if (slot == 36) {
            return bootItems.contains(item.getType());
        } else if (slot == 37) {
            return legItems.contains(item.getType());
        } else if (slot == 38) {
            return chestItems.contains(item.getType());
        } else if (slot == 39) {
            return headItems.contains(item.getType());
        } else {
            throw new IndexOutOfBoundsException("Slot "+slot+" out of bounds for player inventory");
        }
    }

}
