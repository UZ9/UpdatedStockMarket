package com.yerti.stockmarket.threads;


import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;
import com.yerti.stockmarket.menus.MenuListStock;
import com.yerti.stockmarket.stocks.PlayerStocks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StockMarketDividendThread extends Thread {

	private boolean loop = true;
	private int loopTimes = 0;
	private Plugin plugin;
	
	public StockMarketDividendThread (Plugin plugin){
		super ("StockMarketDividendThread");
		this.plugin = plugin;
		
		MySQL mysql = new MySQL();
		
		ResultSet result = mysql.query("SELECT looptime2 FROM looptime");
		
		try {
			while (result.next()) {
				loopTimes = result.getInt("looptime2");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		mysql.close();
	}
	
	public void run() {

		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}



			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getOpenInventory() != null) {
					if (player.getOpenInventory().getTopInventory() != null) {
						if (player.getOpenInventory().getTopInventory().getName().equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + "STOCK")) {
							player.getOpenInventory().getTopInventory().setContents(new MenuListStock(player).getInventory().getContents());
						}
					}
				}
			}
		}



		//Removed per request

		/*
		if (StockMarket.dividendFreq == 0)
			loop = false;
		while (loop) {
			// SLEEP
			try {
				Thread.sleep(plugin.getConfig().getInt("mysql-update-interval"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (loop) {
				loopTimes++;
	
				// DO SOME EVENT STUFF
				
				if (loopTimes % StockMarket.dividendFreq == 0) {
					broadcastMessage("Paying out all stock dividends");
					
					if (StockMarket.payOffline == true) {
						MySQL mysql = new MySQL();
						ResultSet result = mysql.query("SELECT name FROM players");
						
						try {
							while (result.next()) {
								String playerName = result.getString("name");
								Player p = Bukkit.getServer().getPlayer(playerName);
								PlayerStocks ps;
								if (p != null)
									 ps = new PlayerStocks(p);
								else
									ps = new PlayerStocks(playerName);
								ps.payoutDividends();
							}
						} catch (SQLException e) {
							
						}
					} else {
		                //loop through all of the online players and give them all a random item and amount of something, The diamond ore breaker will not get a reward.
		                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
		                	PlayerStocks ps = new PlayerStocks(player);
							ps.payoutDividends();
		                }
						
					}
				}
			}
		}
		*/
	}
	
	public void finish() {
		loop = false;
		
		MySQL mysql = new MySQL();
		
		try {
			mysql.execute("UPDATE looptime SET looptime2 = " + loopTimes);
		} catch (SQLException e) {
			
		}
		
		mysql.close();
	}
	

	
}
