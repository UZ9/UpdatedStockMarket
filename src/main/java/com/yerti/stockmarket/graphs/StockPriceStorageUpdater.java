package com.yerti.stockmarket.graphs;

import com.yerti.stockmarket.api.StockMarketAPI;
import com.yerti.stockmarket.stocks.Stock;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class StockPriceStorageUpdater {

    public StockPriceStorageUpdater(Plugin plugin, FileConfiguration storageConfig, StockPriceStorage stockStorage) {

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {

            for (Stock stock : StockMarketAPI.retrieveStocks()) {
                for (int i = 5; i > 1; i--) {
                    if (stockStorage.getPlayerConfig().getDouble(stock.getID() + "." + (i - 1)) != 0) {
                        stockStorage.getPlayerConfig().set(stock.getID() + "." + i, stockStorage.getPlayerConfig().getDouble(stock.getID() + "." + (i - 1)));
                    }
                }
                stockStorage.getPlayerConfig().set(stock.getID() + ".1", stock.getPrice());
            }

            stockStorage.savePlayersFile();


        }, 60L, 20L * 60L);

    }


    //new: 3000
    //1: 200
}
