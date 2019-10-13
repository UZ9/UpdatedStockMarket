package com.yerti.stockmarket.placeholders;


import com.yerti.stockmarket.stocks.PlayerStock;
import com.yerti.stockmarket.stocks.PlayerStocks;
import com.yerti.stockmarket.stocks.Stock;
import com.yerti.stockmarket.stocks.Stocks;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class StockPlaceholder extends PlaceholderExpansion {

    Plugin plugin;

    public StockPlaceholder(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister(){
        return true;
    }


    @Override
    public String getIdentifier() {
        return "stockmarket";
    }

    @Override
    public String getAuthor() {
        return "IncompatibleType/Yerti";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player,  String identifier) {
        if (identifier.startsWith("stock_")) {
            for (Stock stock : new PlayerStocks(player.getPlayer()).retrieveStocks()) {
                if (stock.getName().equalsIgnoreCase(identifier.substring(6))) {
                    double lastPercent = stock.getLastPercent();
                    String percentFormatted = lastPercent + "";

                    if (lastPercent < 0) {
                        percentFormatted = ChatColor.RED + percentFormatted + "%";
                    } else {
                        percentFormatted = ChatColor.GREEN + percentFormatted + "%";
                    }

                    return ChatColor.GREEN + stock.getName() + ChatColor.GRAY + " \u00BB " + ChatColor.RED + stock.getPrice() + ChatColor.GRAY + " \u00BB " + percentFormatted;
                }
            }
        }
        if (identifier.equals("stock_list")) {
            return new PlayerStocks(player.getPlayer()).retrieveFormattedStock();
        }

        return null;
    }
}
