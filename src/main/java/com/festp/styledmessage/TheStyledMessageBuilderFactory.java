package com.festp.styledmessage;

import java.util.List;

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
	private final Config config;
	private final CommandValidator commandValidator;
	
	public TheStyledMessageBuilderFactory(Config config, CommandValidator commandValidator) {
		this.config = config;
		this.commandValidator = commandValidator;
	}

	@Override
	public StyledMessageBuilder create()
	{
		List<ComponentParser> globalParsers = Lists.newArrayList(getTextStyleParser());
		
		List<ComponentParser> splittingParsers = Lists.newArrayList();
		if (config.get(Key.ENABLE_COPYABLE_TEXT, false))
			splittingParsers.add(getCopyableTextParser());
		if (config.get(Key.ENABLE_LINKS, false))
			splittingParsers.add(getLinkParser());
		if (config.get(Key.ENABLE_COMMANDS, false))
			splittingParsers.add(getCommandParser());
		
		return new TheStyledMessageBuilder(globalParsers, splittingParsers);
	}
	
	private ComponentParser getTextStyleParser()
	{
		return new TextStyleParser();
	}
	
	private ComponentParser getCopyableTextParser()
	{
		return new CopyableTextParser(
				config.get(Key.COPYABLE_TEXT_BEGIN_QUOTES), config.get(Key.COPYABLE_TEXT_END_QUOTES),
				config.get(Key.COPYABLE_TEXT_ONLY_COMMANDS), commandValidator);
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
