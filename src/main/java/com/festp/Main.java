package com.festp;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import com.festp.commands.LinksCommand;
import com.festp.config.Config;
import com.festp.config.LangConfig;
import com.festp.handlers.ChatListenerManager;

public class Main extends JavaPlugin
{
	private ChatListenerManager listenerManager;
	
	public void onEnable()
	{
		Logger.setLogger(getLogger());
		LangConfig lang = new LangConfig(new File(getDataFolder(), "lang.yml"));
		lang.load();
		Config config = new Config(this, lang);
		config.load();
		
		listenerManager = new ChatListenerManager(this, config);
		listenerManager.register();
		config.addListener(listenerManager);
		// TODO send messages on join (to players having configure permission) if (!listenerManager.canRegister())

		LinksCommand commandWorker = new LinksCommand(config, lang);
		getCommand(LinksCommand.COMMAND).setExecutor(commandWorker);
		getCommand(LinksCommand.COMMAND).setTabCompleter(commandWorker);
	}
	
	public void onDisable()
	{
		listenerManager.unregister();
		getCommand(LinksCommand.COMMAND).setExecutor(null);
		getCommand(LinksCommand.COMMAND).setTabCompleter(null);
	}
}
