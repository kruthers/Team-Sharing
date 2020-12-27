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

package com.kruthers.teamsharing.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {

    public static boolean checkMeta(ItemMeta item1, ItemMeta item2) {
        //general checks
        if (item1.getEnchants() != item2.getEnchants()) {
            return false;
        }
//        else if (item1.getItemFlags() != item2.getItemFlags()){
//
//            return false;
//        }

        Bukkit.broadcastMessage(item1.getPersistentDataContainer().getKeys()+ "");
        //model data checks
        if (item1.hasCustomModelData()) {
            if (item2.hasCustomModelData()) {
                if (item1.getCustomModelData() != item2.getCustomModelData()) {
                    return false;
                }
            } else {
                return false;
            }
        }

        //display name checks
        if (item1.hasDisplayName()) {
            if (item2.hasDisplayName()) {
                if (!item1.getDisplayName().equals(item2.getDisplayName())) {
                    return false;
                }
            } else {
                return false;
            }
        }

        //lang name checks
        if (item1.hasLocalizedName()) {
            if (item2.hasLocalizedName()) {
                if (!item1.getLocalizedName().equals(item2.getLocalizedName())) {
                    return false;
                }
            } else {
                return false;
            }
        }

        //Lore checks
        if (item1.hasLore()) {
            if (item2.hasLore()) {
                if (!item1.getLore().equals(item2.getLore())) {
                    Bukkit.broadcastMessage("Failed at lore check");
                    return false;
                }
            } else {
                Bukkit.broadcastMessage("Failed at lore check");
                return false;
            }
        }

        return true;
    }

    public static boolean compareItems(ItemStack item1, ItemStack item2){
        if (item1 != null && item2 != null) {
            if (item1.getType() == item2.getType()){
                ItemMeta item1Meta = item1.getItemMeta();
                ItemMeta item2Meta = item2.getItemMeta();
                if (item1Meta != null && item2Meta != null) {
                    if (ItemUtils.checkMeta(item1Meta.clone(),item2Meta.clone())){
                        return true;
                    }
                } else if (item1Meta == null && item2Meta == null) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean compareForCombining(ItemStack exists, ItemStack adding) {
        if (exists != null && adding != null){
            if (exists.getAmount() < adding.getMaxStackSize()) {
                return compareItems(exists,adding);
            } else {
                return false;
            }
        }

        return false;
    }

    public static boolean checkArmourSlot(int slot, ItemStack item) {
        if (item == null) {
            return false;
        }
        //boots
        if (slot == 36) {
            List<Material> validItems = new ArrayList<>();
            validItems.add(Material.NETHERITE_BOOTS);
            validItems.add(Material.DIAMOND_BOOTS);
            validItems.add(Material.GOLDEN_BOOTS);
            validItems.add(Material.IRON_BOOTS);
            validItems.add(Material.CHAINMAIL_BOOTS);
            validItems.add(Material.LEATHER_BOOTS);

            if (validItems.contains(item.getType())) {
                return true;
            }

        } else if (slot == 37) {
            //leggins
            List<Material> validItems = new ArrayList<>();
            validItems.add(Material.NETHERITE_LEGGINGS);
            validItems.add(Material.DIAMOND_LEGGINGS);
            validItems.add(Material.GOLDEN_LEGGINGS);
            validItems.add(Material.IRON_LEGGINGS);
            validItems.add(Material.CHAINMAIL_LEGGINGS);
            validItems.add(Material.LEATHER_LEGGINGS);

            if (validItems.contains(item.getType())) {
                return true;
            }

        } else if (slot == 38) {
            //chestplate
            List<Material> validItems = new ArrayList<>();
            validItems.add(Material.NETHERITE_CHESTPLATE);
            validItems.add(Material.DIAMOND_CHESTPLATE);
            validItems.add(Material.GOLDEN_CHESTPLATE);
            validItems.add(Material.IRON_CHESTPLATE);
            validItems.add(Material.CHAINMAIL_CHESTPLATE);
            validItems.add(Material.LEATHER_CHESTPLATE);

            if (validItems.contains(item.getType())) {
                return true;
            }

        } else if (slot == 39) {
            //boots
            List<Material> validItems = new ArrayList<>();
            validItems.add(Material.NETHERITE_HELMET);
            validItems.add(Material.DIAMOND_HELMET);
            validItems.add(Material.GOLDEN_HELMET);
            validItems.add(Material.IRON_HELMET);
            validItems.add(Material.CHAINMAIL_HELMET);
            validItems.add(Material.LEATHER_HELMET);
            validItems.add(Material.TURTLE_HELMET);
            validItems.add(Material.CARVED_PUMPKIN);
            validItems.add(Material.PLAYER_HEAD);
            validItems.add(Material.SKELETON_SKULL);
            validItems.add(Material.ZOMBIE_HEAD);
            validItems.add(Material.WITHER_SKELETON_SKULL);

            if (validItems.contains(item.getType())) {
                return true;
            }
        }

        return false;
    }
}
