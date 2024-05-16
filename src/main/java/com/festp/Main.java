package com.festp;

import java.io.File;
import java.util.List;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.commands.LinksCommand;
import com.festp.config.Config;
import com.festp.config.Config.Key;
import com.festp.config.LangConfig;
import com.festp.handlers.ChatHandler;
import com.festp.handlers.SmallCommandsHandler;
import com.festp.handlers.WhisperHandler;
import com.festp.messaging.Chatter;
import com.festp.messaging.MessageSender;
import com.festp.messaging.RawJsonChatter;
import com.festp.messaging.SpigotMessageSender;
import com.festp.parsing.CommandParser;
import com.festp.parsing.ComponentParser;
import com.festp.parsing.CopyableTextParser;
import com.festp.parsing.LinkParser;
import com.festp.parsing.TextStyleParser;
import com.festp.styledmessage.StyledMessageBuilder;
import com.festp.utils.SpigotCommandValidator;
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

		List<ComponentParser> globalParsers = Lists.newArrayList(new TextStyleParser());
		List<ComponentParser> splittingParsers = Lists.newArrayList();
		if (config.get(Key.ENABLE_COPYABLE_TEXT, false))
			splittingParsers.add(new CopyableTextParser(config.get(Key.COPYABLE_TEXT_BEGIN_QUOTES), config.get(Key.COPYABLE_TEXT_END_QUOTES)));
		if (config.get(Key.ENABLE_LINKS, false))
			splittingParsers.add(new LinkParser());
		if (config.get(Key.ENABLE_COMMANDS, false))
			splittingParsers.add(new CommandParser(new SpigotCommandValidator(), config.get(Key.COMMANDS_REMOVE_STARTING_DOT, true)));
		StyledMessageBuilder styledMessageBuilder = new StyledMessageBuilder(globalParsers, splittingParsers);
		MessageSender messageSender = new SpigotMessageSender(this, config);
		Chatter chatter = new RawJsonChatter(config, styledMessageBuilder, messageSender);

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
