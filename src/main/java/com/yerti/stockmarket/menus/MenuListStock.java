package com.yerti.stockmarket.menus;

import com.yerti.stockmarket.core.inventories.CustomInventory;
import com.yerti.stockmarket.core.inventories.IInventory;
import com.yerti.stockmarket.core.items.ItemstackModifier;
import com.yerti.stockmarket.stocks.PlayerStocks;
import com.yerti.stockmarket.stocks.Stock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.text.DecimalFormat;
import java.util.*;

public class MenuListStock implements IInventory {

    private Player player;
    private Map<ItemStack, Stock> stockItems;
    private Map<String, Stock> stockIDs = new HashMap<>();
    private static Plugin plugin;
    private static Map<Player, Integer> currentPage;

    public MenuListStock(Plugin plugin) {
        this.plugin = plugin;
    }

    public MenuListStock(Player player) {
        if (currentPage == null) currentPage = new HashMap<>();

        this.player = player;

        this.currentPage.put(player, 1);

        this.stockItems = new HashMap<>();
    }

    public void generateStockItems() {
        PlayerStocks ps = new PlayerStocks(player);

        DecimalFormat format = new DecimalFormat("#,###.##");

        stockItems.clear();

        for (Stock stock : ps.retrieveStocks()) {
            ItemStack stack = new ItemStack(Material.EMERALD, 1);
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(ChatColor.GREEN + stock.getName());
            List<String> lore = new ArrayList<>();

            double lastPercent = stock.getLastPercent();
            String percentFormatted = lastPercent + "";

            if (lastPercent < 0) {
                percentFormatted = ChatColor.RED + percentFormatted + "%";
            } else {
                percentFormatted = ChatColor.GREEN + percentFormatted + "%";
            }

            lore.add(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------");
            lore.add(ChatColor.GRAY + "Current Price:");
            lore.add(ChatColor.DARK_GRAY + "\u00BB " + ChatColor.RED + "$" + format.format(stock.getPrice()));
            lore.add("");
            lore.add(ChatColor.GRAY + "Amount Left:");
            lore.add(ChatColor.DARK_GRAY + "\u00BB " + ChatColor.RED + format.format(stock.getAmount()));
            lore.add("");
            lore.add(ChatColor.GRAY + "Owned Stock:");
            lore.add(ChatColor.DARK_GRAY + "\u00BB " + ChatColor.RED + ps.numStock(stock));
            lore.add("");
            lore.add(ChatColor.GRAY + "Last Price Change:");
            lore.add(ChatColor.DARK_GRAY + "\u00BB " + percentFormatted);
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "\u00BB " + ChatColor.GRAY + "S. Left Click " + ChatColor.DARK_GRAY + "\u00BB " + ChatColor.RED + "Buy 10");
            lore.add(ChatColor.DARK_GRAY + "\u00BB " + ChatColor.GRAY + "Left Click " + ChatColor.DARK_GRAY + "\u00BB "  + ChatColor.RED + "Buy 1");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "\u00BB " + ChatColor.GRAY + "S. Right Click " + ChatColor.DARK_GRAY + "\u00BB " + ChatColor.RED + "Sell 10");
            lore.add(ChatColor.DARK_GRAY + "\u00BB " + ChatColor.GRAY + "Right Click " + ChatColor.DARK_GRAY + "\u00BB "  + ChatColor.RED + "Sell 1");

            lore.add(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------");

            meta.setLore(lore);

            stack.setItemMeta(meta);

            stockItems.put(stack, stock);
            stockIDs.put(ChatColor.stripColor(stack.getItemMeta().getDisplayName()), stock);
        }
    }

    public Inventory getInventory() {
        generateStockItems();

        List<ItemStack> itemStacks = new ArrayList<>(stockItems.keySet());


        Collections.sort(itemStacks, Comparator.comparing(s -> s.getItemMeta().getDisplayName()));

        CustomInventory inventory = new CustomInventory(this, 27, ChatColor.RED + "" + ChatColor.BOLD + "STOCK", new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15));//)page.build();

        int index = 0;
        for (ItemStack stack : itemStacks) {
            inventory.getInventory().setItem(index, stack);
            index++;
        }

        inventory.createBackground();
        inventory.createFooter(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7));

        inventory.getInventory().setItem(22, new ItemstackModifier(new ItemStack(Material.BOOK, 1)).setDisplayName(ChatColor.AQUA + "Stock Help")
                .addLore("&8&l&m----------------------------&r")
                .addLore("")
                .addLore("&7The &cStockMarket&7 is a dedicated plugin ")
                .addLore("&7to simulate the real world market of")
                .addLore("&7investing in buying and selling")
                .addLore("&7&cstocks&7 from various companies")
                .addLore("&7in order to make a profit.")
                .addLore("")
                .addLore("&7To start, begin by hovering over the")
                .addLore("&7different &aemeralds&7 located in the menu.")
                .addLore("")
                .addLore("&7Every &cminute&7, every")
                .addLore("&7stock will either have an")
                .addLore("&7increase &7in value or a &7drop.")
                .addLore("&7Periodically, there will be a major")
                .addLore("&7event, resulting in either a &aboom")
                .addLore("&7or a &cstock market crash&7.")
                .addLore("")
                .addLore("&7For more information, please take a")
                .addLore("&7look at the StockMarket forum post.")
                .addLore("&8&l&m----------------------------&r")
                .build());


        return inventory.getInventory();
    }


        @Override
        public void onGUI(Player player, int slot, ItemStack clickedItem, InventoryClickEvent event) {
            event.setCancelled(true);
            if (clickedItem == null) return;
            if (clickedItem.getType().equals(Material.EMERALD)) {

                final Runnable runnable = () -> player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());
                if (event.getClick().equals(ClickType.LEFT) || event.getClick().equals(ClickType.SHIFT_LEFT)) {

                    Stock stock = stockIDs.get(ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
                    //Stock stock = stockItems.get(clickedItem);

                    int amountToBuy = event.isShiftClick() ? 10 : 1;

                    PlayerStocks playerStocks = new PlayerStocks(player);
                    playerStocks.buy(stock, amountToBuy);
                    event.setCancelled(true);
                    //Give 3 ticks for mysql in async thread to be written
                    //Bukkit.getScheduler().runTaskLater(plugin, runnable, 3L);
                    return;
                    
                }

                if (event.getClick().equals(ClickType.RIGHT) || event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                    Stock stock = stockItems.get(clickedItem);

                    int amountToBuy = event.isShiftClick() ? 10 : 1;

                    PlayerStocks playerStocks = new PlayerStocks(player);
                    playerStocks.sell(stock, amountToBuy);

                    event.setCancelled(true);
                    //Give 3 ticks for mysql in async thread to be written
                    //Bukkit.getScheduler().runTaskLater(plugin, runnable, 3L);
                    return;
                }


            }

        }




}