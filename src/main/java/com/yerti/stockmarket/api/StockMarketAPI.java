package com.yerti.stockmarket.api;

import com.yerti.stockmarket.stocks.PlayerStocks;
import com.yerti.stockmarket.stocks.Stock;

import java.util.List;
import java.util.UUID;

public class StockMarketAPI {

    /**
     * Fetches all of the current stocks that are created
     * @return list of all current stocks
     */
    public static List<Stock> retrieveStocks() {
        return new PlayerStocks(UUID.fromString("cacca09a-8bc0-4473-8e73-7f0be728637c")).retrieveStocks();
    }




}
