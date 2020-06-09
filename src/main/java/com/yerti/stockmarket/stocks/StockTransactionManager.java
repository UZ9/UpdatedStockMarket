package com.yerti.stockmarket.stocks;

import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StockTransactionManager {

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private List<StockTransaction> transactions;

    public StockTransactionManager() {
        this.transactions = new ArrayList<>();

        loadStockTransactions();
    }

    public void loadStockTransactions() {
        MySQL sql = StockMarket.getMySQL();

        PreparedStatement stmt = sql.prepareStatement("SELECT * FROM transactions");

        ResultSet result = sql.query(stmt);

        try {
            while (result.next()) {
                transactions.add(new StockTransaction(
                        UUID.fromString(result.getString(2)),
                        result.getString(3),
                        result.getInt(4),
                        parseDate(result.getString(5))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveStockTransactions() throws SQLException {
        MySQL sql = StockMarket.getMySQL();
        //id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext, stockID tinytext, quantity int, time tinytext


        for (StockTransaction transaction : transactions) {
            PreparedStatement stmt = sql.prepareStatement("INSERT OR REPLACE INTO transactions (name, stockID, quantity, time) VALUES (?, ?, ?, ?)");

            stmt.setString(1, transaction.getStockName());
            stmt.setString(2, transaction.getUuid().toString());
            stmt.setInt(3, transaction.getQuantity());
            stmt.setString(4, dateToString(transaction.getTime()));

            sql.execute(stmt);
        }
    }


    private LocalDateTime parseDate(String text) {

        return LocalDateTime.parse(text, FORMATTER);
    }

    private String dateToString(LocalDateTime time) {
        return time.format(FORMATTER);
    }
}
