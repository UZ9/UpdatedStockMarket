package com.yerti.stockmarket.core.inventories;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryHandler implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof IInventory) {
            event.setCancelled(true);
            IInventory inventory = (IInventory) event.getInventory().getHolder();
            inventory.onGUI((Player) event.getWhoClicked(), event.getRawSlot(), event.getCurrentItem(), event);
        }
    }


}
