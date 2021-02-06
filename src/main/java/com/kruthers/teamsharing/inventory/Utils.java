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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Utils {

    /**
     * Used to compare 2 items to see if they are the same
     * @param item1 the primary item
     * @param item2 the secondary item
     * @param checkCount this will check if the count also matches of the 2
     * @return true if they pass all checks
     */
    public static boolean compareItems(@NonNull ItemStack item1, @NonNull ItemStack item2, @NonNull boolean checkCount) {
        // if it is a max stack of 1 they cant mix
        if (item1.getMaxStackSize() == 1 || item2.getMaxStackSize() == 1) {
            return false;
        }

        // Check if they are the say item type
        if (item1.getType() != item2.getType()) {
            return false;
        }

        // Checks if they are being added to each other they wont go above the max count size
        if (checkCount && item1.getAmount() == item2.getAmount()) {
            return false;
        }

        // Compare the item meta to ensure they are the same
        if (item1.getItemMeta() == item2.getItemMeta()) {
            Bukkit.broadcastMessage("Failed on item meta check (inventory.Utils.class:44)");
            return false;
        }

        return true; // All good

    }

    public static void loadInvToTeam(CustomInventory inv, String team, UUID excluded) {
        for (Player player : TeamSharing.getTeamPlayers(team)) {
            if (player.getUniqueId() == excluded) {
                continue;
            }

            inv.LoadToPlayer(player);
        }
    }

}
