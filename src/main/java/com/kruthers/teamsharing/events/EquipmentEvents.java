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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;

public class EquipmentEvents implements Listener {

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;


        Bukkit.broadcastMessage("Damaging an item");
        ItemStack item = event.getItem();

        int handSlot = player.getInventory().getHeldItemSlot();
        int slot = -1;
        for (int i = 0; i < 4; i++) {
            ItemStack armour = inv.getItem(36+i);
            if (item.getType() == armour.getType()) {
                slot = 36+i;
                break;
            }
        }

        if (slot == -1 && inv.getItem(handSlot).getType() == item.getType()) {
            slot = handSlot;
        } else if (slot == -1 && inv.getItem(40).getType() == item.getType()) {
            slot = 40;
        }


        boolean canceled = false;
        if (slot != -1) {
            ItemStack invItem = inv.getItem(slot).clone();
            int damage = event.getDamage();
            Damageable meta = (Damageable) item.getItemMeta();

            meta.setDamage(meta.getDamage()+damage);

            invItem.setItemMeta((ItemMeta) meta);
            inv.setItem(invItem,slot);

        } else {
            canceled = true;
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
    public void onItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack item = event.getBrokenItem();
        int slot = -1;
        int handSlot = player.getInventory().getHeldItemSlot();

        for (int i = 0; i < inv.getArmorContents().length; i++) {
            if (Utils.compareItems(item,inv.getItem(i+36),false)) {
                slot = 36+i;
                break;
            }
        }

        if (slot != -1 && Utils.compareItems(item,inv.getItem(handSlot),false)) {
            slot = handSlot;
        } else if (slot != -1 && Utils.compareItems(item,inv.getItem(40),false)) {
            slot = 40;
        }

        if (slot == -1) {
            player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
            inv.LoadToPlayer(player);

        } else {
            inv.setItem(null,slot);
            Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
            TeamSharing.setInventory(team.getName(),inv);
        }

    }

    @EventHandler
    public void onBowUse(EntityShootBowEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team == null || !TeamSharing.isSharingActive()) return;
            CustomInventory inv = TeamSharing.getInventory(team.getName());
            if (inv == null) return;

            if (player.getGameMode() == GameMode.CREATIVE) return;


            int slot = -1;
            ItemStack projectile = event.getBow();
            if (inv.getItem(40).getType() == projectile.getType()) {
                slot = 40;
            } else {
                for (int i = 0; i <= 35; i++) {
                    if (inv.getItem(i).getType() == projectile.getType()) {
                        slot = i;
                        break;
                    }
                }
            }

            if (slot > -1) {
                ItemStack item = inv.getItem(slot).clone();
                int count = item.getAmount();
                count-=1;

                if (count < 1) {
                    inv.setItem(null,slot);
                } else {
                    item.setAmount(count);
                    inv.setItem(item,slot);
                }

                Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
                TeamSharing.setInventory(team.getName(),inv);

            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
                inv.LoadToPlayer(player);
            }
        }
    }


    @EventHandler
    public void onItemUse(PlayerItemConsumeEvent event) {
        // Default checks to check that the system is active and that they are ona  team
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        ItemStack item = event.getItem();
        int slot = player.getInventory().getHeldItemSlot();
        boolean canceled = false;

        if (!Utils.compareItems(item,inv.getItem(slot),true)) {
            canceled = true;
        } else {
            if (item.getType() == Material.POTION || item.getType() == Material.HONEY_BOTTLE) {
                ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE);
                inv.setItem(bottle,slot);
            } else {
                if (item.getAmount() > 1) {
                    int count = item.getAmount();
                    count--;
                    item.setAmount(count);
                    inv.setItem(item,slot);
                } else {
                    inv.setItem(null,slot);
                }
            }
        }

        if (canceled) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED+"Inventory desynced, resyncing");
            inv.LoadToPlayer(player);

        } else {
            Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
            TeamSharing.setInventory(team.getName(),inv);

        }

    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        boolean canceled = false;
        int handSlot = player.getInventory().getHeldItemSlot();
        if (inv.getItem(handSlot).getType() == Material.BUCKET) {
            Bukkit.broadcastMessage(event.getBucket()+"");
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !TeamSharing.isSharingActive()) return;
        CustomInventory inv = TeamSharing.getInventory(team.getName());
        if (inv == null) return;

        boolean canceled = false;
        int handSlot = player.getInventory().getHeldItemSlot();
        Material bucket = event.getBucket();
        ItemStack emptyBucket = new ItemStack(Material.BUCKET);

        if (inv.getItem(handSlot).getType() == bucket) {
            inv.setItem(emptyBucket,handSlot);
        } else if (inv.getItem(40).getType() == bucket) {
            inv.setItem(emptyBucket,40);
        }

        Utils.loadInvToTeam(inv,team.getName(),player.getUniqueId());
        TeamSharing.setInventory(team.getName(),inv);

    }

}
