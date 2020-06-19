package com.yerti.stockmarket.threads;


import com.yerti.stockmarket.api.StockMarketAPI;
import com.yerti.stockmarket.events.EventInstance;
import com.yerti.stockmarket.MySQL;
import com.yerti.stockmarket.StockMarket;
import com.yerti.stockmarket.stocks.Stock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class StockMarketEventThread extends Thread {

	private boolean loop = true;
	private int loopTimes = 0;
	
	public StockMarketEventThread (){
		super ("StockMarketEventThread");
		
		MySQL mysql = new MySQL(StockMarket.getInstance());
		
		ResultSet result = mysql.query("SELECT looptime FROM looptime");
		
		try {
			while (result.next()) {
				loopTimes = result.getInt("looptime");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		mysql.close();
	}
	
	public void run() {
		if (StockMarket.randomEventFreq == 0)
			loop = false;
		while (loop) {
			// SLEEP
			try {
				Thread.sleep(60000); // THIS DELAY COULD BE CONFIG'D
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (loop) {
			
				loopTimes++;
	
				// DO SOME EVENT STUFF
				
				if (loopTimes % StockMarket.randomEventFreq == 0) {
					loopTimes = 0;
					Stock randomStock = StockMarketAPI.retrieveStocks().get(ThreadLocalRandom.current().nextInt(0, StockMarketAPI.retrieveStocks().size()));

					EventInstance ei = new EventInstance();
					ei.forceRandomEvent(randomStock);

				}
			}
		}
	}
	
	public void finish() {
		loop = false;
		
		MySQL mysql = new MySQL(StockMarket.getInstance());
		
		try {
			mysql.execute("UPDATE looptime SET looptime = " + loopTimes);
		} catch (SQLException e) {
			
		}
		
		mysql.close();
	}
	
	
}
