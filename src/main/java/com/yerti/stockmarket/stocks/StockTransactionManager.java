package com.yerti.stockmarket.stocks;

import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;
import com.yerti.stockmarket.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class StockTransactionManager {

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private List<StockTransaction> transactions;
    private Map<UUID, >

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

    public void buyStock(Player player, String id, int amount) {
        Message m = new Message(player);

        Stock stock = StockMarket.getInstance().getStockManager().getStock(id);

        if (stock == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Stock was found null during purchase.");
            return;
        }

        if (stock.exists()) {
            if ((stock.getAmount() >= amount) || stock.getAmount() == 1) {

                if (!StockMarket.economy.has(player, stock.getPrice() * amount)) {
                    m.errorMessage("Not enough money!");
                    return;
                }

                PlayerStocks playersStocks = new PlayerStocks(player);

                if (playersStocks.numTotal() + amount > StockMarket.maxPerPlayer) {
                    m.errorMessage("Buying that many would put you over the limit for total stocks!");
                    return;
                }

                if (playersStocks.numStock(stock) + amount > StockMarket.maxPerPlayerPerStock) {
                    m.errorMessage("Buying that many would put you over the limit for that stock!");
                    return;
                }

                this.stocks.get(stock.getID()).amount += amount;

                //Moved MySQL to async task (a lot faster now)
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    PreparedStatement stmt = mysql.prepareStatement("UPDATE players SET " + stock.getID() + " = ? WHERE name LIKE ?");
                    try {
                        stmt.setInt(1, this.stocks.get(stock.getID()).amount);
                        stmt.setString(2, player.getUniqueId().toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    mysql.execute(stmt);

                    if (stock.getAmount() != -1) {
                        stmt = mysql.prepareStatement("UPDATE stocks SET amount = amount - ? WHERE StockID LIKE ?");
                        try {
                            stmt.setInt(1, amount);
                            stmt.setString(2, stock.getID());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        mysql.execute(stmt);
                    }

                    mysql.close();
                });



                StockMarket.economy.withdrawPlayer(player, amount * stock.getPrice());
                m.successMessage("Successfully purchased " + amount + " " + stock + " stocks for " + stock.getPrice() + " " + StockMarket.economy.currencyNamePlural() + " each.");
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                return true;
            } else {
                m.errorMessage("There is not enough of that stock left to buy that many!");
                return false;
            }
        } else {
            m.errorMessage("Invalid stock ID");
            return false;
        }



    }


    private LocalDateTime parseDate(String text) {

        return LocalDateTime.parse(text, FORMATTER);
    }

    private String dateToString(LocalDateTime time) {
        return time.format(FORMATTER);
    }
}
