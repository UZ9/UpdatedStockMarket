package com.yerti.stockmarket.stocks;

import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StockManager {

    private List<Stock> stocks;

    public StockManager() {
        this.stocks = new ArrayList<>();

        loadStocks();
    }

    public void loadStocks() {
        MySQL sql = StockMarket.getMySQL();

        //"CREATE TABLE IF NOT EXISTS stocks (id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext,
        // stockID tinytext, price decimal(64, 2), basePrice decimal(64, 2), maxPrice decimal(64, 2), minPrice decimal(64, 2),
        // volatility decimal(64, 2), amount int, lastPercent decimal(64, 2), dividend decimal(10, 2))");

        PreparedStatement stmt = sql.prepareStatement("SELECT * FROM stocks");

        ResultSet result = sql.query(stmt);

        try {
            while (result.next()) {
                stocks.add(new Stock(result.getString("name"), result.getString("stockID"),
                        result.getLong("price"),
                        result.getLong("basePrice"),
                        result.getLong("maxPrice"),
                        result.getLong("minPrice"),
                        result.getDouble("volatility"), result.getDouble("lastPercent"),
                        result.getInt("amount")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveStocks() throws SQLException {
        MySQL sql = StockMarket.getMySQL();
        //id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext, stockID tinytext, quantity int, time tinytext


        for (Stock stock : stocks) {
            PreparedStatement stmt = sql.prepareStatement("INSERT OR REPLACE INTO stocks (name, stockID, price, basePrice, maxPrice, minPrice, volatility, amount, lastPercent, dividend) VALUES (?, ?, ?, ?)");

            stmt.setString(1, stock.getName());
            stmt.setString(2, stock.getID());
            stmt.setDouble(3, stock.getPrice());
            stmt.setDouble(4, stock.getBasePrice());
            stmt.setDouble(5, stock.getMaxPrice());
            stmt.setDouble(6, stock.getMinPrice());
            stmt.setDouble(7, stock.getVolatility());
            stmt.setDouble(8, stock.getAmount());
            stmt.setDouble(9, stock.getLastPercent());
            stmt.setDouble(10, stock.getDividend());

            sql.execute(stmt);
        }
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public Stock getStock(String name) {
        return stocks.stream().filter(s -> s.getID().equalsIgnoreCase(name)).findFirst().orElse(null);
    }


}
