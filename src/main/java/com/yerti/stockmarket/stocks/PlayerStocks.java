package com.yerti.stockmarket.stocks;


import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;
import com.yerti.stockmarket.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerStocks {

	private Player player;
	private HashMap<String, PlayerStock> stocks = new HashMap<String, PlayerStock>();
	private boolean exists;
	private UUID playerID;
	private static Plugin plugin;

	public PlayerStocks (Plugin plugin) {
		PlayerStocks.plugin = plugin;
	}

	public PlayerStocks (Player player) {
		this.player = player;
		if (player != null)
			this.playerID = player.getUniqueId();
		else
			this.playerID = UUID.fromString("");
		
		exists = getPlayerInfo();
	}
	
	public PlayerStocks (UUID playerName) {
		this.player = null;
		this.playerID = playerName;
		
		exists = getPlayerInfo();
	}
	
	private boolean getPlayerInfo() {
			// FIND THIS PLAYER IN THE DB, FILL IN HIS INFO
			MySQL mysql = new MySQL();

			// NOW LETS FIND EM
			PreparedStatement stmt = mysql.prepareStatement("SELECT * FROM players WHERE name LIKE ? ");
			try {
				stmt.setString(1, playerID.toString());
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			ResultSet result = mysql.query(stmt);



			try {
				while (result.next()) {

					// WE FOUND IT, STORE SOME INFO
					stmt = mysql.prepareStatement("SELECT stockID FROM stocks");
					ResultSet result2 = mysql.query(stmt);
					while (result2.next()) {
						PlayerStock newS = new PlayerStock();

						newS.stock = new Stock(result2.getString("stockID"));
						newS.amount = result.getInt(newS.stock.toID());

						this.stocks.put(newS.stock.getID().toUpperCase(), newS);
					}

					mysql.close();
					return true;
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			// WE DIDNT FIND IT, LETS CREATE IT



			stmt = mysql.prepareStatement("INSERT INTO players (name) Values(?)");
			try {
				stmt.setString(1, playerID.toString());
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			mysql.execute(stmt);


			mysql.close();



		return false;

		}
	public boolean exists() {
		return this.exists;
	}
	
	public boolean sell (Stock stock, int amount) {
		Message m = new Message(player);
		
		if (stock.exists()) {
				// CHECK THE PLAYER HAS ENOUGH TO SELL
				if (this.stocks.get(stock.getID()).amount - amount < 0) {
					m.errorMessage("Failed to sell!  Check that you have that many!");
					return false;
				}
				
				// OKAY THEY DO, LETS SELL EM
				this.stocks.get(stock.getID()).amount -= amount;

				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					MySQL mysql = new MySQL();
					PreparedStatement stmt = mysql.prepareStatement("UPDATE players SET " + stock.getID() + " = ? WHERE name LIKE ?");
					try {
						stmt.setInt(1, this.stocks.get(stock.getID()).amount);
						stmt.setString(2, player.getUniqueId().toString());
					} catch (SQLException e) {
						e.printStackTrace();
					}

					mysql.execute(stmt);

					// UPDATE AMOUNT IF NOT INFINITE

					stmt = mysql.prepareStatement("UPDATE stocks SET amount = amount + ? WHERE StockID LIKE ?");
					try {
						stmt.setInt(1, amount);
						stmt.setString(2, stock.getID());
					} catch (SQLException e) {
						e.printStackTrace();
					}

					mysql.execute(stmt);



					mysql.close();

				});

				StockMarket.economy.depositPlayer(player, amount * stock.getPrice());
				m.successMessage("Successfully sold " + amount + " " + stock + " stocks for " + stock.getPrice() + " " + StockMarket.economy.currencyNamePlural() + " each.");
				player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
				return true;
		} else {
			m.errorMessage("Invalid stock ID");
			return false;
		}
	}
	
	public boolean buy (Stock stock, int amount) {
		Message m = new Message(player);

		if (stock == null) {
			Bukkit.getLogger().log(Level.SEVERE, "Stock was found null during purchase.");
			return false;
		}

		if (stock.exists()) {
			if ((stock.getAmount() >= amount) || stock.getAmount() == 1 || stock.getAmount() == 0) {

				if (!StockMarket.economy.has(player, stock.getPrice() * amount)) {
					m.errorMessage("Not enough money!");
					return false;
				}

				if (numTotal() + amount > StockMarket.maxPerPlayer) {
					m.errorMessage("Buying that many would put you over the limit for total stocks!");
					return false;
				}

				if (numStock(stock) + amount > StockMarket.maxPerPlayerPerStock) {
					m.errorMessage("Buying that many would put you over the limit for that stock!");
					return false;
				}

				this.stocks.get(stock.getID()).amount += amount;

				//Moved MySQL to async task (a lot faster now)
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					MySQL mysql = new MySQL();
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


	public List<Stock> retrieveStocks() {
		List<Stock> stockList = new ArrayList<>();

		for (PlayerStock ps : stocks.values())
			stockList.add(ps.stock);
		return stockList;
	}

	
	public boolean payoutDividends () {
		boolean success = false;
		for (PlayerStock ps : stocks.values()) {
			StockMarket.economy.depositPlayer(player, ps.amount * ps.stock.getDividend() * .01 * ps.stock.getPrice());
			success = true;
		}
		return success;
	}
	
	private int numTotal () {
		int total = 0;
		for (PlayerStock ps : stocks.values()) {
			total += ps.amount;
		}
		return total;
	}

	public int numStock (Stock s) {
		return stocks.get(s.getID()).amount;
	}
	
	private boolean hasStocks () {
		for (PlayerStock ps : stocks.values())
			if (ps.amount > 0)
				return true;
		
		return false;
	}

	public String retrieveFormattedStock() {

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

			builder.append(ChatColor.GREEN + stock.getName() + ChatColor.GRAY + " - " + ChatColor.RED + "$"  + stock.getPrice() + ChatColor.GRAY + " - " + percentFormatted + "\n");

		}

		builder.append(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------\n");

		return builder.toString();
	}
	
}
