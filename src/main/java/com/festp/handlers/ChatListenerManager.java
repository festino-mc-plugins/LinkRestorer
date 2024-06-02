package com.festp.handlers;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.Logger;
import com.festp.config.Config;
import com.festp.config.ConfigListener;
import com.festp.config.Config.Key;
import com.festp.messaging.Chatter;
import com.festp.messaging.MessageSender;
import com.festp.messaging.RawJsonChatter;
import com.festp.messaging.SpigotMessageSender;
import com.festp.styledmessage.StyledMessageBuilderFactory;
import com.festp.styledmessage.TheStyledMessageBuilderFactory;
import com.festp.utils.SpigotCommandValidator;

public class ChatListenerManager implements ListenerManager, ConfigListener
{
	private final JavaPlugin plugin;
	private final Config config;
	
	private final ChatHandler chatHandler;
	private final SmallCommandsHandler smallHandler;
	private final WhisperHandler whisperHandler;
	/** equals null if could not access ProtocolLib */
	private final ChatPacketListener packetListener;
	
	private boolean listenPackets;
	
	public ChatListenerManager(JavaPlugin plugin, Config config)
	{
		this.plugin = plugin;
		this.config = config;
		listenPackets = getListenPackets();
		
		StyledMessageBuilderFactory factory = new TheStyledMessageBuilderFactory(config, new SpigotCommandValidator(config));
		MessageSender messageSender = new SpigotMessageSender(plugin, config);
		Chatter chatter = new RawJsonChatter(config, factory, messageSender);

		chatHandler = new ChatHandler(chatter);
		smallHandler = new SmallCommandsHandler(chatter);
		whisperHandler = new WhisperHandler(chatter, config);
		
		if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib"))
			packetListener = new ChatPacketListener(plugin, chatter);
		else
			packetListener = null;
	}
	
	public void register()
	{
		if (listenPackets)
		{
			if (packetListener != null) {
				Logger.info("Registering ProtocolLib listeners...");
				packetListener.register();
			} else {
				Logger.warning("Could not find ProtocolLib plugin, chat messages will not be affected.");
			}
			return;
		}

		Logger.info("Registering Spigot listeners...");
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(smallHandler, plugin);
		pm.registerEvents(whisperHandler, plugin);
		pm.registerEvents(chatHandler, plugin);
	}
	
	public void unregister()
	{
		if (listenPackets)
		{
			if (packetListener != null) packetListener.unregister();
			return;
		}
		
		HandlerList.unregisterAll(smallHandler);
		HandlerList.unregisterAll(whisperHandler);
		HandlerList.unregisterAll(chatHandler);
	}
	
	public boolean canRegister()
	{
		return !listenPackets || packetListener != null;
	}
	
	public void onConfigUpdate()
	{
		boolean newListenPackets = getListenPackets();
		if (listenPackets == newListenPackets) return;
		
		unregister();
		listenPackets = newListenPackets;
		register();
	}
	
	private boolean getListenPackets()
	{
		return config.get(Key.MODIFY_PACKETS, false);
	}
}
