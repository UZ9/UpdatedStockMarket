package com.yerti.stockmarket;


import com.yerti.stockmarket.api.StockMarketAPI;
import com.yerti.stockmarket.events.EventInstance;
import com.yerti.stockmarket.menus.MenuListStock;
import com.yerti.stockmarket.messages.Message;
import com.yerti.stockmarket.stocks.Stock;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class StockMarketCommandExecutor implements CommandExecutor {

    private StockMarket plugin;

    public StockMarketCommandExecutor(StockMarket plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;

        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            return false;
        }

        Message m = new Message(player);

        if (command.getName().equalsIgnoreCase("sm") || command.getName().equalsIgnoreCase("stock")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("togglechat")) {
                if (plugin.toggledUsers.contains(player.getUniqueId())) {
                    plugin.toggledUsers.remove(player.getUniqueId());
                    new Message(player).successMessage("Turned on stock event messages.");
                } else {
                    plugin.toggledUsers.add(player.getUniqueId());
                    new Message(player).successMessage("Turned off stock event messages.");
                }


                return true;
            } else if (args.length >= 1 && args[0].equalsIgnoreCase("help") && StockMarket.permission.has(player, "stockmarket.user.help")) {
                int page = 1;

                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {

                        m.errorMessage("Invalid Syntax. /sm help for help.");
                        return true;
                    }
                }

                m.displayHelp(page);
            } else if (args.length == 1 && args[0].equalsIgnoreCase("info") && StockMarket.permission.has(player, "stockmarket.user.info")) {
                m.displayInfo();
            } else if (args.length >= 1 && args[0].equalsIgnoreCase("list") && StockMarket.permission.has(player, "stockmarket.user.list")) {
                // LIST ALL THE STOCKS THIS PLAYER CAN BUY
                //PlayerStocks ps = new PlayerStocks(player);
                //ps.listAll();
                player.openInventory(new MenuListStock(player).getInventory());
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("buy") && StockMarket.permission.has(player, "stockmarket.user.buy")) {
                Stock stock = StockMarket.getInstance().getStockManager().getStock(args[1]);
                int amount = 1;

                if (args.length == 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        m.errorMessage("Invalid Syntax");
                        return true;
                    }
                }

                if (amount > 0) {
                    StockMarket.getInstance().getStockManager().buy(player, stock, amount);
                } else {
                    m.errorMessage("Invalid amount.");
                }
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("sell") && StockMarket.permission.has(player, "stockmarket.user.sell")) {
                Stock stock = StockMarket.getInstance().getStockManager().getStock(args[1]);
                int amount = 1;

                if (args.length == 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        m.errorMessage("Invalid Syntax");
                        return true;
                    }
                }

                if (amount > 0) {
                    StockMarket.getInstance().getStockManager().sell(player, stock, amount);
                } else {
                    m.errorMessage("Invalid amount.");
                }
            } else if (args.length >= 9 && args[0].equalsIgnoreCase("add") && StockMarket.permission.has(player, "stockmarket.admin.add")) {
                final String stockID = args[1];
                final long baseprice;
                final long minprice;
                final long maxprice;
                final double volatility;
                final int amount;
                final double dividend;

                try {
                    baseprice = Long.parseLong(args[2]);
                    maxprice = Long.parseLong(args[3]);
                    minprice = Long.parseLong(args[4]);
                    volatility = Double.parseDouble(args[5]);
                    amount = Integer.parseInt(args[6]);
                    dividend = Double.parseDouble(args[7]);
                } catch (NumberFormatException e) {
                    m.errorMessage("Invalid syntax.");
                    return true;
                }

                if (amount < -1) {
                    m.errorMessage("Invalid amount.");
                    return true;
                }

                String name = args[8];
                for (int i = 9; i < args.length; i++) {
                    name += " ";
                    name += args[i];
                }

                Stock stock = StockMarket.getInstance().getStockManager().getStock(stockID);

                if (stock == null) {
                    if (StockMarket.getInstance().getStockManager().addStock(name, stockID, baseprice, maxprice, minprice, volatility, amount, dividend, 0))
                        m.successMessage("Successfully created new stock.");
                    else
                        m.errorMessage("Failed to create new stock.  Make sure the ID was valid.");
                } else {
                    m.errorMessage("A stock with that ID already exists!");
                    return true;
                }

            } else if (args.length == 2 && args[0].equalsIgnoreCase("remove") && StockMarket.permission.has(player, "stockmarket.admin.remove")) {
                String stockID = args[1];

                Stock stock = StockMarket.getInstance().getStockManager().getStock(args[1]);

                if (stock.exists()) {
                    stock.remove();

                    m.successMessage("Successfully removed that stock.");
                } else {
                    m.errorMessage("That stock does not exist.");
                    return true;
                }
            } else if (args.length >= 8 && args[0].equalsIgnoreCase("set") && StockMarket.permission.has(player, "stockmarket.admin.set")) {
                final String stockID = args[1];
                final long baseprice;
                final long minprice;
                final long maxprice;
                final double volatility;
                final int amount;
                final double dividend;
                try {
                    baseprice = Long.parseLong(args[2]);
                    maxprice = Long.parseLong(args[3]);
                    minprice = Long.parseLong(args[5]);
                    volatility = Double.parseDouble(args[4]);
                    amount = Integer.parseInt(args[6]);
                    dividend = Double.parseDouble(args[7]);
                } catch (NumberFormatException e) {
                    m.errorMessage("Invalid syntax.");
                    return true;
                }

                if (amount < -1) {
                    m.errorMessage("Invalid amount.");
                    return true;
                }

                String name = args[8];
                for (int i = 9; i < args.length; i++) {
                    name += " ";
                    name += args[i];
                }

                Stock stock = StockMarket.getInstance().getStockManager().getStock(args[1]);

                if (stock.set(name, stockID, baseprice, maxprice, minprice, volatility, amount, dividend, 0))
                    m.successMessage("Successfully adjusted stock.");
                else
                    m.errorMessage("Failed to adjust stock.  Make sure the ID was valid.");

            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload") && StockMarket.permission.has(player, "stockmarket.admin.reload")) {
                m.regularMessage("Saving config..");
                plugin.reloadConfig();
                plugin.loadConfiguration();
                m.regularMessage("Saving stocks..");
                for (Stock stock : StockMarketAPI.retrieveStocks()) {
                    Bukkit.getLogger().log(Level.INFO, "Found " + stock.getName());
                    if (stock.getID().equalsIgnoreCase("BigBank")) {
                        Bukkit.getLogger().log(Level.INFO, "Found " + stock.getName() + " at -1, setting to 1..");
                        MySQL mysql = new MySQL(plugin);
                        PreparedStatement stmt;
                        stmt = mysql.prepareStatement("UPDATE stocks SET amount = ? WHERE StockID LIKE ?");
                        try {
                            stmt.setInt(1, 1);
                            stmt.setString(2, stock.getID());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        mysql.execute(stmt);

                        mysql.close();
                    }
                    //Force call java plugin saving
                    JavaPlugin.getProvidingPlugin(StockMarket.class).onDisable();
                    JavaPlugin.getProvidingPlugin(StockMarket.class).onEnable();
                }
                m.regularMessage("Done!");
                m.successMessage("Successfully reloaded StockMarket.");
            } else if (args.length == 1 && args[0].equalsIgnoreCase("forcerandom") && StockMarket.permission.has(player, "stockmarket.admin.event")) {

                EventInstance ei = new EventInstance();
                ei.forceRandomEvent(StockMarketAPI.retrieveStocks().get(ThreadLocalRandom.current().nextInt(StockMarketAPI.retrieveStocks().size())));

            } else if (args.length == 1 && StockMarket.permission.has(player, "stockmarket.user.detail")) {
                // CHECK IF THIS IS A STOCK NAME
                String stockID = args[0];

                Stock stock = StockMarket.getInstance().getStockManager().getStock(args[1]);

                if (stock.exists()) {
                    m.successMessage(stock.toString());
                    m.regularMessage("Current Price: " + stock.getPrice());

                    // BASE SHOULD ONLY DISPLAY FOR A SPECIAL PERMISSION NODE
                    if (StockMarket.permission.has(player, "stockmarket.admin.baseprice"))
                        m.regularMessage("Base Price: " + stock.getBasePrice());
                    m.regularMessage("Max Price: " + stock.getMaxPrice());
                    m.regularMessage("Min Price: " + stock.getMinPrice());
                    m.regularMessage("Volatility: " + stock.getVolatility());
                    m.regularMessage("Dividend: " + stock.getDividend() + "% per stock.");
                    if (stock.getAmount() != -1)
                        m.regularMessage("Current Amount: " + stock.getAmount());
                    else
                        m.regularMessage("Current Amount: Infinite");
                } else {
                    m.unknownCommand();
                    return true;
                }
            } else if (args.length > 0) {
                m.unknownCommand();
            } else {
                player.openInventory(new MenuListStock(player).getInventory());
            }
        }


        return true;
    }

}
