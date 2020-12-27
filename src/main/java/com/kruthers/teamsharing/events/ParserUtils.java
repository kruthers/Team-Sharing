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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ParserUtils {

    public TeamInventory parseConsumable(TeamInventory inv, int slot, ItemStack item) {
        if (item.getType() == Material.POTION) {
            ItemStack glassBottle = new ItemStack(Material.GLASS_BOTTLE);
            inv.setItem(slot,glassBottle);

        } else if (item.getType()==Material.MILK_BUCKET) {
            inv.setItem(slot,null);

        } else {
            int count = item.getAmount();
            count -=1;
            if (count < 1){
                inv.setItem(slot,null);

            } else {
                item.setAmount(count);
                inv.setItem(slot,item);
            }
        }
        return inv;
    }

    public TeamInventory damageItem(TeamInventory inv, int slot, int dmg){
        ItemStack item = inv.getItem(slot);
        Damageable itemMeta = (Damageable) item.getItemMeta();
        itemMeta.setDamage(itemMeta.getDamage()+dmg);
        item.setItemMeta((ItemMeta) itemMeta);

        if (itemMeta.getDamage() >= item.getType().getMaxDurability()){
            item = null;
        }

        inv.setItem(slot,item);

        return inv;
    }

}
