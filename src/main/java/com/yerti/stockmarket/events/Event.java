package com.yerti.stockmarket.events;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

public class Event {

	private String message;
	private int scalar;
	private boolean up = false;
	private int frequency;
	
	public Event(String message, int scalar, boolean up, int freq) {
		this.message = message;
		this.scalar = scalar;
		this.up = up;
		frequency = freq;


	}
	
	public String getMessage() {

		if (!up) {
			return ChatColor.RED + message;
		} else {
			return ChatColor.GREEN + message;
		}


		//return message;
	}
	
	public int getScalar() {
		return scalar;
	}
	
	public boolean getUp () {
		return up;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
}
