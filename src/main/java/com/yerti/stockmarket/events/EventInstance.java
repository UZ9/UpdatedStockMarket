package com.yerti.stockmarket.events;


import com.yerti.stockmarket.StockMarket;
import com.yerti.stockmarket.stocks.Stock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Random;

public class EventInstance {

    private Random random = new Random();

    private Event getRandomEvent() {
        int r = random.nextInt(totalPossibilities());
        int i = 0;

        Iterator<Event> it = StockMarket.events.iterator();
        while (it.hasNext()) {
            Event e = it.next();
            i += e.getFrequency();
            if (r < i) {
                return e;
            }
        }

        return null;
    }

    private int totalPossibilities() {
        int i = 0;
        Iterator<Event> it = StockMarket.events.iterator();
        while (it.hasNext()) {
            Event e = it.next();
            i += e.getFrequency();
        }

        return i;
    }

    //100000
    //10000
    //900000

    public boolean forceRandomEvent(Stock s) {
        Event e = getRandomEvent();


        Random rand = new Random();
        boolean val = rand.nextInt(30) == 0;

        long newPrice = s.updatePrice(e.getUp(), val ? e.getScalar() * 1.5 : e.getScalar());

        if (val) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!StockMarket.getInstance().toggledUsers.contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "STOCK " + ChatColor.DARK_GRAY + "\u00BB " + ChatColor.DARK_GREEN + e.getMessage().replace("%s", s.getName()));
                }
            }
        }


        double percentIncrease = (newPrice / (s.getPrice() / 100.));


        s.setLastPercent(percentIncrease);
        s.changePrice(newPrice);


        return true;
    }


}
