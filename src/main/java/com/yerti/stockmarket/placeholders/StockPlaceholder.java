package com.yerti.stockmarket.placeholders;


import com.yerti.stockmarket.StockMarket;
import com.yerti.stockmarket.api.StockMarketAPI;
import com.yerti.stockmarket.stocks.Stock;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
            for (Stock stock : StockMarket.getInstance().getStockManager().getStocks()) {
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
            return StockMarketAPI.retrieveFormattedStock();
        }

        return null;
    }
}
