package com.yerti.stockmarket.stocks;


import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Random;
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

	public Stock (String name) {
		this.stockID = name;
		this.mysql = StockMarket.getMySQL();
		
		exists = getInfo();
	}
	
	private boolean getInfo () {
		
		PreparedStatement stmt = mysql.prepareStatement("SELECT * FROM stocks WHERE stockID LIKE ? ");
		try {
			stmt.setString(1, stockID);
		} catch (SQLException e) {
			
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
	}
	
	public boolean add (String name, String stockID, long baseprice, long maxprice, long minprice, double volatility, int amount, double dividend, double lastPercent) {
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
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		mysql.execute(stmt);
		mysql.close();
		
		return true;
	}
	
	public boolean set (String name, String stockID, long baseprice, long maxprice, long minprice, double volatility, int amount, double dividend, double lastPercent) {

		Bukkit.getScheduler().runTaskAsynchronously(StockMarket.getInstance(), () -> {

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
		
		return true;
	}
	
	public boolean remove () {

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

		Bukkit.getScheduler().runTaskAsynchronously(StockMarket.getInstance(), () -> {

			PreparedStatement stmt;

			stmt = mysql.prepareStatement("UPDATE stocks SET lastPercent = ? WHERE StockID = ?");
			try {
				stmt.setDouble(1, lastPercent);
				stmt.setString(2, getID());
			} catch (SQLException e) {
				e.printStackTrace();
			}


			mysql.execute(stmt);
			mysql.close();
		});

		this.lastPercent = lastPercent;


		return true;
	}

	private void setAmount(double amount) {
		this.amount = (int) amount;
	}

	
	public boolean changePrice (long amount) {

		Bukkit.getScheduler().runTaskAsynchronously(StockMarket.getInstance(), () -> {

			DecimalFormat newFormat = new DecimalFormat("#.##");

			setAmount(Double.parseDouble(newFormat.format(amount)));

			PreparedStatement stmt;
			if (getPrice() + amount > getMaxPrice()) {
				stmt = mysql.prepareStatement("UPDATE stocks SET price = ? WHERE StockID = ?");
				try {
					stmt.setLong(1, getMaxPrice());
					stmt.setString(2, getID());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (getPrice() + amount < getMinPrice()) {
				stmt = mysql.prepareStatement("UPDATE stocks SET price = ? WHERE StockID = ?");
				try {
					stmt.setLong(1, getMinPrice());
					stmt.setString(2, getID());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				stmt = mysql.prepareStatement("UPDATE stocks SET price = price + ? WHERE StockID = ?");
				try {
					stmt.setLong(1, amount);
					stmt.setString(2, getID());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}


			mysql.execute(stmt);
		});


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
	
	public int getAmount () {
		return this.amount;
	}
	
	public double getDividend () {
		return this.dividend;
	}
	
}
