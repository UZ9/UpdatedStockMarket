package com.yerti.stockmarket.api;

import com.yerti.stockmarket.stocks.PlayerStocks;
import com.yerti.stockmarket.stocks.Stock;

import java.util.List;

public class StockMarketAPI {

    /**
     * Fetches all of the current stocks that are created
     * @return list of all current stocks
     */
    public static List<Stock> retrieveStocks() {
        return new PlayerStocks("Console").retrieveStocks();
    }




}
