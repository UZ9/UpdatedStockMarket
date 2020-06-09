package com.yerti.stockmarket.stocks;

import java.time.LocalDateTime;
import java.util.UUID;

public class StockTransaction {

    private UUID uuid;
    private String stockName;
    private int quantity;
    private LocalDateTime time;

    public StockTransaction(UUID uuid, String stockName, int quantity) {
        this.uuid = uuid;
        this.stockName = stockName;
        this.quantity = quantity;
        this.time = LocalDateTime.now();
    }

    public StockTransaction(UUID uuid, String stockName, int quantity, LocalDateTime time) {
        this.uuid = uuid;
        this.stockName = stockName;
        this.quantity = quantity;
        this.time = time;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getStockName() {
        return stockName;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
