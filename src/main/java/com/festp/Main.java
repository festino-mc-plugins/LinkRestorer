package com.festp;

import java.io.File;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.commands.LinksCommand;
import com.festp.config.Config;
import com.festp.config.LangConfig;
import com.festp.handlers.ChatHandler;
import com.festp.handlers.SmallCommandsHandler;
import com.festp.handlers.WhisperHandler;
import com.festp.messaging.Chatter;
import com.festp.parsing.ComponentParser;
import com.festp.parsing.LinkParser;
import com.festp.parsing.TextStyleParser;
import com.festp.styledmessage.StyledMessageBuilder;
import com.google.common.collect.Lists;

public class Main extends JavaPlugin
{
	public void onEnable()
	{
		Logger.setLogger(getLogger());
		LangConfig lang = new LangConfig(new File(getDataFolder(), "lang.yml"));
		lang.load();
		Config config = new Config(this, lang);
		config.load();

		ComponentParser textStyleParser = new TextStyleParser();
		StyledMessageBuilder formatParser = new StyledMessageBuilder(Lists.newArrayList(textStyleParser), Lists.newArrayList());
		ComponentParser[] splittingParsers = new ComponentParser[] { new LinkParser() };
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(textStyleParser), Lists.newArrayList(splittingParsers));
		Chatter chatter = new Chatter(this, config, parser, formatParser);

		PluginManager pm = getServer().getPluginManager();

		ChatHandler chatHandler = new ChatHandler(chatter);
		pm.registerEvents(chatHandler, this);

		SmallCommandsHandler smallHandler = new SmallCommandsHandler(chatter);
		pm.registerEvents(smallHandler, this);

		WhisperHandler whisperHandler = new WhisperHandler(chatter, config);
		pm.registerEvents(whisperHandler, this);

		LinksCommand commandWorker = new LinksCommand(config, lang);
		getCommand(LinksCommand.COMMAND).setExecutor(commandWorker);
		getCommand(LinksCommand.COMMAND).setTabCompleter(commandWorker);
	}
}
