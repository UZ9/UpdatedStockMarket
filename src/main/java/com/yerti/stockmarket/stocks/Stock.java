package com.yerti.stockmarket.stocks;


import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

public class Stock {

    private String name;
    private String stockID;
    private long price;
    private long basePrice;
    private long maxPrice;
    private long minPrice;
    private double volatility;
    private double lastPercent;
    private int amount;
    private double dividend;
    private MySQL mysql;

    private boolean exists;


    public Stock(String name, String stockID, long price, long basePrice, long maxPrice, long minPrice, double volatility, double lastPercent, int amount) {
        this.name = name;
        this.stockID = stockID;
        this.price = price;
        this.basePrice = basePrice;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.volatility = volatility;
        this.lastPercent = lastPercent;
        this.amount = amount;
        this.mysql = StockMarket.getMySQL();
    }

    /*
    public Stock(String name) {
        this.stockID = name;
        this.mysql = StockMarket.getMySQL();

        exists = getInfo();
    }

    private boolean getInfo() {

        PreparedStatement stmt = mysql.prepareStatement("SELECT * FROM stocks WHERE stockID LIKE ? ");
        try {
            stmt.setString(1, stockID);
        } catch (SQLException e) {
			e.printStackTrace();
        }
        ResultSet result = mysql.query(stmt);

        try {
            while (result.next()) {
                // WE FOUND IT, STORE SOME INFO
                name = result.getString("name");
                price = result.getLong("price");
                basePrice = result.getLong("basePrice");
                maxPrice = result.getLong("maxPrice");
                minPrice = result.getLong("minPrice");
                volatility = result.getDouble("volatility");
                amount = result.getInt("amount");
                dividend = result.getDouble("dividend");
                lastPercent = result.getDouble("lastPercent");
                mysql.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mysql.close();

        return false;
    }*/

    public boolean add(int amount) {
        this.amount += amount;

        return true;
    }


    //keeping mysql because it'll only be called like once
    public boolean remove() {

        Bukkit.getScheduler().runTaskAsynchronously(StockMarket.getInstance(), () -> {

            try {
                mysql.execute("ALTER TABLE players DROP COLUMN " + stockID);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

            PreparedStatement stmt = mysql.prepareStatement("DELETE FROM stocks WHERE StockID LIKE ?");
            try {
                stmt.setString(1, stockID);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            mysql.execute(stmt);

            mysql.close();
        });

        return true;
    }

    public boolean setLastPercent(double lastPercent) {
        this.lastPercent = lastPercent;
        return true;
    }

    public boolean changePrice(long amount) {


        DecimalFormat newFormat = new DecimalFormat("#.##");

        //setAmount(Double.parseDouble(newFormat.format(amount)));


        price = Math.max(minPrice, Math.min(maxPrice, amount));


        return true;
    }

    public long updatePrice(boolean up, double scalar) {
        long d;
        double a = ThreadLocalRandom.current().nextDouble(0.01, 0.5);


        if (up) {
            //
            d = (long) (price * (1 + a) * volatility * scalar * 0.0001);
            //d = (long) (basePrice / price * a * (1. + (a * .25)) * volatility * scalar);
            //d = (long) ((getVolatility() / 100.) * (a * (scalar * .01) * Math.log(getBasePrice() + 1) / Math.log(1.01)));

        } else {
            d = -1 * (long) (price * (1 + a) * volatility * scalar * 0.0001);
            //d = (-1) * (long) ((getVolatility() / 100.) * (a * (scalar * .01) * Math.log(getBasePrice() + 1) / Math.log(1.001)));

        }


        return d;
    }

    public boolean set(String name, String stockID, long baseprice, long maxprice, long minprice, double volatility, int amount, double dividend, double lastPercent) {

        Bukkit.getScheduler().runTaskAsynchronously(StockMarket.getInstance(), () -> {
            MySQL mysql = new MySQL(StockMarket.getInstance());

            PreparedStatement stmt = mysql.prepareStatement("UPDATE stocks SET name = ?, basePrice = ?, maxPrice = ?, minPrice = ?, volatility = ?, amount = ?, lastPercent = ?, dividend = ? WHERE StockID LIKE ?");
            try {
                stmt.setString(1, name);
                stmt.setLong(2, baseprice);
                stmt.setLong(3, maxprice);
                stmt.setLong(4, minprice);
                stmt.setDouble(5, volatility);
                stmt.setInt(6, amount);
                stmt.setDouble(7, lastPercent);
                stmt.setDouble(8, dividend);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            mysql.execute(stmt);
            mysql.close();
        });

        this.name = name;
        this.basePrice = baseprice;
        this.maxPrice = maxprice;
        this.minPrice = minprice;
        this.volatility = volatility;
        this.amount = amount;
        this.lastPercent = lastPercent;
        this.dividend = dividend;

        return true;
    }


    public double getLastPercent() {
        return lastPercent;
    }

    public boolean exists() {
        return this.exists;
    }

    public long getMinPrice() {
        return this.minPrice;
    }

    public long getMaxPrice() {
        return this.maxPrice;
    }

    public long getBasePrice() {
        return this.basePrice;
    }

    public long getPrice() {
        return this.price;
    }

    public double getVolatility() {
        return this.volatility;
    }

    public String getID() {
        return this.stockID.toUpperCase();
    }

    public String getName() {
        return this.name;
    }

    public String toID() {
        return this.stockID.toUpperCase();
    }

    public String toString() {
        return this.name;
    }

    public int getAmount() {
        return this.amount;
    }

    private void setAmount(double amount) {
        this.amount = (int) amount;
    } //why is this double?

    public void addAmount(int amount) {
        this.amount += amount;
    }

    public double getDividend() {
        return this.dividend;
    }


}
