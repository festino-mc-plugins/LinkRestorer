package com.festp.messaging;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.Logger;
import com.festp.config.Config;

public class SpigotMessageSender implements MessageSender
{
	
	private JavaPlugin plugin;
	private Config config;
	
	public SpigotMessageSender(JavaPlugin plugin, Config config)
	{
		this.plugin = plugin;
		this.config = config;
	}
	
	public void sendRawJson(Player player, CharSequence rawJsonMessage)
	{
		boolean isLogging = config.get(Config.Key.LOG_DEBUG, false);
		if (isLogging) Logger.info("Sending raw JSON: " + rawJsonMessage);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				StringBuilder command = new StringBuilder("tellraw ");
				command.append(player.getName());
				command.append(' ');
				command.append(rawJsonMessage);
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.toString());
			}
		});
	}
}
