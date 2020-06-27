package com.yerti.stockmarket;

import com.yerti.stockmarket.core.inventories.InventoryHandler;
import com.yerti.stockmarket.events.Event;
import com.yerti.stockmarket.menus.MenuListStock;
import com.yerti.stockmarket.messages.Command;
import com.yerti.stockmarket.placeholders.StockPlaceholder;
import com.yerti.stockmarket.stocks.StockManager;
import com.yerti.stockmarket.threads.StockMarketEventThread;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StockMarket extends JavaPlugin {

    public static Vector<Command> commands = new Vector<>();
    public static Vector<Event> events = new Vector<>();
    public static Permission permission = null;
    public static Economy economy = null;
    public static int randomEventFreq = 60;
    public static int maxPerPlayer = 250;
    public static int maxPerPlayerPerStock = 50;
    public static boolean broadcastEvents = true;
    public static boolean debugMode = false;
    private static StockMarket instance;
    private static MySQL mySQL;
    public List<UUID> toggledUsers;
    private StockManager stockManager;
    private Logger log = Logger.getLogger("StockMarket");
    private StockMarketEventThread e;

    public static StockMarket getInstance() {
        return instance;
    }

    public static MySQL getMySQL() {
        return mySQL;
    }

    public void onDisable() {

        if (toggledUsers != null)
            getConfig().set("toggled-users", toggledUsers);

        try {
            e.finish();
        } catch (NullPointerException e) {
            log.info("[StockMarket] No cleanup required as event threads never started.");
        }

        try {
            stockManager.saveStocks();
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, "Failed to save stock data");
            ex.printStackTrace();
        }
    }

    private void disablePlugin() {
        Bukkit.getServer().getPluginManager().disablePlugin(this);
    }

    public void onEnable() {
        instance = this;

        checkAPI();
        setupVault();

        toggledUsers = new ArrayList<>();
        List<String> toggles = getConfig().getStringList("toggled-users");
        toggles.forEach(e -> toggledUsers.add(UUID.fromString(e)));

        Bukkit.getPluginManager().registerEvents(new InventoryHandler(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDataLoader(), this);

        initCommands();

        loadConfiguration();

        if (verifyDatabase()) {
            log.info("[StockMarket] Database initialized.");
            log.info("[StockMarket] Successfully loaded.");
        } else {
            log.severe("[StockMarket] Database failed to initialise. Check to make sure you have the correct MySQL information in the config.yml!");
            this.disablePlugin();
            return;
        }

        new MenuListStock(this);

        this.stockManager = new StockManager();

        e = new StockMarketEventThread();
        e.start();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                stockManager.saveStocks();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }, 20L, 20L * 60L * 5L); // 5 minutes

        //new StockGraph(null).generateGraph();

    }

    void loadConfiguration() {
        getConfig().options().copyDefaults(true);
        saveConfig();


        randomEventFreq = getConfig().getInt("random-event-frequency");
        maxPerPlayer = getConfig().getInt("max-total-stocks-per-player");
        maxPerPlayerPerStock = getConfig().getInt("max-total-stocks-per-player-per-stock");

        debugMode = getConfig().getBoolean("debug-mode");

        broadcastEvents = getConfig().getBoolean("broadcast-events");

        // LOAD EVENTS
        events.clear();
        int i = 0;
        while (getConfig().getString("events." + i + ".message") != null) {
            events.add(new Event(getConfig().getString("events." + i + ".message"), getConfig().getInt("events." + i + ".effect"), getConfig().getBoolean("events." + i + ".up"), getConfig().getInt("events." + i + ".frequency")));
            i++;
        }
    }

    private void initCommands() {
        StockMarketCommandExecutor myExecutor = new StockMarketCommandExecutor(this);

        getCommand("sm").setExecutor(myExecutor);
        getCommand("stock").setExecutor(myExecutor);

        commands.add(new Command("help", "Displays StockMarket help.", "<page>", "stockMarket.user.help"));
        commands.add(new Command("info", "Displays plugin version & status.", "", "stockMarket.user.info"));
        commands.add(new Command("list", "Displays a list of stocks you are allowed to buy and their current price.", "", "stockMarket.user.list"));
        commands.add(new Command("reload", "Reloads the StockMarket config.", "", "stockMarket.admin.reload"));
        commands.add(new Command("buy", "Buys the stock & amount specified.", "<stockID> <amount>", "stockMarket.user.buy"));
        commands.add(new Command("sell", "Sells the stock & amount specified.", "<stockID> <amount>", "stockMarket.user.sell"));
        commands.add(new Command("add", "Adds a new stock to the list of all stocks.", "<stockID> <basePrice> <maxPrice> <minPrice> <volatility> <amount> <dividend> <stockName>", "stockMarket.admin.add"));
        commands.add(new Command("remove", "Removes an existing stock from the list of all stocks.  Cannot be undone.", "<stockID>", "stockMarket.admin.remove"));
        commands.add(new Command("forcerandom", "Forces a random event to occur on a random stock.", "", "stockMarket.admin.event"));
        commands.add(new Command("stock", "Displays more info about stock requested.", "<stockID>", "stockMarket.user.detail"));
        commands.add(new Command("togglechat", "Toggles whether special stock events are sent to the player.", "", ""));

    }

    private Boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private Boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    private Boolean verifyDatabase() {
        mySQL = new MySQL(this);

        return MySQL.dbstatus;
    }

    private void setupVault() {
        if (setupEconomy()) {
            log.info("[StockMarket] Economy plugin detected and hooked into.");
        } else {
            log.severe("[StockMarket] Economy plugin not detected!");
            this.disablePlugin();
            return;
        }
        if (setupPermissions()) {
            log.info("[StockMarket] Permissions plugin detected and hooked into.");
        } else {
            log.severe("[StockMarket] Permissions plugin not detected!");
            this.disablePlugin();
        }
    }

    private void checkAPI() {
        //Check if BanditMaps shell is online
        //Temporarily disabled
        /*if (Bukkit.getPluginManager().isPluginEnabled("BanditMaps")) {
            StockPriceStorage storage = new StockPriceStorage(this);
            storage.savePlayersFile();
            new StockPriceStorageUpdater(this, storage.getPlayerConfig(), storage);
            new StockGraph(storage, this).startChartUpdate();
            log.info("[StockMarket] Successfully loaded BanditMaps API");
        } else {
            log.info("[StockMarket] Graphs disabled, BanditMaps API not found.");
        }*/

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            log.info("[StockMarket] Found PlaceholderAPI, adding placeholder.");
            new StockPlaceholder(this).register();
        } else {
            log.info("[StockMarket] PlaceholderAPI not found, disabling placeholder.");
        }
    }

    public StockManager getStockManager() {
        return stockManager;
    }
}
