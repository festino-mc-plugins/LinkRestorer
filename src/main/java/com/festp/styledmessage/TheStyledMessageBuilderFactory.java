package com.festp.styledmessage;

import java.util.List;

import org.bukkit.permissions.Permissible;

import com.festp.config.Config;
import com.festp.config.Config.Key;
import com.festp.parsing.CommandParser;
import com.festp.parsing.ComponentParser;
import com.festp.parsing.CopyableTextParser;
import com.festp.parsing.LinkParser;
import com.festp.parsing.TextStyleParser;
import com.festp.utils.CommandValidator;
import com.google.common.collect.Lists;

public class TheStyledMessageBuilderFactory implements StyledMessageBuilderFactory
{
	private static final String PERMISSION_LINKS = "clickablelinks.use.links";
	private static final String PERMISSION_COMMANDS = "clickablelinks.use.commands";
	private static final String PERMISSION_COPYABLE_TEXT = "clickablelinks.use.copyable";
	
	private final Config config;
	private final CommandValidator commandValidator;
	
	public TheStyledMessageBuilderFactory(Config config, CommandValidator commandValidator) {
		this.config = config;
		this.commandValidator = commandValidator;
	}

	@Override
	public StyledMessageBuilder create(Permissible user)
	{
		boolean useLinks = config.get(Key.ENABLE_LINKS, false) && user.hasPermission(PERMISSION_LINKS);
		boolean useCommands = config.get(Key.ENABLE_COMMANDS, false) && user.hasPermission(PERMISSION_COMMANDS);
		boolean useCopyableText = config.get(Key.ENABLE_COPYABLE_TEXT, false) && user.hasPermission(PERMISSION_COPYABLE_TEXT);
		List<ComponentParser> globalParsers = Lists.newArrayList(getTextStyleParser());
		
		List<ComponentParser> splittingParsers = Lists.newArrayList();
		if (useCommands || useCopyableText)
			splittingParsers.add(getCopyableTextParser(useCommands, useCopyableText));
		if (useLinks)
			splittingParsers.add(getLinkParser());
		if (useCommands)
			splittingParsers.add(getCommandParser());
		
		return new TheStyledMessageBuilder(globalParsers, splittingParsers);
	}
	
	private ComponentParser getTextStyleParser()
	{
		return new TextStyleParser();
	}
	
	private ComponentParser getCopyableTextParser(boolean useCommands, boolean useCopyableText)
	{
		return new CopyableTextParser(
				config.get(Key.COPYABLE_TEXT_BEGIN_QUOTES), config.get(Key.COPYABLE_TEXT_END_QUOTES),
				useCommands, useCopyableText, commandValidator);
	}
	
	private ComponentParser getLinkParser()
	{
		return new LinkParser();
	}
	
	private ComponentParser getCommandParser()
	{
		return new CommandParser(commandValidator, config.get(Key.COMMANDS_REMOVE_STARTING_DOT, true));
	}
}
