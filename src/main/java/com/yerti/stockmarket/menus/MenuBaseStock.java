package com.yerti.stockmarket.menus;

import com.yerti.stockmarket.core.inventories.IInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

//TODO: Add menu for between the list of stock and the player log
public class MenuBaseStock implements IInventory {
    @Override
    public void onGUI(Player player, int slot, ItemStack clickedItem, InventoryClickEvent event) {

    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
