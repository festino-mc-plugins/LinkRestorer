package com.festp.handlers;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.festp.Logger;
import com.festp.config.Config;
import com.festp.config.ConfigListener;
import com.festp.config.Config.Key;
import com.festp.messaging.Chatter;
import com.festp.messaging.MessageSender;
import com.festp.messaging.ChatComponentChatter;
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
	private final MemoryChatHandler memoryChatHandler;
	private BukkitTask clearMessageInfoTask;
	
	private boolean listenPackets;
	
	public ChatListenerManager(JavaPlugin plugin, Config config)
	{
		this.plugin = plugin;
		this.config = config;
		listenPackets = getListenPackets();
		
		StyledMessageBuilderFactory factory = new TheStyledMessageBuilderFactory(config, new SpigotCommandValidator(config));
		MessageSender messageSender = new SpigotMessageSender(plugin, config);
		Chatter chatter = new ChatComponentChatter(config, factory, messageSender);

		chatHandler = new ChatHandler(chatter);
		smallHandler = new SmallCommandsHandler(chatter);
		whisperHandler = new WhisperHandler(chatter, config);
		
		if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
			memoryChatHandler = new MemoryChatHandler();
			packetListener = new ChatPacketListener(plugin, chatter, memoryChatHandler, config);
			clearMessageInfoTask = null;
		}
		else {
			memoryChatHandler = null;
			packetListener = null;
			clearMessageInfoTask = null;
		}
	}
	
	public void register()
	{
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(smallHandler, plugin);
		pm.registerEvents(whisperHandler, plugin);
		
		if (listenPackets)
		{
			if (packetListener != null) {
				Logger.info("Registering ProtocolLib chat listeners...");
				packetListener.register();
				pm.registerEvents(memoryChatHandler, plugin);
				clearMessageInfoTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
					@Override
					public void run() {
						memoryChatHandler.onTick();
					}
				}, 0, 1);
			} else {
				Logger.warning("Could not find ProtocolLib plugin, chat messages will not be affected.");
			}
			return;
		}

		Logger.info("Registering Spigot chat listener...");
		pm.registerEvents(chatHandler, plugin);
	}
	
	public void unregister()
	{
		HandlerList.unregisterAll(smallHandler);
		HandlerList.unregisterAll(whisperHandler);
		
		if (listenPackets)
		{
			if (packetListener != null) {
				packetListener.unregister();
				HandlerList.unregisterAll(memoryChatHandler);
				if (clearMessageInfoTask != null) plugin.getServer().getScheduler().cancelTask(clearMessageInfoTask.getTaskId());
			}
			return;
		}
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
