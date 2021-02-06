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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class BlockEvents implements Listener {
    private List<Material> wallBlocks = new ArrayList<>();
    private List<Material> ignoredBlocks = new ArrayList<>();

    public BlockEvents() {
        //wall block list
        wallBlocks.add(Material.WALL_TORCH);

        wallBlocks.add(Material.WARPED_WALL_SIGN);
        wallBlocks.add(Material.ACACIA_WALL_SIGN);
        wallBlocks.add(Material.BIRCH_WALL_SIGN);
        wallBlocks.add(Material.CRIMSON_WALL_SIGN);
        wallBlocks.add(Material.DARK_OAK_WALL_SIGN);
        wallBlocks.add(Material.JUNGLE_WALL_SIGN);
        wallBlocks.add(Material.OAK_WALL_SIGN);
        wallBlocks.add(Material.SPRUCE_WALL_SIGN);

        wallBlocks.add(Material.WITHER_SKELETON_WALL_SKULL);
        wallBlocks.add(Material.SKELETON_WALL_SKULL);
        wallBlocks.add(Material.ZOMBIE_WALL_HEAD);
        wallBlocks.add(Material.PLAYER_WALL_HEAD);
        wallBlocks.add(Material.CREEPER_WALL_HEAD);

        wallBlocks.add(Material.BRAIN_CORAL_WALL_FAN);
        wallBlocks.add(Material.DEAD_BRAIN_CORAL_WALL_FAN);
        wallBlocks.add(Material.BUBBLE_CORAL_WALL_FAN);
        wallBlocks.add(Material.DEAD_BUBBLE_CORAL_WALL_FAN);
        wallBlocks.add(Material.FIRE_CORAL_WALL_FAN);
        wallBlocks.add(Material.DEAD_FIRE_CORAL_WALL_FAN);
        wallBlocks.add(Material.HORN_CORAL_WALL_FAN);
        wallBlocks.add(Material.DEAD_HORN_CORAL_WALL_FAN);
        wallBlocks.add(Material.TUBE_CORAL_WALL_FAN);
        wallBlocks.add(Material.DEAD_TUBE_CORAL_WALL_FAN);

        wallBlocks.add(Material.WHITE_WALL_BANNER);
        wallBlocks.add(Material.ORANGE_WALL_BANNER);
        wallBlocks.add(Material.MAGENTA_WALL_BANNER);
        wallBlocks.add(Material.LIGHT_BLUE_WALL_BANNER);
        wallBlocks.add(Material.YELLOW_WALL_BANNER);
        wallBlocks.add(Material.LIME_WALL_BANNER);
        wallBlocks.add(Material.PINK_WALL_BANNER);
        wallBlocks.add(Material.GRAY_WALL_BANNER);
        wallBlocks.add(Material.LIGHT_GRAY_WALL_BANNER);
        wallBlocks.add(Material.CYAN_WALL_BANNER);
        wallBlocks.add(Material.PURPLE_WALL_BANNER);
        wallBlocks.add(Material.BLUE_WALL_BANNER);
        wallBlocks.add(Material.BROWN_WALL_BANNER);
        wallBlocks.add(Material.GREEN_WALL_BANNER);
        wallBlocks.add(Material.RED_WALL_BANNER);
        wallBlocks.add(Material.BLACK_WALL_BANNER);

        //Blocks that you can place by creating
        ignoredBlocks.add(Material.STRIPPED_ACACIA_LOG);
        ignoredBlocks.add(Material.STRIPPED_ACACIA_WOOD);
        ignoredBlocks.add(Material.STRIPPED_BIRCH_LOG);
        ignoredBlocks.add(Material.STRIPPED_BIRCH_WOOD);
        ignoredBlocks.add(Material.STRIPPED_CRIMSON_HYPHAE);
        ignoredBlocks.add(Material.STRIPPED_CRIMSON_STEM);
        ignoredBlocks.add(Material.STRIPPED_DARK_OAK_LOG);
        ignoredBlocks.add(Material.STRIPPED_DARK_OAK_WOOD);
        ignoredBlocks.add(Material.STRIPPED_JUNGLE_LOG);
        ignoredBlocks.add(Material.STRIPPED_JUNGLE_WOOD);
        ignoredBlocks.add(Material.STRIPPED_OAK_LOG);
        ignoredBlocks.add(Material.STRIPPED_OAK_WOOD);
        ignoredBlocks.add(Material.STRIPPED_SPRUCE_LOG);
        ignoredBlocks.add(Material.STRIPPED_SPRUCE_WOOD);
        ignoredBlocks.add(Material.STRIPPED_WARPED_HYPHAE);
        ignoredBlocks.add(Material.STRIPPED_WARPED_STEM);

        ignoredBlocks.add(Material.CARVED_PUMPKIN);
        ignoredBlocks.add(Material.FARMLAND);
        ignoredBlocks.add(Material.GRASS_PATH);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        Material placedItem = event.getBlockPlaced().getType();
        ItemStack mainItem = player.getInventory().getItemInMainHand();
        ItemStack offItem = player.getInventory().getItemInOffHand();

        if (wallBlocks.contains(placedItem)){
            //convert it if is a wall variant
            String modify = placedItem.toString();
            modify = modify.replace("WALL_","");
            placedItem = Material.getMaterial(modify);

            //Seed parsing
        } else if (placedItem == Material.WHEAT) {
            placedItem = Material.WHEAT_SEEDS;
        } else if (placedItem == Material.CARROTS) {
            placedItem = Material.CARROT;
        } else if (placedItem == Material.POTATOES) {
            placedItem = Material.POTATO;
        } else if (placedItem == Material.BEETROOTS) {
            placedItem = Material.BEETROOT_SEEDS;
        } else if (placedItem == Material.SWEET_BERRY_BUSH) {
            placedItem = Material.SWEET_BERRIES;
        } else if (placedItem == Material.MELON_STEM) {
            placedItem = Material.MELON_SEEDS;
        } else if (placedItem == Material.PUMPKIN_STEM) {
            placedItem = Material.PUMPKIN_SEEDS;
        }

        if (mainItem.getType() == placedItem) {
            int slot = player.getInventory().getHeldItemSlot();
            ItemStack item = inv.getItem(slot);
            int count = item.getAmount();
            count -= 1;
            if ( count <= 0 ) {
                inv.setItem(slot, null);
            } else {
                item.setAmount(count);
                inv.setItem(slot,item);
            }

            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);

        } else if (offItem.getType() == placedItem) {
            ItemStack item = inv.getItem(40);
            int count = item.getAmount();
            count -= 1;
            if ( count <= 0 ) {
                inv.setItem(40, null);
            } else {
                item.setAmount(count);
                inv.setItem(40,item);
            }

            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);
        } else if(ignoredBlocks.contains(placedItem)) {
            return;
        } else {
            player.sendMessage(ChatColor.RED+"Your inventory has de-synced, re-syncing...");
            event.setCancelled(true);
        }

    }



    @EventHandler
    public void onBoneMeal(BlockFertilizeEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team == null) return;
            TeamInventory inv = TeamSharing.getInventory(team.getName());
            if (inv == null) return;

            ItemStack mainItem = player.getInventory().getItemInMainHand();
            ItemStack offItem = player.getInventory().getItemInOffHand();

            if (mainItem.getType() == Material.BONE_MEAL) {
                int slot = player.getInventory().getHeldItemSlot();
                ItemStack item = inv.getItem(slot);
                int count = item.getAmount();
                count -= 1;
                if ( count <= 0 ) {
                    inv.setItem(slot, null);
                } else {
                    item.setAmount(count);
                    inv.setItem(slot,item);
                }

                TeamSharing.setTeamInventory(team.getName(),inv);
                TeamManagement.syncTeam(team,player);

            } else if (offItem.getType() == Material.BONE_MEAL) {
                ItemStack item = inv.getItem(40);
                int count = item.getAmount();
                count -= 1;
                if ( count <= 0 ) {
                    inv.setItem(40, null);
                } else {
                    item.setAmount(count);
                    inv.setItem(40,item);
                }

                TeamSharing.setTeamInventory(team.getName(),inv);
                TeamManagement.syncTeam(team,player);
            } else {
                player.sendMessage(ChatColor.RED+"Your inventory looks to have de-synced, re-syncing...");
                event.setCancelled(true);
            }
        }
    }

}
