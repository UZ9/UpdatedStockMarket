package com.yerti.stockmarket.stocks;

import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;
import com.yerti.stockmarket.messages.Message;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StockManager {

    private Map<UUID, Map<Stock, Integer>> storage = new HashMap<>();

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

            result.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadPlayer(Player player) {
        MySQL sql = StockMarket.getMySQL();

        Map<Stock, Integer> stockData = new HashMap<>();


        try {
            PreparedStatement stmt = sql.prepareStatement("SELECT * from players where name = ?");
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet set = sql.query(stmt);

            if (set.next()) {
                for (Stock stock : stocks) {
                    for (int i = 1; i <= set.getMetaData().getColumnCount(); i++) {
                        if (set.getMetaData().getColumnName(i).equalsIgnoreCase(stock.getID())) {
                            stockData.put(stock, set.getInt(i));
                        }
                    }
                }
            }

            set.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //couldn't find player
        if (stockData.size() == 0) {
            for (Stock stock : stocks) {
                stockData.put(stock, 0);
            }
        }

        System.out.println("Successfully loaded stock data for player " + player.getName());
        storage.put(player.getUniqueId(), stockData);
    }

    public void saveStocks() throws SQLException {
        MySQL sql = StockMarket.getMySQL();
        //id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name tinytext, stockID tinytext, quantity int, time tinytext


        //save stocks
        for (Stock stock : stocks) {
            PreparedStatement stmt = sql.prepareStatement("UPDATE stocks SET name = ?, stockID = ?, price = ?, basePrice = ?, maxPrice = ?, minPrice = ?, volatility = ?, amount = ?, lastPercent = ?, dividend = ? WHERE name = ?");

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
            stmt.setString(11, stock.getName());

            sql.execute(stmt);
        }

        //save player stuff
        for (Map.Entry<UUID, Map<Stock, Integer>> player : storage.entrySet()) { //loop through all stock values
            for (Map.Entry<Stock, Integer> stockValue : player.getValue().entrySet()) { //loop through each stock value
                //updates players to set <stockid> to its appropriate value where the name is the same as the uuid
                PreparedStatement check = sql.prepareStatement("SELECT name FROM players WHERE name like ?");
                check.setString(1, player.getKey().toString());
                ResultSet resultSet = sql.query(check);
                if (!resultSet.next()) {
                    PreparedStatement stmt = sql.prepareStatement("INSERT INTO players (name) VALUES (?)");
                    stmt.setString(1, player.getKey().toString());
                    sql.execute(stmt);
                }

                PreparedStatement stmt = sql.prepareStatement("UPDATE players SET " + stockValue.getKey().getID() + " = ? WHERE name like ?");


                stmt.setInt(1, stockValue.getValue());
                stmt.setString(2, player.getKey().toString());

                sql.execute(stmt);

                check.close();
                resultSet.close();
            }


        }


    }

    public boolean addStock(String name, String stockID, long baseprice, long maxprice, long minprice, double volatility, int amount, double dividend, double lastPercent) {
        MySQL mysql = new MySQL(StockMarket.getInstance());
        try {
            mysql.execute("ALTER TABLE players ADD COLUMN " + stockID + " INT DEFAULT 0");
        } catch (SQLException e) {
            return false;
        }

        PreparedStatement stmt = mysql.prepareStatement("INSERT INTO stocks (name, stockID, price, basePrice, maxPrice, minPrice, volatility, amount, lastPercent, dividend) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        try {
            stmt.setString(1, name);
            stmt.setString(2, stockID);
            stmt.setLong(3, baseprice);
            stmt.setLong(4, baseprice);
            stmt.setLong(5, maxprice);
            stmt.setLong(6, minprice);
            stmt.setDouble(7, volatility);
            stmt.setInt(8, amount);
            stmt.setDouble(9, lastPercent);
            stmt.setDouble(10, dividend);

            System.out.println("Statement will be " + stmt.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        mysql.execute(stmt);
        mysql.close();

        return true;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public Stock getStock(String name) {
        return stocks.stream().filter(s -> s.getID().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean sell(Player player, Stock stock, int amount) {
        Message m = new Message(player);

        int currentStockAmount = storage.get(player.getUniqueId()).getOrDefault(stock, 0);

        if (stock != null) {

            if (currentStockAmount - amount < 0) {
                m.errorMessage("Failed to sell!  Check that you have that many!");
                return false;
            }

            storage.get(player.getUniqueId()).put(stock, currentStockAmount - amount);
            stock.addAmount(amount);

            StockMarket.economy.depositPlayer(player, amount * stock.getPrice());
            m.successMessage("Successfully sold " + amount + " " + stock + " stocks for " + stock.getPrice() + " " + StockMarket.economy.currencyNamePlural() + " each.");
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
            return true;
        } else {
            m.errorMessage("Invalid stock ID");
            return false;
        }
    }

    public boolean buy(Player player, Stock stock, int amount) {
        Message m = new Message(player);

        int currentStockAmount = storage.get(player.getUniqueId()).getOrDefault(stock, 0);

        if (stock != null) {
            if ((stock.getAmount() >= amount) || stock.getAmount() == 1) {

                if (!StockMarket.economy.has(player, stock.getPrice() * amount)) {
                    m.errorMessage("Not enough money!");
                    return false;
                }

                if (totalStockAmount(player) + amount > StockMarket.maxPerPlayer) {
                    m.errorMessage("Buying that many would put you over the limit for total stocks!");
                    return false;
                }

                if (currentStockAmount + amount > StockMarket.maxPerPlayerPerStock) {
                    m.errorMessage("Buying that many would put you over the limit for that stock!");
                    return false;
                }

                storage.get(player.getUniqueId()).put(stock, currentStockAmount + amount);
                stock.addAmount(-amount);

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

    private int totalStockAmount(Player player) {
        int sum = 0;

        for (Map.Entry<Stock, Integer> entry : storage.get(player.getUniqueId()).entrySet()) {
            sum += entry.getValue();
        }

        return sum;
    }

    public int getStockAmount(Player player, Stock stock) {
        return storage.get(player.getUniqueId()).get(stock);
    }


}
