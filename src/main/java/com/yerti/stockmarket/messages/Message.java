package com.yerti.stockmarket.messages;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Message {

	private Player player;
	private final String prefix = ChatColor.RED + "" + ChatColor.BOLD + "STOCK" + ChatColor.DARK_GRAY + " \u00BB" + ChatColor.GRAY;
	
	public Message (Player player) {
		this.player = player;
	}
	
	public void displayInfo () {
		successMessage("Current version: v" + Bukkit.getServer().getPluginManager().getPlugin("StockMarket").getDescription().getVersion() + " developed by Yerti.");
	}
	
	public void unknownCommand () {
		errorMessage("Unknown command.  Use /sm help for help.");
	}
	
	public void errorMessage(String message) {
		if (player != null)
			player.sendMessage(prefix + ChatColor.RED + " " + message);
		else
			System.out.println("[Stock] " + message);
	}
	
	public void regularMessage(String message) {
		if (player != null)
			player.sendMessage(prefix + ChatColor.BLUE + " " + message);
		else
			System.out.println("[Stock] " + message);
	}
	
	public void successMessage(String message) {
		if (player != null)
			player.sendMessage(prefix +  " " + message);
		else
			System.out.println("[Stock] " + message);
	}
	
	public void helpMessage(String message) {
		successMessage(message);
	}
	
	public void displayHelp (int page) {
		Help h = new Help(player);
		h.display(page);
	}
	
}
