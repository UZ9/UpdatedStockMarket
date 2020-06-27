package com.yerti.stockmarket.graphs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Stores the past 5 stock values for every stock to be used in graphs
 */
public class StockPriceStorage {

    private File playerFile;
    private FileConfiguration playerConfig;

    public StockPriceStorage(Plugin plugin) {
        playerFile = new File(plugin.getDataFolder() + "/paststockprices.yml");
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
    }

    public void savePlayersFile() {

    /*if (!playerFile.exists()) {
        playerFile.mkdir();
    }*/

        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {

    }

    public FileConfiguration getPlayerConfig() {
        return playerConfig;
    }


}
