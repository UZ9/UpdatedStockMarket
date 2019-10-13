package com.yerti.stockmarket.graphs;

import com.yerti.stockmarket.api.StockMarketAPI;
import com.yerti.stockmarket.stocks.Stock;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StockGraph {

    private final boolean debug = true;

    private StockPriceStorage storage;
    private Plugin plugin;

    public StockGraph(StockPriceStorage storage, Plugin plugin) {
        this.storage = storage;
        this.plugin = plugin;
    }

    public void generateGraph() {
        List<Stock> stockData = StockMarketAPI.retrieveStocks();
        Map<Stock, List<Double>> stockPrices = new HashMap<>();

        for (Stock stock : stockData) {
            List<Double> tempList = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                tempList.add(storage.getPlayerConfig().getDouble(stock.getID() + "." + i));
            }

            stockPrices.put(stock, tempList);
        }

        String name = "Stock Prices";

        for (Stock stock : stockData) {
            //Create XYChart for each stock
            XYChart chart = new XYChart(500, 400);
            chart.setTitle(stock.getName());

            //Add previous stock data
            XYSeries series = chart.addSeries(stock.getName(), null, stockPrices.get(stock));
            series.setMarker(SeriesMarkers.CIRCLE);

            //Save chart (png)
            saveChart(chart);
        }
    }

    public void saveChart(XYChart chart) {
        try {
            if (!new File(".//plugins/ImageMaps/images").exists()) {
                new File(".//plugins/ImageMaps/images").mkdir();
            }

            File chartImage = new File(".//plugins/BanditMaps/images/" + chart.getTitle() + ".png");

            if (chartImage.exists()) {
                chartImage.delete();
            }

            BitmapEncoder.saveBitmap(chart, ".//plugins/BanditMaps/images/" + chart.getTitle(), BitmapEncoder.BitmapFormat.PNG);

            //Reload all charts
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "banditmaps " + chart.getTitle() + ".png reload");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startChartUpdate() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::generateGraph, 60L, 20L * 60L * 5L); //5 minutes
    }


}
