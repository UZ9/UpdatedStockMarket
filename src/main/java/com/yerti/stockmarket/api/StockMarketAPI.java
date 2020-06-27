package com.yerti.stockmarket.api;

import com.yerti.stockmarket.StockMarket;
import com.yerti.stockmarket.stocks.Stock;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.util.List;

public class StockMarketAPI {

    /**
     * Fetches all of the current stocks that are created
     *
     * @return list of all current stocks
     */
    public static List<Stock> retrieveStocks() {
        return StockMarket.getInstance().getStockManager().getStocks();
    }

    /**
     * Returns a formatted message of the stocks
     *
     * @return
     */
    public static String retrieveFormattedStock() {

        StringBuilder builder = new StringBuilder();

        builder.append(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------\n");

        DecimalFormat format = new DecimalFormat("#,###.#");

        for (Stock stock : retrieveStocks()) {


            double lastPercent = stock.getLastPercent();
            String percentFormatted = lastPercent + "";

            if (lastPercent < 0) {
                percentFormatted = ChatColor.RED + percentFormatted + "%";
            } else {
                percentFormatted = ChatColor.GREEN + percentFormatted + "%";
            }

            builder.append(ChatColor.GREEN + stock.getName() + ChatColor.GRAY + " - " + ChatColor.RED + "$" + stock.getPrice() + ChatColor.GRAY + " - " + percentFormatted + "\n");

        }

        builder.append(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------\n");

        return builder.toString();
    }


}
