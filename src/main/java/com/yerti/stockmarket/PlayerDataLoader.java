package com.yerti.stockmarket;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataLoader implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        StockMarket.getInstance().getStockManager().loadPlayer(event.getPlayer());
    }

}
