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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Team;

public class ToolEvents implements Listener {
    private ParserUtils parserUtils;

    public ToolEvents() {
        parserUtils = new ParserUtils();

    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack returnItem = event.getItemStack();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (mainHand.getType() == Material.BUCKET) {
            int slot = player.getInventory().getHeldItemSlot();
            inv.setItem(slot,returnItem);

        } else if (offHand.getType() == Material.BUCKET) {
            inv.setItem(40,returnItem);

        } else {
            player.sendMessage(ChatColor.RED+"Inventory de-sync detected, refreshing inv");
        }

    }


    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack returnItem = event.getItemStack();
        Material bucketUsed = event.getBucket();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (mainHand.getType() == bucketUsed) {
            int slot = player.getInventory().getHeldItemSlot();
            inv.setItem(slot,returnItem);

        } else if (offHand.getType() == bucketUsed) {
            inv.setItem(40,returnItem);

        } else {
            player.sendMessage(ChatColor.RED+"Inventory de-sync detected, refreshing inv");
        }

    }


    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack item = event.getItem();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ItemStack offItem = player.getInventory().getItemInOffHand();
        if (item.getType() == heldItem.getType()) {
            int slot = player.getInventory().getHeldItemSlot();
            inv = parserUtils.parseConsumable(inv,slot,item);
        } else if (item.getType() == offItem.getType()) {
            int slot = 40;
            inv = parserUtils.parseConsumable(inv,slot,item);
        } else {
            Bukkit.broadcastMessage(player.getName()+" managed to consume a "+item.getType());
        }

        TeamSharing.setTeamInventory(team.getName(),inv);
        TeamManagement.syncTeam(team,player);
    }


    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event){
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack item = event.getBrokenItem();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ItemStack offItem = player.getInventory().getItemInOffHand();

        if (item.getType() == heldItem.getType()) {
            int slot = player.getInventory().getHeldItemSlot();
            inv.setItem(slot, null);
        } else if (item.getType() == offItem.getType()) {
            inv.setItem(40, null);
        } else {
            ItemStack[] armorItems = inv.getArmourContents();
            for (int i = 0; i<4; i++) {
                ItemStack oldItem = armorItems[i];
                if (oldItem == null) continue;
                if (oldItem.getType() == item.getType()) {
                    inv.setItem(40,null);
                    break;
                }
            }
        }

        TeamSharing.setTeamInventory(team.getName(),inv);
        TeamManagement.syncTeam(team,player);
    }


    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event){
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.sharingActive) return;
        TeamInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        ItemStack offItem = player.getInventory().getItemInOffHand();
        ItemStack item = event.getItem();
        int dmg = event.getDamage();

        if (handItem.getType() == item.getType()){
            int slot = player.getInventory().getHeldItemSlot();
            inv = parserUtils.damageItem(inv,slot,dmg);
        } else if (offItem.getType() == item.getType()){
            inv = parserUtils.damageItem(inv,40,dmg);
        } else {
            ItemStack[] armorItems = inv.getArmourContents();
            for (int i = 0; i<4; i++){
                ItemStack oldItem = armorItems[i];
                if (oldItem == null) continue;
                if (oldItem.getType() == item.getType()){
                    inv = parserUtils.damageItem(inv,36+i,dmg);
                    break;
                }
            }
        }

        TeamSharing.setTeamInventory(team.getName(),inv);
        TeamManagement.syncTeam(team,player);
    }


    @EventHandler
    public void onBowUse(EntityShootBowEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player){
            Player player = (Player) entity;
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team == null || !TeamSharing.sharingActive) return;
            TeamInventory inv = TeamSharing.getInventory(team.getName());
            if (inv == null) return;

            ItemStack arrowShot = event.getConsumable();
            int slot = inv.findFirstSlot(arrowShot,true);

            if (slot != -1){
                ItemStack arrow = inv.getItem(slot);
                if (arrow.getAmount() < 2) {
                    inv.setItem(slot,null);
                } else {
                    int count = arrow.getAmount();
                    count -= 1;
                    arrow.setAmount(count);
                    inv.setItem(slot,arrow);
                }
            } else {
                player.sendMessage(ChatColor.RED+"Your Inv looks to have de-synced, re-syncing");
                event.setCancelled(true);
                inv.loadToPlayer(player);
                return;
            }

            TeamSharing.setTeamInventory(team.getName(),inv);
            TeamManagement.syncTeam(team,player);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        ProjectileSource source = event.getEntity().getShooter();
        if (source instanceof Player){
            Player player = (Player) source;
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team == null) return;
            TeamInventory inv = TeamSharing.getInventory(team.getName());
            if (inv == null) return;

            Projectile projectile = event.getEntity();
            Material type = null;
            if (projectile instanceof Egg) {
                type = Material.EGG;
            } else if (projectile instanceof EnderPearl) {
                type = Material.ENDER_PEARL;
            } else if (projectile instanceof EnderSignal) {
                type = Material.ENDER_EYE;
            } else {
                return;
            }

            ItemStack mainItem = player.getInventory().getItemInMainHand();
            ItemStack offItem = player.getInventory().getItemInOffHand();

            if (mainItem.getType() == type) {
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

            } else if (offItem.getType() == type) {
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
                player.sendMessage(ChatColor.RED+"Your inventory has de-synced, re-syncing...");
                inv.loadToPlayer(player);
                event.setCancelled(true);
            }

        }
    }


}
